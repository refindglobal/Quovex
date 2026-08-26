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

const MATH_READABILITY_RULES = `
CRITICAL READABILITY & MATHEMATICAL NOTATION RULES:
1. Use clean unicode superscripts for exponents: x², x³, aⁿ, 10⁻³, 2⁵ (do NOT use raw carets like x^2 when avoidable).
2. For roots, use standard symbols: √x, √(x² + y²), ∛x rather than sqrt(...).
3. Use readable Greek symbols and mathematical operators: θ, α, β, γ, π, λ, μ, σ, Ω, ≤, ≥, ≠, →, ± rather than typing words like "theta".
4. For trigonometry, use clean notation: sin θ, cos θ, tan θ, sin² θ, cos² θ.
5. For chemistry, format chemical formulas cleanly: H₂O, CO₂, O₂, H₂SO₄, NaCl.
6. For units, preserve standard spacing: 5 m/s², 9.8 m/s², 20 N, 3.5 kg, 25 °C.
7. Visually separate equations and key formulas on their own lines.
8. Do NOT alter code syntax inside programming code blocks.
9. Identity is always "Quovex AI" — never mention underlying LLM provider names or internal model IDs.
`;

/**
 * Universal AI Caller with 4-Key Rotation & Safe Failover
 * Approved Models from docs/AI_MODELS.md:
 * - Groq Default: 'openai/gpt-oss-20b'
 * - Groq Vision/Large: 'openai/gpt-oss-120b'
 * - Cerebras Fallback: 'gpt-oss-120b' / 'gemma-4-31b' (vision)
 */
async function callAiWithFailover({
    messages,
    temperature = 0.3,
    maxTokens = 2048,
    isVision = false
}) {
    // 1. Try Groq Primary Model (4 Keys Rotating Pool)
    const primaryModel = isVision ? 'qwen/qwen3.6-27b' : 'openai/gpt-oss-20b';
    for (let attempt = 0; attempt < GROQ_KEYS.length; attempt++) {
        const groqKey = getNextGroqKey();
        if (!groqKey) break;

        try {
            const response = await axios.post(
                'https://api.groq.com/openai/v1/chat/completions',
                {
                    model: primaryModel,
                    messages: messages,
                    temperature: temperature,
                    max_tokens: maxTokens
                },
                {
                    headers: {
                        'Authorization': `Bearer ${groqKey}`,
                        'Content-Type': 'application/json'
                    },
                    timeout: isVision ? 30000 : 20000
                }
            );

            return {
                success: true,
                provider: 'groq',
                model: primaryModel,
                content: response.data.choices[0].message.content
            };
        } catch (error) {
            console.warn(`Groq Key ${attempt + 1} (${primaryModel}) failed: ${error.response?.status || error.message}. Trying next key...`);
        }
    }

    // 1b. Try Groq Secondary Fallback Model (qwen/qwen3.6-27b for chat/study)
    if (!isVision) {
        for (let attempt = 0; attempt < GROQ_KEYS.length; attempt++) {
            const groqKey = getNextGroqKey();
            if (!groqKey) break;

            try {
                const response = await axios.post(
                    'https://api.groq.com/openai/v1/chat/completions',
                    {
                        model: 'qwen/qwen3.6-27b',
                        messages: messages,
                        temperature: temperature,
                        max_tokens: maxTokens
                    },
                    {
                        headers: {
                            'Authorization': `Bearer ${groqKey}`,
                            'Content-Type': 'application/json'
                        },
                        timeout: 25000
                    }
                );

                return {
                    success: true,
                    provider: 'groq',
                    model: 'qwen/qwen3.6-27b',
                    content: response.data.choices[0].message.content
                };
            } catch (error) {
                console.warn(`Groq Fallback Key ${attempt + 1} failed: ${error.response?.status || error.message}`);
            }
        }
    }

    // 2. Cerebras Fallback (if Groq keys exhausted)
    for (let attempt = 0; attempt < CEREBRAS_KEYS.length; attempt++) {
        const cerebrasKey = getNextCerebrasKey();
        if (!cerebrasKey) break;

        const cerebrasModel = isVision ? 'gemma-4-31b' : 'gpt-oss-120b';

        try {
            const cerebrasResponse = await axios.post(
                'https://api.cerebras.ai/v1/chat/completions',
                {
                    model: cerebrasModel,
                    messages: messages,
                    temperature: temperature,
                    max_tokens: maxTokens
                },
                {
                    headers: {
                        'Authorization': `Bearer ${cerebrasKey}`,
                        'Content-Type': 'application/json'
                    },
                    timeout: 35000
                }
            );

            return {
                success: true,
                provider: 'cerebras',
                model: cerebrasModel,
                content: cerebrasResponse.data.choices[0].message.content
            };
        } catch (cerebrasError) {
            console.warn(`Cerebras Key ${attempt + 1} failed: ${cerebrasError.response?.status || cerebrasError.message}`);
        }
    }

    throw new Error('Quovex AI is temporarily busy. Please try again.');
}

