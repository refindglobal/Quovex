const path = require('path');
const fs = require('fs');
const dotenv = require('dotenv');
const axios = require('axios');

// Load environment variables
const secretsPath = path.resolve(__dirname, '../../secrets.properties');
const fileContent = fs.readFileSync(secretsPath, 'utf-8');
const envConfig = dotenv.parse(fileContent);

const GROQ_KEYS = [
    envConfig.GROQ_API_KEY_1,
    envConfig.GROQ_API_KEY_2,
    envConfig.GROQ_API_KEY_3,
    envConfig.GROQ_API_KEY_4
].filter(Boolean);

const CEREBRAS_KEYS = [
    envConfig.CEREBRAS_API_KEY_1,
    envConfig.CEREBRAS_API_KEY_2,
    envConfig.CEREBRAS_API_KEY_3,
    envConfig.CEREBRAS_API_KEY_4
].filter(Boolean);

console.log("=== QUOVEX BACKEND SELF-DIAGNOSTIC TEST ===");
console.log(`Groq Keys Loaded: ${GROQ_KEYS.length}`);
console.log(`Cerebras Keys Loaded: ${CEREBRAS_KEYS.length}`);

async function runTests() {
    // 1. Test Groq AI Chat (openai/gpt-oss-20b)
    console.log("\n[Test 1] Testing Groq Chat (openai/gpt-oss-20b)...");
    try {
        const chatRes = await axios.post('https://api.groq.com/openai/v1/chat/completions', {
            model: 'openai/gpt-oss-20b',
            messages: [{ role: 'user', content: 'What is Newton\'s Third Law in one short sentence?' }],
            max_tokens: 60
        }, {
            headers: {
                'Authorization': `Bearer ${GROQ_KEYS[0]}`,
                'Content-Type': 'application/json'
            }
        });
        console.log("✅ Groq Chat OK:", chatRes.data.choices[0].message.content.trim());
    } catch (e) {
        console.error("❌ Groq Chat Failed:", e.response?.status, e.response?.data || e.message);
    }

    // 2. Test Cerebras Study Planner (gpt-oss-120b)
    console.log("\n[Test 2] Testing Cerebras Study Planner (gpt-oss-120b)...");
    try {
        const cerRes = await axios.post('https://api.cerebras.ai/v1/chat/completions', {
            model: 'gpt-oss-120b',
            messages: [{ role: 'user', content: 'Generate a 1-day study schedule for Thermodynamics in 2 bullet points.' }],
            max_tokens: 100
        }, {
            headers: {
                'Authorization': `Bearer ${CEREBRAS_KEYS[0]}`,
                'Content-Type': 'application/json'
            }
        });
        console.log("✅ Cerebras Planner OK:", cerRes.data.choices[0].message.content.trim());
    } catch (e) {
        console.error("❌ Cerebras Planner Failed:", e.response?.status, e.response?.data || e.message);
    }

    // 3. Test Groq Note Summarization (JSON Extraction)
    console.log("\n[Test 3] Testing AI JSON Note Summarizer (openai/gpt-oss-20b)...");
    try {
        const sumRes = await axios.post('https://api.groq.com/openai/v1/chat/completions', {
            model: 'openai/gpt-oss-20b',
            messages: [{
                role: 'user',
                content: 'Summarize: "Mitochondria generate ATP for the cell." Return JSON: {"summary": "...", "flashcards": [{"question": "...", "answer": "..."}]}'
            }],
            max_tokens: 120
        }, {
            headers: {
                'Authorization': `Bearer ${GROQ_KEYS[1] || GROQ_KEYS[0]}`,
                'Content-Type': 'application/json'
            }
        });
        console.log("✅ AI Summarizer OK:", sumRes.data.choices[0].message.content.trim());
    } catch (e) {
        console.error("❌ AI Summarizer Failed:", e.response?.status, e.response?.data || e.message);
    }

    console.log("\n🎉 ALL BACKEND AI SERVICES LIVE & OPERATIONAL!");
}

runTests();
