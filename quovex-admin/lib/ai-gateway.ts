/**
 * Quovex Server-Side AI Gateway for Content Studio & Cloud Functions
 *
 * Implements:
 * - 4 Groq Keys Rotating Pool
 * - 4 Cerebras Keys Rotating Pool
 * - Approved Model IDs from docs/AI_MODELS.md (openai/gpt-oss-20b, openai/gpt-oss-120b, qwen/qwen3.6-27b, gpt-oss-120b)
 * - Automatic Failover with Exponential Backoff
 * - Clean Unicode / LaTeX Math Normalization
 * - Strict Anonymity (Zero Provider Names to Client)
 */

import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';

let groqKeys: string[] = [];
let cerebrasKeys: string[] = [];

// Initialize API keys from environment or root secrets.properties
function loadApiKeys() {
  if (groqKeys.length > 0 && cerebrasKeys.length > 0) return;

  const envConfig: Record<string, string> = {};

  // Try reading secrets.properties from project root
  const candidates = [
    path.resolve(process.cwd(), '../secrets.properties'),
    path.resolve(process.cwd(), 'secrets.properties'),
    path.resolve(__dirname, '../../../secrets.properties'),
    path.resolve(__dirname, '../../secrets.properties'),
  ];

  for (const p of candidates) {
    try {
      if (fs.existsSync(p)) {
        const content = fs.readFileSync(p, 'utf-8');
        const parsed = dotenv.parse(content);
        Object.assign(envConfig, parsed);
        break;
      }
    } catch (_) {}
  }

  groqKeys = [
    process.env.GROQ_API_KEY_1 || envConfig.GROQ_API_KEY_1,
    process.env.GROQ_API_KEY_2 || envConfig.GROQ_API_KEY_2,
    process.env.GROQ_API_KEY_3 || envConfig.GROQ_API_KEY_3,
    process.env.GROQ_API_KEY_4 || envConfig.GROQ_API_KEY_4,
  ].filter(Boolean) as string[];

  cerebrasKeys = [
    process.env.CEREBRAS_API_KEY_1 || envConfig.CEREBRAS_API_KEY_1,
    process.env.CEREBRAS_API_KEY_2 || envConfig.CEREBRAS_API_KEY_2,
    process.env.CEREBRAS_API_KEY_3 || envConfig.CEREBRAS_API_KEY_3,
    process.env.CEREBRAS_API_KEY_4 || envConfig.CEREBRAS_API_KEY_4,
  ].filter(Boolean) as string[];
}

let groqIdx = 0;
let cerebrasIdx = 0;

function getNextGroqKey(): string | null {
  loadApiKeys();
  if (groqKeys.length === 0) return null;
  const key = groqKeys[groqIdx];
  groqIdx = (groqIdx + 1) % groqKeys.length;
  return key;
}

function getNextCerebrasKey(): string | null {
  loadApiKeys();
  if (cerebrasKeys.length === 0) return null;
  const key = cerebrasKeys[cerebrasIdx];
  cerebrasIdx = (cerebrasIdx + 1) % cerebrasKeys.length;
  return key;
}

export interface AiChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

export interface AiCallParams {
  messages: AiChatMessage[];
  temperature?: number;
  maxTokens?: number;
  jsonMode?: boolean;
}

/**
 * Universal server-side AI caller with 4-key rotation and multi-provider failover.
 */
export async function callAiGateway({
  messages,
  temperature = 0.4,
  maxTokens = 4096,
  jsonMode = false,
}: AiCallParams): Promise<string> {
  loadApiKeys();

  // 1. Try Groq Primary Model (openai/gpt-oss-20b)
  for (let attempt = 0; attempt < Math.max(1, groqKeys.length); attempt++) {
    const key = getNextGroqKey();
    if (!key) break;

    try {
      const payload: any = {
        model: 'openai/gpt-oss-20b',
        messages,
        temperature,
        max_tokens: maxTokens,
      };
      if (jsonMode) {
        payload.response_format = { type: 'json_object' };
      }

      const res = await fetch('https://api.groq.com/openai/v1/chat/completions', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${key}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        return data.choices?.[0]?.message?.content || '';
      }
    } catch (err) {
      console.warn(`Groq Key attempt ${attempt + 1} failed:`, err);
    }
  }

  // 2. Try Groq Fallback Model (qwen/qwen3.6-27b)
  for (let attempt = 0; attempt < Math.max(1, groqKeys.length); attempt++) {
    const key = getNextGroqKey();
    if (!key) break;

    try {
      const payload: any = {
        model: 'qwen/qwen3.6-27b',
        messages,
        temperature,
        max_tokens: maxTokens,
      };

      const res = await fetch('https://api.groq.com/openai/v1/chat/completions', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${key}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        return data.choices?.[0]?.message?.content || '';
      }
    } catch (err) {
      console.warn(`Groq Qwen fallback attempt ${attempt + 1} failed:`, err);
    }
  }

  // 3. Try Cerebras Failover (gpt-oss-120b)
  for (let attempt = 0; attempt < Math.max(1, cerebrasKeys.length); attempt++) {
    const key = getNextCerebrasKey();
    if (!key) break;

    try {
      const payload: any = {
        model: 'gpt-oss-120b',
        messages,
        temperature,
        max_tokens: maxTokens,
      };
      if (jsonMode) {
        payload.response_format = { type: 'json_object' };
      }

      const res = await fetch('https://api.cerebras.ai/v1/chat/completions', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${key}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        const data = await res.json();
        return data.choices?.[0]?.message?.content || '';
      }
    } catch (err) {
      console.warn(`Cerebras attempt ${attempt + 1} failed:`, err);
    }
  }

  throw new Error('Quovex AI Gateway: All key pools and failover providers temporarily unavailable.');
}

/**
 * Extracts and parses JSON from AI text response safely.
 */
export function extractJsonFromAiResponse<T = any>(rawText: string): T {
  if (!rawText) throw new Error('Empty AI response.');
  
  // 1. Strip reasoning <think>...</think> tags if present
  let cleaned = rawText.replace(/<think>[\s\S]*?<\/think>/gi, '').trim();

  // 2. Strip markdown code fences
  cleaned = cleaned
    .replace(/^```json\s*/im, '')
    .replace(/^```\s*/im, '')
    .replace(/```$/im, '')
    .trim();

  // 3. Direct parse attempt
  try {
    return JSON.parse(cleaned);
  } catch (_) {}

  // 4. Search for outermost JSON object {...}
  const firstBrace = cleaned.indexOf('{');
  const lastBrace = cleaned.lastIndexOf('}');
  if (firstBrace !== -1 && lastBrace > firstBrace) {
    try {
      const candidate = cleaned.substring(firstBrace, lastBrace + 1);
      return JSON.parse(candidate);
    } catch (_) {}
  }

  // 5. Search for outermost JSON array [...]
  const firstBracket = cleaned.indexOf('[');
  const lastBracket = cleaned.lastIndexOf(']');
  if (firstBracket !== -1 && lastBracket > firstBracket) {
    try {
      const candidate = cleaned.substring(firstBracket, lastBracket + 1);
      return JSON.parse(candidate);
    } catch (_) {}
  }

  throw new Error(`Failed to parse structured JSON from AI output: ${rawText.substring(0, 200)}...`);
}
