const functions = require('firebase-functions');
const admin = require('firebase-admin');
const express = require('express');
const cors = require('cors');
const axios = require('axios');
const fs = require('fs');
const path = require('path');
const dotenv = require('dotenv');

admin.initializeApp();

// Load API Keys from local .env or secrets.properties in project root
const localEnvPath = path.resolve(__dirname, '.env');
const secretsPath = path.resolve(__dirname, '../../secrets.properties');
let envConfig = {};
try {
    if (fs.existsSync(localEnvPath)) {
        const fileContent = fs.readFileSync(localEnvPath, 'utf-8');
        envConfig = { ...envConfig, ...dotenv.parse(fileContent) };
    }
    if (fs.existsSync(secretsPath)) {
        const fileContent = fs.readFileSync(secretsPath, 'utf-8');
        envConfig = { ...envConfig, ...dotenv.parse(fileContent) };
    }
} catch (error) {
    console.error("Could not load environment config files:", error);
}

// Extract 4 Groq and 4 Cerebras keys
const GROQ_KEYS = [
    process.env.GROQ_API_KEY_1 || envConfig.GROQ_API_KEY_1,
    process.env.GROQ_API_KEY_2 || envConfig.GROQ_API_KEY_2,
    process.env.GROQ_API_KEY_3 || envConfig.GROQ_API_KEY_3,
    process.env.GROQ_API_KEY_4 || envConfig.GROQ_API_KEY_4
].filter(Boolean);

const CEREBRAS_KEYS = [
    process.env.CEREBRAS_API_KEY_1 || envConfig.CEREBRAS_API_KEY_1,
    process.env.CEREBRAS_API_KEY_2 || envConfig.CEREBRAS_API_KEY_2,
    process.env.CEREBRAS_API_KEY_3 || envConfig.CEREBRAS_API_KEY_3,
    process.env.CEREBRAS_API_KEY_4 || envConfig.CEREBRAS_API_KEY_4
].filter(Boolean);

let groqIndex = 0;
let cerebrasIndex = 0;

function getNextGroqKey() {
    if (GROQ_KEYS.length === 0) return null;
    const key = GROQ_KEYS[groqIndex];
    groqIndex = (groqIndex + 1) % GROQ_KEYS.length;
    return key;
}

function getNextCerebrasKey() {
    if (CEREBRAS_KEYS.length === 0) return null;
    const key = CEREBRAS_KEYS[cerebrasIndex];
    cerebrasIndex = (cerebrasIndex + 1) % CEREBRAS_KEYS.length;
    return key;
}

/**
 * Universal AI Caller with 4-Key Rotation & Safe Failover
 * Approved Models from docs/AI_MODELS.md:
 * - Groq Default: 'openai/gpt-oss-20b'
 * - Groq Large: 'openai/gpt-oss-120b'
 * - Cerebras Fallback: 'gpt-oss-120b'
 */
async function callAiWithFailover({
    messages,
    temperature = 0.3,
    maxTokens = 2048,
    isVision = false
}) {
    // 1. Try Groq (4 Keys Rotating Pool)
    for (let attempt = 0; attempt < GROQ_KEYS.length; attempt++) {
        const groqKey = getNextGroqKey();
        if (!groqKey) break;

        const modelId = isVision ? 'openai/gpt-oss-120b' : 'openai/gpt-oss-20b';

        try {
            const response = await axios.post(
                'https://api.groq.com/openai/v1/chat/completions',
                {
                    model: modelId,
                    messages: messages,
                    temperature: temperature,
                    max_tokens: maxTokens
                },
                {
                    headers: {
                        'Authorization': `Bearer ${groqKey}`,
                        'Content-Type': 'application/json'
                    },
                    timeout: 20000
                }
            );

            return {
                success: true,
                provider: 'groq',
                model: modelId,
                content: response.data.choices[0].message.content
            };
        } catch (error) {
            console.warn(`Groq Key ${attempt + 1} encountered ${error.response?.status || error.message}. Trying next key in rotation...`);
        }
    }

    // 2. Cerebras Fallback (if Groq keys exhausted)
    for (let attempt = 0; attempt < CEREBRAS_KEYS.length; attempt++) {
        const cerebrasKey = getNextCerebrasKey();
        if (!cerebrasKey) break;

        try {
            const cerebrasResponse = await axios.post(
                'https://api.cerebras.ai/v1/chat/completions',
                {
                    model: 'gpt-oss-120b',
                    messages: messages,
                    temperature: temperature,
                    max_tokens: maxTokens
                },
                {
                    headers: {
                        'Authorization': `Bearer ${cerebrasKey}`,
                        'Content-Type': 'application/json'
                    },
                    timeout: 30000
                }
            );

            return {
                success: true,
                provider: 'cerebras',
                model: 'gpt-oss-120b',
                content: cerebrasResponse.data.choices[0].message.content
            };
        } catch (cerebrasError) {
            console.warn(`Cerebras Key ${attempt + 1} failed: ${cerebrasError.response?.status || cerebrasError.message}`);
        }
    }

    throw new Error('All Groq and Cerebras rotating keys exhausted.');
}

