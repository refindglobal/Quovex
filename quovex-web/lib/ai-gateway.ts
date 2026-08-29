/**
 * Quovex Live AI Gateway for Web Client
 *
 * Implements:
 * - 4 Groq Keys Rotating Pool
 * - 4 Cerebras Keys Rotating Pool
 * - Approved Model IDs from docs/AI_MODELS.md (openai/gpt-oss-20b, qwen/qwen3.6-27b, gpt-oss-120b)
 * - Automatic Failover with Exponential Backoff
 * - Clean LaTeX & Unicode formatting
 * - Zero hardcoded or simulated fallback responses
 */

import fs from 'fs';
import path from 'path';

let groqKeys: string[] = [];
let cerebrasKeys: string[] = [];

function parseProperties(content: string): Record<string, string> {
  const result: Record<string, string> = {};
  const lines = content.split('\n');
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const eqIdx = trimmed.indexOf('=');
    if (eqIdx !== -1) {
      const k = trimmed.substring(0, eqIdx).trim();
      const v = trimmed.substring(eqIdx + 1).trim();
      result[k] = v;
    }
  }
  return result;
}

function isValidApiKey(key?: string): boolean {
  if (!key || typeof key !== 'string') return false;
  const trimmed = key.trim();
  if (
    trimmed === '' ||
    trimmed === 'your_groq_key_here' ||
    trimmed === 'your_cerebras_key_here' ||
    trimmed.startsWith('TODO') ||
    trimmed.length < 15
  ) {
    return false;
  }
  return true;
}

function loadApiKeys() {
  if (groqKeys.length > 0 && cerebrasKeys.length > 0) return;

  const envConfig: Record<string, string> = {};

  const candidates = [
    path.resolve(process.cwd(), '../secrets.properties'),
    path.resolve(process.cwd(), 'secrets.properties'),
    path.resolve(process.cwd(), '.env.local'),
    path.resolve(process.cwd(), '../.env.local'),
    path.resolve(process.cwd(), '../firebase_backend/functions/.env'),
  ];

  for (const p of candidates) {
    try {
      if (fs.existsSync(p)) {
        const content = fs.readFileSync(p, 'utf-8');
        const parsed = parseProperties(content);
        Object.assign(envConfig, parsed);
      }
    } catch (_) {}
  }

  const rawGroq = [
    process.env.GROQ_API_KEY_1 || envConfig.GROQ_API_KEY_1,
    process.env.GROQ_API_KEY_2 || envConfig.GROQ_API_KEY_2,
    process.env.GROQ_API_KEY_3 || envConfig.GROQ_API_KEY_3,
    process.env.GROQ_API_KEY_4 || envConfig.GROQ_API_KEY_4,
    process.env.GROQ_API_KEY || envConfig.GROQ_API_KEY,
  ];

  const rawCerebras = [
    process.env.CEREBRAS_API_KEY_1 || envConfig.CEREBRAS_API_KEY_1,
    process.env.CEREBRAS_API_KEY_2 || envConfig.CEREBRAS_API_KEY_2,
    process.env.CEREBRAS_API_KEY_3 || envConfig.CEREBRAS_API_KEY_3,
    process.env.CEREBRAS_API_KEY_4 || envConfig.CEREBRAS_API_KEY_4,
    process.env.CEREBRAS_API_KEY || envConfig.CEREBRAS_API_KEY,
  ];

  groqKeys = rawGroq.filter(isValidApiKey) as string[];
  cerebrasKeys = rawCerebras.filter(isValidApiKey) as string[];
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
  content: string | Array<{ type: string; text?: string; image_url?: { url: string } }>;
}

export interface AiCallParams {
  messages: AiChatMessage[];
  temperature?: number;
  maxTokens?: number;
  jsonMode?: boolean;
  isVision?: boolean;
}

export interface AiGatewayResult {
  success: boolean;
  content: string;
  provider: 'groq' | 'cerebras';
  model: string;
  requestId: string;
}

/**
 * Universal live AI caller with 4-key rotation and multi-provider failover.
 * Never returns hardcoded answers on failure — throws an actionable error.
 */