function extractJson(text) {
    if (!text) return null;
    let clean = text.replace(/<think>[\s\S]*?<\/think>/gi, '').trim();
    // Remove outer code fences if present
    clean = clean.replace(/^```json\s*/i, '').replace(/^```\s*/, '').replace(/```$/, '').trim();

    // 1. Direct parse attempt
    try {
        return JSON.parse(clean);
    } catch (e1) {}

    // 2. Extract largest outer JSON object
    const firstBrace = clean.indexOf('{');
    const lastBrace = clean.lastIndexOf('}');
    if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
        const jsonCandidate = clean.slice(firstBrace, lastBrace + 1);
        try {
            return JSON.parse(jsonCandidate);
        } catch (e2) {
            // 3. Fix unescaped backslashes inside JSON strings (very common with LaTeX)
            try {
                // Escape backslashes that are not valid JSON escape sequences: \", \\, \/, \b, \f, \n, \r, \t, \uXXXX
                const fixed = jsonCandidate.replace(/\\(?!["\\/bfnrt]|u[0-9a-fA-F]{4})/g, '\\\\');
                return JSON.parse(fixed);
            } catch (e3) {
                try {
                    // Strip all single backslashes except quotes
                    const stripped = jsonCandidate.replace(/\\(?!")/g, '');
                    return JSON.parse(stripped);
                } catch (e4) {}
            }
        }
    }
    return null;
}

function cleanMathAndLatex(raw) {
    if (!raw || typeof raw !== 'string') return raw;
    let text = raw;

    // 0. Remove internal model thinking tags (<think>...</think>)
    text = text.replace(/<think>[\s\S]*?<\/think>/gi, '').trim();

    // 1. Remove LaTeX delimiters
    text = text.replace(/\\\[/g, '\n').replace(/\\\]/g, '\n');
    text = text.replace(/\\\(/g, '').replace(/\\\)/g, '');
    text = text.replace(/\$\$/g, '\n').replace(/\$/g, '');

    // 2. Remove LaTeX formatting tags
    text = text.replace(/\\displaystyle/g, '');
    text = text.replace(/\\limits/g, '');
    text = text.replace(/\\nolimits/g, '');

    // 3. Clean \boxed{...}, \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}
    text = text.replace(/\\(?:boxed|text|mathrm|mathbf|mathit|boldsymbol|overline)\{([^}]*)\}/g, '$1');
    text = text.replace(/\\(?:boxed|text|mathrm|mathbf|mathit|boldsymbol|overline)\{([^}]*)\}/g, '$1');

    // 4. Fractions: \frac{a}{b} -> (a / b)
    text = text.replace(/\\frac\{([^}]*)\}\{([^}]*)\}/g, '($1 / $2)');
    text = text.replace(/\\frac\{([^}]*)\}\{([^}]*)\}/g, '($1 / $2)');

    // 5. Roots: \sqrt[n]{x} and \sqrt{x}
    text = text.replace(/\\sqrt\[(.*?)\]\{(.*?)\}/g, '$1√($2)');
    text = text.replace(/\\sqrt\{([^}]*)\}/g, '√($1)');

    // 6. Calculus: \int, \sum
    text = text.replace(/\\int_\{?(.*?)\}?\^\{?(.*?)\}?/g, '∫[$1 to $2] ');
    text = text.replace(/\\int/g, '∫ ');
    text = text.replace(/\\sum_\{?(.*?)\}?\^\{?(.*?)\}?/g, '∑[$1 to $2] ');
    text = text.replace(/\\sum/g, '∑ ');

    // 7. Greek letters
    const greek = {
        '\\alpha': 'α', '\\beta': 'β', '\\gamma': 'γ', '\\delta': 'δ',
        '\\epsilon': 'ε', '\\varepsilon': 'ε', '\\zeta': 'ζ', '\\eta': 'η',
        '\\theta': 'θ', '\\vartheta': 'θ', '\\iota': 'ι', '\\kappa': 'κ',
        '\\lambda': 'λ', '\\mu': 'μ', '\\nu': 'ν', '\\xi': 'ξ',
        '\\pi': 'π', '\\rho': 'ρ', '\\sigma': 'σ', '\\tau': 'τ',
        '\\phi': 'φ', '\\varphi': 'φ', '\\chi': 'χ', '\\psi': 'ψ', '\\omega': 'ω',
        '\\Gamma': 'Γ', '\\Delta': 'Δ', '\\Theta': 'Θ', '\\Lambda': 'Λ',
        '\\Xi': 'Ξ', '\\Pi': 'Π', '\\Sigma': 'Σ', '\\Phi': 'Φ', '\\Psi': 'Ψ', '\\Omega': 'Ω'
    };
    for (const [k, v] of Object.entries(greek)) {
        text = text.replaceAll(k, v);
    }

    // 8. Operators & symbols
    const ops = {
        '\\cdot': '·', '\\times': '×', '\\div': '÷', '\\pm': '±', '\\mp': '∓',
        '\\le': '≤', '\\leq': '≤', '\\ge': '≥', '\\geq': '≥', '\\ne': '≠', '\\neq': '≠',
        '\\approx': '≈', '\\equiv': '≡', '\\to': '→', '\\rightarrow': '→', '\\implies': '→',
        '\\leftarrow': '←', '\\leftrightarrow': '↔', '\\iff': '↔', '\\infty': '∞',
        '\\partial': '∂', '\\nabla': '∇', '\\in': '∈', '\\notin': '∉', '\\subset': '⊂',
        '\\subseteq': '⊆', '\\forall': '∀', '\\exists': '∃', '\\circ': '°'
    };
    for (const [k, v] of Object.entries(ops)) {
        text = text.replaceAll(k, v);
    }

    // 9. Spacing
    text = text.replace(/\\[,;:!]/g, ' ').replace(/\\quad/g, '   ').replace(/\\qquad/g, '      ');

    // 10. Superscripts & Subscripts
    const superMap = { '0':'⁰','1':'¹','2':'²','3':'³','4':'⁴','5':'⁵','6':'⁶','7':'⁷','8':'⁸','9':'⁹','+':'⁺','-':'⁻','=':'⁼','(':'⁽',')':'⁾','n':'ⁿ','i':'ⁱ','x':'ˣ','y':'ʸ' };
    const subMap = { '0':'₀','1':'₁','2':'₂','3':'₃','4':'₄','5':'₅','6':'₆','7':'₇','8':'₈','9':'₉','+':'₊','-':'₋','=':'₌','(':'₍',')':'₎','a':'ₐ','e':'ₑ','i':'ᵢ','o':'ₒ','u':'ᵤ','x':'ₓ' };

    text = text.replace(/\^\{([0-9+\-nixy=()]+)\}/g, (_, m) => m.split('').map(c => superMap[c] || c).join(''));
    text = text.replace(/\^([0-9n])/g, (_, m) => superMap[m] || m);
    text = text.replace(/\^(-[0-9]+)/g, (_, m) => m.split('').map(c => superMap[c] || c).join(''));

    text = text.replace(/_\{([0-9+\-aeioux=()]+)\}/g, (_, m) => m.split('').map(c => subMap[c] || c).join(''));
    text = text.replace(/_\{([a-zA-Z0-9_]+)\}/g, '_$1');
    text = text.replace(/_([0-9])/g, (_, m) => subMap[m] || m);

    // 11. Chemistry notation
    text = text.replace(/\b(H|He|Li|Be|B|C|N|O|F|Ne|Na|Mg|Al|Si|P|S|Cl|Ar|K|Ca|Sc|Ti|V|Cr|Mn|Fe|Co|Ni|Cu|Zn|Ga|Ge|As|Se|Br|Kr|Rb|Sr|Ag|Cd|Sn|Sb|I|Xe|Cs|Ba|Pt|Au|Hg|Pb|Bi|U)([0-9]+)/g, (_, el, count) => {
        return el + count.split('').map(c => subMap[c] || c).join('');
    });

    // 12. Strip leftover backslashes
    text = text.replace(/\\([a-zA-Z]+)/g, '$1');

    return text.trim();
}

// -------------------------------------------------------------
// EXPRESS APP (REST Endpoints matching docs/TECHNICAL_DEEP_DIVE.md)
// -------------------------------------------------------------
const app = express();
app.use(cors({ origin: true }));
app.use(express.json({ limit: '15mb' }));

/**
 * Firebase Auth Verification Middleware
 */
const authenticateFirebaseUser = async (req, res, next) => {
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
        version: '3.0.0',
        timestamp: new Date().toISOString()
    });
});

/**
 * POST /api/ai/chat
 * Contextual Study Tutor with subject, topic, material context, recent mistakes, and math readability
 */
app.post('/ai/chat', async (req, res) => {
    try {
        const {
            message,
            subject = 'General',
            topic = '',
            materialSummary = null,
            recentMistakes = [],
            history = []
        } = req.body;

        if (!message) {
            return res.status(400).json({ error: 'Missing message parameter' });
        }

        let contextSections = [];
        if (topic) {
            contextSections.push(`Active Topic: ${topic}`);
        }
        if (materialSummary) {
            contextSections.push(`Context from student's study material:\n${materialSummary}`);
        }
        if (recentMistakes && recentMistakes.length > 0) {
            contextSections.push(`Recent quiz mistakes to reinforce:\n${recentMistakes.join('\n')}`);
        }

        const systemPrompt = `You are Quovex AI Tutor, an elite academic coach for ${subject}.
Explain concepts step-by-step with clear intuition, formulas, and encouraging discipline.
${contextSections.join('\n\n')}

${MATH_READABILITY_RULES}`;

        let validHistory = (history || []).filter(h => h && (h.role === 'user' || h.role === 'assistant'));
        while (validHistory.length > 0 && validHistory[0].role === 'assistant') {
            validHistory.shift();
        }

        const messages = [
            { role: 'system', content: systemPrompt },
            ...validHistory.slice(-8),
            { role: 'user', content: message }
        ];

        const result = await callAiWithFailover({ messages, temperature: 0.4 });
        res.json({
            success: true,
            response: cleanMathAndLatex(result.content),
            provider: result.provider,
            model: result.model
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/ai/classify
 * Analyzes OCR text or note content to infer Subject, Topic, Subtopic, Exam Relevance, and Confidence
 */
app.post('/ai/classify', async (req, res) => {
    try {
        const { textSample = '', filename = '' } = req.body;
        if (!textSample && !filename) {
            return res.status(400).json({ error: 'Missing textSample or filename' });
        }

        const prompt = `Analyze this study snippet/title and infer its academic classification.
Title/Filename: ${filename}
Text Sample:
${textSample.slice(0, 3000)}

Return ONLY valid JSON format:
{
  "subject": "Physics | Chemistry | Mathematics | Biology | History | Computer Science | Economics | General",
  "topic": "Specific main topic name (e.g. Newton's Laws of Motion)",
  "subtopic": "Specific subtopic (e.g. Friction and Incline Planes)",
  "examRelevance": ["JEE", "NEET", "CBSE", "SAT", "AP", "GCSE"],
  "confidence": 0.95
}`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an academic content classifier. Respond ONLY with valid JSON.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.1
        });

        let parsed = extractJson(result.content);
        if (!parsed) {
            parsed = {
                subject: "General",
                topic: "Study Material",
                subtopic: "",
                examRelevance: ["General"],
                confidence: 0.5
            };
        }

        res.json({
            success: true,
            subject: parsed.subject || "General",
            topic: parsed.topic || "Study Material",
            subtopic: parsed.subtopic || "",
            examRelevance: parsed.examRelevance || [],
            confidence: parsed.confidence || 0.8,
            provider: result.provider
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/ai/summarize
 * Transforms OCR/PDF/Web text into Structured Learning Material (Summary, Key Points, Flashcards, Formulas)
 */
app.post('/ai/summarize', async (req, res) => {
    try {
        const { text, subject = 'General' } = req.body;
        if (!text) {
            return res.status(400).json({ error: 'Missing text parameter' });
        }

        const prompt = `Analyze this study text on "${subject}" and generate structured revision notes.
${MATH_READABILITY_RULES}

Return ONLY valid JSON format:
{
  "summary": "Clear, comprehensive conceptual overview (3-5 paragraphs with markdown formatting)",
  "keyPoints": [
    "Key concept or principle 1",
    "Key concept or principle 2",
    "Key concept or principle 3",
    "Key concept or principle 4"
  ],
  "flashcards": [
    { "question": "Question 1", "answer": "Answer 1", "formula": "Optional Formula" },
    { "question": "Question 2", "answer": "Answer 2", "formula": "Optional Formula" }
  ]
}

Text:
${text.slice(0, 8000)}`;

        const result = await callAiWithFailover({
            messages: [
                { role: 'system', content: 'You are an elite academic transformation engine. Respond ONLY with valid JSON.' },
                { role: 'user', content: prompt }
            ],
            temperature: 0.2
        });

        let parsed = extractJson(result.content);
        if (!parsed) {
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
 * POST /api/ai/quiz/generate
 * Generates 5 high-yield MCQs with plausible distractors, explanations, and mapped concepts
 */
app.post('/ai/quiz/generate', async (req, res) => {
    try {
        const { subject = 'Physics', topic = 'Mechanics', difficulty = 'Medium', keyPoints = [] } = req.body;

        const prompt = `Generate 5 high-yield multiple-choice questions for ${subject} on "${topic}" (Difficulty: ${difficulty}).
${keyPoints.length > 0 ? `Key Points from Material:\n${keyPoints.join('\n')}` : ''}
${MATH_READABILITY_RULES}

Return ONLY valid JSON format:
{
  "questions": [
    {
      "id": 1,
      "question": "Clear question text",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "Step-by-step explanation of why the correct option is right and others are wrong",
      "relatedConcept": "Specific concept name for remedial tracking"
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

        let parsed = extractJson(result.content);
        if (!parsed || !Array.isArray(parsed.questions)) {
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
 * Visual problem solver: receives real image (base64 or URL) and solves step-by-step
 */
app.post('/ai/doubt/image', async (req, res) => {
    try {
        const { imageUrl = '', base64Image = '', subject = 'General', questionText = '' } = req.body;

        const effectiveText = questionText.trim() || 'Please solve this problem step-by-step, state all formulas, and highlight the final answer.';
        const targetImageUri = imageUrl || (base64Image ? `data:image/jpeg;base64,${base64Image}` : '');

        const promptText = `Subject: ${subject}
Student Doubt / Question:
${effectiveText}

Break down the problem step-by-step, state applicable physical laws/theorems, show all formulas, and calculate the final solution cleanly formatted in Markdown.
${MATH_READABILITY_RULES}`;

        let userMessageContent;
        if (targetImageUri) {
            userMessageContent = [
                { type: 'text', text: promptText },
                { type: 'image_url', image_url: { url: targetImageUri } }
            ];
        } else {
            userMessageContent = promptText;
        }

        const messages = [
            {
                role: 'system',
                content: `You are Quovex AI, an elite STEM tutor and visual problem solver.
If the image contains a solvable academic problem, math equation, physics diagram, chemistry formula, or study question:
1. Identify the problem and state given values clearly.
2. State applicable laws, theorems, and formulas.
3. Provide rigorous step-by-step mathematical/conceptual derivations.
4. Highlight the final calculated answer clearly with standard units.
5. List 1-2 common student pitfalls or exam tips.

If the image does NOT contain an academic problem (for example, if it is a photo of a pet, person, landscape, random object, or is completely unreadable/blurry):
Politely state: "No solvable academic problem was detected in this photo. Please capture a clear, focused photo of a textbook question, handwritten problem, or formula sheet."`
            },
            { role: 'user', content: userMessageContent }
        ];

        const result = await callAiWithFailover({
            messages: messages,
            temperature: 0.2,
            isVision: Boolean(targetImageUri)
        });

        res.json({
            success: true,
            solution: cleanMathAndLatex(result.content),
            provider: result.provider,
            model: result.model
        });
    } catch (error) {
        console.error("Image doubt error:", error.message);
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/notes/extract-youtube
 * Extracts YouTube transcript and returns AI structured summary
 */
app.post('/notes/extract-youtube', async (req, res) => {
    try {
        const { url, subject = 'General' } = req.body;
        if (!url) return res.status(400).json({ error: 'Missing url parameter' });

        // Extract video ID
        const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:embed\/|v\/|watch\?v=|watch\?.+&v=))([\w-]{11})/);
        const videoId = match ? match[1] : null;

        if (!videoId) {
            return res.status(400).json({ error: 'Invalid YouTube URL' });
        }

        // Fetch YouTube page to get caption track or video title
        let videoTitle = `YouTube Video (${videoId})`;
        let transcriptText = '';

        try {
            const pageResponse = await axios.get(`https://www.youtube.com/watch?v=${videoId}`, {
                headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)' },
                timeout: 10000
            });
            const titleMatch = pageResponse.data.match(/<title>(.*?)<\/title>/);
            if (titleMatch && titleMatch[1]) {
                videoTitle = titleMatch[1].replace(' - YouTube', '').trim();
            }

            // Extract captions URL from player response
            const captionsMatch = pageResponse.data.match(/"captionTracks":\[(.*?)\]/);
            if (captionsMatch && captionsMatch[1]) {
                const tracks = JSON.parse(`[${captionsMatch[1]}]`);
                const englishTrack = tracks.find(t => t.languageCode === 'en' || t.languageCode === 'hi') || tracks[0];
                if (englishTrack && englishTrack.baseUrl) {
                    const captionResponse = await axios.get(englishTrack.baseUrl, { timeout: 8000 });
                    transcriptText = captionResponse.data
                        .replace(/<text[^>]*>/g, ' ')
                        .replace(/<\/text>/g, ' ')
                        .replace(/&amp;/g, '&')
                        .replace(/&quot;/g, '"')
                        .replace(/&#39;/g, "'")
                        .replace(/<[^>]+>/g, '')
                        .replace(/\s+/g, ' ')
                        .trim();
                }
            }
        } catch (scrapeError) {
            console.warn('YouTube caption scrape fallback:', scrapeError.message);
        }

        if (!transcriptText) {
            transcriptText = `Video Title: ${videoTitle}. Video URL: https://www.youtube.com/watch?v=${videoId}.`;
        }

        res.json({
            success: true,
            title: videoTitle,
            text: transcriptText.slice(0, 10000),
            wordCount: transcriptText.split(' ').length
        });
    } catch (error) {
        res.status(500).json({ error: 'Failed to extract YouTube content: ' + error.message });
    }
});

/**
 * POST /api/notes/extract-url
 */
app.post('/notes/extract-url', async (req, res) => {
    try {
        const { url } = req.body;
        if (!url) return res.status(400).json({ error: 'Missing url parameter' });

        try {
            const response = await axios.get(url, {
                headers: { 'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) QuovexBot/3.0' },
                timeout: 12000
            });

            const textOnly = response.data
                .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
                .replace(/<style\b[^<]*(?:(?!<\/style>)<[^<]*)*<\/style>/gi, '')
                .replace(/<[^>]+>/g, ' ')
                .replace(/\s+/g, ' ')
                .trim();

            if (!textOnly || textOnly.length < 50) {
                return res.status(422).json({ error: 'No readable text content found at this URL.' });
            }

            res.json({
                success: true,
                title: url,
                text: textOnly.slice(0, 10000),
                wordCount: textOnly.split(' ').length
            });
        } catch (fetchError) {
            return res.status(400).json({ error: 'Could not access URL. Please check the address.' });
        }
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

/**
 * POST /api/notes/extract-pdf
 */
app.post('/notes/extract-pdf', authenticateFirebaseUser, async (req, res) => {
    try {
        const { storageRef, noteId, subject = 'General' } = req.body;
        if (!storageRef) {
            return res.status(400).json({ success: false, error: 'Missing storageRef parameter' });
        }

        const bucket = admin.storage().bucket();
        const file = bucket.file(storageRef);

        let pdfBuffer;
        try {
            const [contents] = await file.download();
            pdfBuffer = contents;
        } catch (storageError) {
            return res.status(404).json({ success: false, error: 'PDF file not found in Storage.' });
        }

        let extractedText = '';
        try {
            const pdfParse = require('pdf-parse');
            const pdfData = await pdfParse(pdfBuffer);
            extractedText = pdfData.text || '';
        } catch (parseError) {
            return res.status(422).json({ success: false, error: 'Could not extract text from PDF. The file may be image-only. Please use Document Scanner instead.' });
        }

        if (!extractedText.trim()) {
            return res.status(422).json({ success: false, error: 'No readable text found in PDF.' });
        }

        const textChunk = extractedText.replace(/\s+/g, ' ').trim().slice(0, 6000);

        const prompt = `Analyze this study text on "${subject}" and generate structured revision notes.
${MATH_READABILITY_RULES}
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
        res.status(500).json({ success: false, error: 'PDF processing failed: ' + error.message });
    }
});

/**
 * POST /api/ai/plan/generate
 */
app.post('/ai/plan/generate', async (req, res) => {
    try {
        const { examName = 'JEE Advanced', targetHours = 4, subjects = ['Physics', 'Chemistry', 'Maths'], days = 30 } = req.body;

        const prompt = `Create a high-yield ${days}-day study roadmap for ${examName}.
Daily study target: ${targetHours} hours/day.
Subjects: ${subjects.join(', ')}.
Break down week-by-week with key milestones, problem solving targets, and spaced repetition review days. Use Markdown tables.
${MATH_READABILITY_RULES}`;

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
 * GET /api/ncert/catalog
 * Serves remote NCERT metadata-only catalog for Classes 9-12
 */
app.get('/ncert/catalog', async (req, res) => {
    try {
        const fs = require('fs');
        const path = require('path');
        const localPath = path.join(__dirname, 'ncert_catalog_v1.json');
        const assetPath = path.join(__dirname, '../../android/app/src/main/assets/ncert/ncert_catalog_v1.json');
        
        let targetPath = null;
        if (fs.existsSync(localPath)) {
            targetPath = localPath;
        } else if (fs.existsSync(assetPath)) {
            targetPath = assetPath;
        }

        if (targetPath) {
            const data = JSON.parse(fs.readFileSync(targetPath, 'utf8'));
            return res.json(data);
        }
        res.json({
            version: 1,
            lastUpdated: "2026-08-20",
            curriculum: "CBSE / NCERT Rationalised Edition",
            publisher: "NCERT",
            books: [],
            chapters: []
        });
    } catch (e) {
        res.status(500).json({ error: e.message });
    }
});

/**
 * GET /ncert/pdf and /api/ncert/pdf
 * Proxy stream for NCERT PDFs to handle client network/TLS reset issues
 */
const handlePdfProxy = async (req, res) => {
    try {
        let targetUrl = req.query.url;
        if (!targetUrl || !targetUrl.includes('ncert.nic.in')) {
            return res.status(400).json({ error: 'Invalid or missing NCERT PDF URL' });
        }
        if (!targetUrl.startsWith('https://') && !targetUrl.startsWith('http://')) {
            targetUrl = 'https://' + targetUrl;
        }

        // Always use https for NCERT PDF download with custom TLS agent
        const httpsUrl = targetUrl.replace('http://', 'https://');
        const https = require('https');
        const agent = new https.Agent({
            rejectUnauthorized: false
        });

        https.get(httpsUrl, {
            agent,
            headers: {
                'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
                'Accept': '*/*'
            }
        }, (proxyRes) => {
            if (proxyRes.statusCode === 301 || proxyRes.statusCode === 302) {
                const redirectUrl = proxyRes.headers.location;
                if (redirectUrl) {
                    return https.get(redirectUrl, { agent, headers: { 'User-Agent': 'Mozilla/5.0', 'Accept': '*/*' } }, (rRes) => {
                        res.setHeader('Content-Type', 'application/pdf');
                        if (rRes.headers['content-length']) res.setHeader('Content-Length', rRes.headers['content-length']);
                        rRes.pipe(res);
                    });
                }
            }

            if (proxyRes.statusCode !== 200) {
                return res.status(proxyRes.statusCode || 502).json({
                    error: `NCERT server returned status ${proxyRes.statusCode}`
                });
            }
            res.setHeader('Content-Type', 'application/pdf');
            if (proxyRes.headers['content-length']) {
                res.setHeader('Content-Length', proxyRes.headers['content-length']);
            }
            proxyRes.pipe(res);
        }).on('error', (err) => {
            console.error('PDF proxy error:', err.message);
            res.status(502).json({ error: 'Failed to fetch PDF from NCERT portal: ' + err.message });
        });
    } catch (e) {
        console.error('PDF proxy exception:', e.message);
        res.status(500).json({ error: e.message });
    }
};

app.get('/ncert/pdf', handlePdfProxy);
app.get('/api/ncert/pdf', handlePdfProxy);

// Export Express App as Cloud Function
exports.api = functions.https.onRequest(app);

// Standalone local execution support for emulator/dev testing
if (require.main === module) {
    const PORT = process.env.PORT || 5001;
    app.listen(PORT, '0.0.0.0', () => {
        console.log(`🚀 Quovex Backend API Server running locally on http://0.0.0.0:${PORT}`);
    });
}