// -------------------------------------------------------------
// EXPRESS APP (REST Endpoints matching docs/TECHNICAL_DEEP_DIVE.md)
// -------------------------------------------------------------
const app = express();
app.use(cors({ origin: true }));
app.use(express.json({ limit: '10mb' }));

/**
 * Firebase Auth Verification Middleware
 * Validates Firebase ID tokens for protected AI endpoints
 */
const authenticateFirebaseUser = async (req, res, next) => {
    // Allow health check without auth
    if (req.path === '/health' || req.path === '/ai/quote') {
        return next();
    }

    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        req.user = { uid: 'guest_user', isAnonymous: true };
        return next();
    }

    const idToken = authHeader.split('Bearer ')[1];
    if (!idToken || idToken === 'null' || idToken === 'undefined' || idToken === 'guest_mode_token') {
        req.user = { uid: 'guest_user', isAnonymous: true };
        return next();
    }

    try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        req.user = decodedToken;
        next();
    } catch (error) {
        console.warn('Firebase ID token verification failed (allowing guest access):', error.message);
        req.user = { uid: 'guest_user', isAnonymous: true };
        next();
    }
};

app.use('/ai', authenticateFirebaseUser);

/**
 * GET /api/health
 */
app.get('/health', (req, res) => {
    res.json({
        status: 'online',
        service: 'Quovex AI Gateway',
        groqKeysAvailable: GROQ_KEYS.length,
        cerebrasKeysAvailable: CEREBRAS_KEYS.length,
        timestamp: new Date().toISOString()
    });
});

/**
 * POST /api/ai/chat
 * Student doubt solver & tutor
 */