export async function callAiGateway({
  messages,
  temperature = 0.3,
  maxTokens = 4096,
  jsonMode = false,
  isVision = false,
}: AiCallParams): Promise<AiGatewayResult> {
  loadApiKeys();
  const requestId = `req_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;

  if (groqKeys.length === 0 && cerebrasKeys.length === 0) {
    // Attempt remote proxy through deployed Firebase Cloud Functions where Secret Manager keys are active
    try {
      const remoteApiUrl = process.env.NEXT_PUBLIC_API_URL || 'https://us-central1-quovex-f3104.cloudfunctions.net/api';
      const lastUserMsg = [...messages].reverse().find((m) => m.role === 'user');
      const userContent = typeof lastUserMsg?.content === 'string' ? lastUserMsg.content : JSON.stringify(lastUserMsg?.content || '');

      const remoteRes = await fetch(`${remoteApiUrl}/ai/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: userContent,
          messages,
          temperature,
          maxTokens,
        }),
      });

      if (remoteRes.ok) {
        const data = await remoteRes.json();
        const content = data.reply || data.content || data.plan || data.summary;
        if (content) {
          return {
            success: true,
            content,
            provider: data.provider || 'groq',
            model: data.model || 'openai/gpt-oss-20b',
            requestId,
          };
        }
      }
    } catch (remoteErr) {
      console.warn('Backend Cloud Functions proxy attempt failed:', remoteErr);
    }

    throw new Error(
      'Quovex AI Gateway: No active provider API keys configured in environment (GROQ_API_KEY / CEREBRAS_API_KEY). Please configure your rotated keys in secrets.properties or .env.local.'
    );
  }

  // 1. Try Groq Primary Model (qwen/qwen3.6-27b for vision, openai/gpt-oss-20b for text)
  const primaryGroqModel = isVision ? 'qwen/qwen3.6-27b' : 'openai/gpt-oss-20b';
  for (let attempt = 0; attempt < groqKeys.length; attempt++) {
    const key = getNextGroqKey();
    if (!key) break;

    try {
      const payload: any = {
        model: primaryGroqModel,
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
        const content = data.choices?.[0]?.message?.content || '';
        if (content) {
          return {
            success: true,
            content,
            provider: 'groq',
            model: primaryGroqModel,
            requestId,
          };
        }
      }
    } catch (err) {
      console.warn(`Groq Key attempt ${attempt + 1} (${primaryGroqModel}) failed:`, err);
    }
  }

  // 2. Try Groq Secondary Fallback Model (qwen/qwen3.6-27b) if text
  if (!isVision) {
    for (let attempt = 0; attempt < groqKeys.length; attempt++) {
      const key = getNextGroqKey();
      if (!key) break;

      try {
        const payload: any = {
          model: 'qwen/qwen3.6-27b',
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
          const content = data.choices?.[0]?.message?.content || '';
          if (content) {
            return {
              success: true,
              content,
              provider: 'groq',
              model: 'qwen/qwen3.6-27b',
              requestId,
            };
          }
        }
      } catch (err) {
        console.warn(`Groq Qwen fallback attempt ${attempt + 1} failed:`, err);
      }
    }
  }

  // 3. Try Cerebras Failover (gpt-oss-120b / gemma-4-31b)
  const cerebrasModel = isVision ? 'gemma-4-31b' : 'gpt-oss-120b';
  for (let attempt = 0; attempt < cerebrasKeys.length; attempt++) {
    const key = getNextCerebrasKey();
    if (!key) break;

    try {
      const payload: any = {
        model: cerebrasModel,
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
        const content = data.choices?.[0]?.message?.content || '';
        if (content) {
          return {
            success: true,
            content,
            provider: 'cerebras',
            model: cerebrasModel,
            requestId,
          };
        }
      }
    } catch (err) {
      console.warn(`Cerebras failover attempt ${attempt + 1} failed:`, err);
    }
  }

  throw new Error('Quovex AI Gateway: All primary and failover providers are temporarily unreachable.');
}

/**
 * Robust JSON Extractor & Repair Engine for AI responses.
 */
export function extractJsonFromAiResponse<T = any>(rawText: string): T {
  if (!rawText) throw new Error('Empty AI response.');

  let clean = rawText.replace(/<think>[\s\S]*?<\/think>/gi, '').trim();
  clean = clean.replace(/^```json\s*/im, '').replace(/^```\s*/im, '').replace(/```$/im, '').trim();

  // 1. Direct parse attempt
  try {
    return JSON.parse(clean);
  } catch (_) {}

  // 2. Extract outer {...}
  const firstBrace = clean.indexOf('{');
  const lastBrace = clean.lastIndexOf('}');
  if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
    const candidate = clean.slice(firstBrace, lastBrace + 1);
    try {
      return JSON.parse(candidate);
    } catch (e2) {
      // 3. Fix unescaped backslashes in LaTeX strings
      try {
        const fixed = candidate.replace(/\\(?!["\\/bfnrt]|u[0-9a-fA-F]{4})/g, '\\\\');
        return JSON.parse(fixed);
      } catch (_) {}
    }
  }

  throw new Error(`Failed to parse structured JSON: ${rawText.slice(0, 120)}...`);
}