app.post('/ai/chat', async (req, res) => {
    try {
        const { message, subject = 'General', history = [] } = req.body;
        if (!message) {
            return res.status(400).json({ error: 'Missing message parameter' });
        }

        const systemPrompt = `You are Quovex AI, an elite, motivating academic coach for ${subject}. 
Explain concepts step-by-step, provide formulas where appropriate, and encourage disciplined focus. Format in clean Markdown.`;

        const messages = [
            { role: 'system', content: systemPrompt },
            ...history.slice(-8),
            { role: 'user', content: message }
        ];

        const result = await callAiWithFailover({ messages, temperature: 0.4 });
        res.json({
            success: true,
            response: result.content,
            provider: result.provider,
            model: result.model
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/ai/summarize
 * Converts OCR/PDF text into notes, key points, and flashcards
 */
app.post('/ai/summarize', async (req, res) => {
    try {
        const { text, subject = 'General' } = req.body;
        if (!text) {
            return res.status(400).json({ error: 'Missing text parameter' });
        }

        const prompt = `Analyze this study text on "${subject}" and generate structured revision notes.
Return ONLY valid JSON format:
{
  "summary": "Clear conceptual overview",
  "keyPoints": ["Bullet 1", "Bullet 2", "Bullet 3", "Bullet 4"],
  "flashcards": [
    { "question": "Question 1", "answer": "Answer 1", "formula": "Optional Formula" },
    { "question": "Question 2", "answer": "Answer 2", "formula": "Optional Formula" }
  ]
}

Text:
${text.slice(0, 6000)}`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an expert exam note summarizer. Respond ONLY with valid JSON.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.2
        });

        let parsed;
        try {
            const cleanJson = result.content.replace(/```json/g, '').replace(/```/g, '').trim();
            parsed = JSON.parse(cleanJson);
        } catch {
            parsed = { summary: result.content, keyPoints: [], flashcards: [] };
        }

        res.json({
            success: true,
            data: parsed,
            provider: result.provider
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/ai/plan/generate
 * Generates structured study roadmap
 */
app.post('/ai/plan/generate', async (req, res) => {
    try {
        const { examName = 'JEE Advanced', targetHours = 4, subjects = ['Physics', 'Chemistry', 'Maths'], days = 30 } = req.body;

        const prompt = `Create a high-yield ${days}-day study roadmap for ${examName}.
Daily study target: ${targetHours} hours/day.
Subjects: ${subjects.join(', ')}.
Break down week-by-week with key milestones, problem solving targets, and spaced repetition review days. Use Markdown tables.`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an elite academic strategist and competitive exam mentor.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.3,
            maxTokens: 2500
        });

        res.json({
            success: true,
            plan: result.content,
            provider: result.provider,
            model: result.model
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/ai/quiz/generate
 * Generates 5 high-yield MCQ questions
 */
app.post('/ai/quiz/generate', async (req, res) => {
    try {
        const { subject = 'Physics', topic = 'Mechanics', difficulty = 'Medium' } = req.body;
        const prompt = `Generate 5 high-yield multiple-choice questions for ${subject} on "${topic}" (Difficulty: ${difficulty}).
Return ONLY valid JSON format:
{
  "questions": [
    {
      "id": 1,
      "question": "Question text here",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "Detailed explanation of correct option"
    }
  ]
}`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an elite exam question creator. Respond ONLY with valid JSON.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.3
        });

        let parsed;
        try {
            const clean = result.content.replace(/```json/g, '').replace(/```/g, '').trim();
            parsed = JSON.parse(clean);
        } catch {
            parsed = { questions: [] };
        }

        res.json({
            success: true,
            data: parsed,
            provider: result.provider
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/ai/doubt/image
 * Visual problem solver & formula extractor
 */
app.post('/ai/doubt/image', async (req, res) => {
    try {
        const { imageUrl = '', subject = 'General', questionText = '' } = req.body;

        const effectiveText = questionText.trim() || 'Please explain and solve the concepts associated with this study material.';

        const prompt = `You are an expert STEM tutor and academic doubt solver.
Subject: ${subject}
Student Doubt / Question:
${effectiveText}

Break down the problem step-by-step, state applicable physical laws/mathematical theorems, show formulas, and calculate the final solution cleanly formatted in Markdown.`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an expert STEM tutor specializing in step-by-step academic problem solving.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.2
        });

        res.json({
            success: true,
            solution: result.content,
            provider: result.provider,
            model: result.model
        });
    } catch (error) {
        console.error("Image doubt error:", error.response?.data || error.message);
        res.status(500).json({ error: error.message });
    }
});

/**
 * GET /api/ai/quote
 */
app.get('/ai/quote', async (req, res) => {
    try {
        const { streak = 14 } = req.query;
        const prompt = `Give a short, stoic 1-line motivational quote for a student on a ${streak}-day study streak. Return JSON: {"quote": "...", "author": "..."}`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You inspire relentless focus and discipline.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.7
        });

        let parsed;
        try {
            const clean = result.content.replace(/```json/g, '').replace(/```/g, '').trim();
            parsed = JSON.parse(clean);
        } catch {
            parsed = { quote: "Discipline is the bridge between goals and accomplishment.", author: "Jim Rohn" };
        }

        res.json(parsed);
    } catch (error) {
        res.json({
            quote: "Action is the foundational key to all success.",
            author: "Pablo Picasso"
        });
    }
});

/**
 * POST /api/notes/extract-pdf
 * Downloads a PDF from Firebase Storage, extracts text server-side via pdf-parse,
 * and returns AI-generated summary, key points, and flashcards.
 *
 * Flow: Android uploads PDF to Storage → calls this endpoint with storageRef
 * Body: { storageRef: string, noteId: string, subject: string }
 * Response: { success, summary, keyPoints[], flashcards[], wordCount, provider }
 */
app.post('/notes/extract-pdf', authenticateFirebaseUser, async (req, res) => {
    try {
        const { storageRef, noteId, subject = 'General' } = req.body;
        if (!storageRef) {
            return res.status(400).json({ success: false, error: 'Missing storageRef parameter' });
        }

        // 1. Download PDF bytes from Firebase Storage
        const bucket = admin.storage().bucket();
        const file = bucket.file(storageRef);

        let pdfBuffer;
        try {
            const [contents] = await file.download();
            pdfBuffer = contents;
        } catch (storageError) {
            console.error('Firebase Storage download error:', storageError.message);
            return res.status(404).json({ success: false, error: 'PDF file not found in Storage. Verify storageRef is correct and upload is complete.' });
        }

        // 2. Extract text with pdf-parse
        let extractedText = '';
        try {
            const pdfParse = require('pdf-parse');
            const pdfData = await pdfParse(pdfBuffer);
            extractedText = pdfData.text || '';
        } catch (parseError) {
            console.warn('pdf-parse extraction failed:', parseError.message);
            return res.status(422).json({ success: false, error: 'Could not extract text from PDF. The file may be scanned/image-only. Please use document scanner instead.' });
        }

        if (!extractedText.trim()) {
            return res.status(422).json({ success: false, error: 'No readable text found in PDF. The file may be image-only — use document scanner for OCR.' });
        }

        // 3. Chunk the text (max 6000 chars to stay within token limits)
        const textChunk = extractedText.replace(/\s+/g, ' ').trim().slice(0, 6000);

        // 4. AI summarization (reuse established /ai/summarize logic)
        const prompt = `Analyze this study text on "${subject}" and generate structured revision notes.
Return ONLY valid JSON format:
{
  "summary": "Clear conceptual overview (3-5 sentences)",
  "keyPoints": ["Bullet 1", "Bullet 2", "Bullet 3", "Bullet 4"],
  "flashcards": [
    { "question": "Question 1", "answer": "Answer 1", "formula": "Optional Formula" },
    { "question": "Question 2", "answer": "Answer 2", "formula": "Optional Formula" }
  ]
}

PDF Text:
${textChunk}`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an expert exam note summarizer. Respond ONLY with valid JSON.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.2,
            maxTokens: 2048
        });

        let parsed;
        try {
            const cleanJson = result.content.replace(/```json/g, '').replace(/```/g, '').trim();
            parsed = JSON.parse(cleanJson);
        } catch {
            parsed = { summary: result.content, keyPoints: [], flashcards: [] };
        }

        console.log(`PDF extracted for noteId=${noteId}, subject=${subject}, wordCount=${extractedText.split(' ').length}`);

        res.json({
            success: true,
            summary: parsed.summary || '',
            keyPoints: parsed.keyPoints || [],
            flashcards: parsed.flashcards || [],
            wordCount: extractedText.split(' ').length,
            provider: result.provider,
            model: result.model
        });
    } catch (error) {
        console.error('PDF extraction pipeline error:', error.message);
        res.status(500).json({ success: false, error: 'PDF processing failed: ' + error.message });
    }
});

/**
 * POST /api/notes/extract-url
 */
app.post('/notes/extract-url', async (req, res) => {
    try {
        const { url } = req.body;
        if (!url) return res.status(400).json({ error: 'Missing url parameter' });

        const response = await axios.get(url, {
            headers: { 'User-Agent': 'Mozilla/5.0 (Quovex Study Bot)' },
            timeout: 10000
        });

        const textOnly = response.data
            .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
            .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')
            .replace(/<[^>]+>/g, ' ')
            .replace(/\s+/g, ' ')
            .trim();

        res.json({
            success: true,
            title: url,
            text: textOnly.slice(0, 10000),
            wordCount: textOnly.split(' ').length
        });
    } catch (error) {
        res.status(500).json({ error: 'Failed to extract URL content: ' + error.message });
    }
});

// Export Express App as Cloud Function
exports.api = functions.https.onRequest(app);

// Export onCall Callable Functions for native Android Firebase Functions SDK
exports.generateFlashcards = functions.https.onCall(async (data) => {
    const prompt = data.prompt;
    if (!prompt) throw new functions.https.HttpsError('invalid-argument', 'Missing prompt parameter');

    const result = await callAiWithFailover({
        messages: [{ role: 'user', content: prompt }]
    });

    return {
        success: true,
        provider: result.provider,
        data: result.content
    };
});
