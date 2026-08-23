# Quovex — AI Models Reference

**Version:** 2.0 | **Last verified:** 2026-08-21 (live API call using actual keys)

---

## ✅ Groq — Available Models (Live Verified)

| Model ID | Best For | Notes |
|---|---|---|
| `openai/gpt-oss-120b` | Vision (Image Doubt Solver), complex reasoning | **Largest, most capable. Supports images.** |
| `openai/gpt-oss-20b` | Fast chat, note summarization, quiz gen, flashcard gen, quotes | Faster + cheaper than 120b |
| `qwen/qwen3.6-27b` | Balanced chat + reasoning, Groq fallback | Good multilingual support |
| `groq/compound` | Agentic tasks, tool use, AI level assessment | Best for complex multi-step reasoning |
| `groq/compound-mini` | Quick agentic tasks | Faster compound model |
| `allam-2-7b` | Arabic language support | Only if targeting Arabic users |
| `whisper-large-v3` | Audio transcription | Future: voice input feature |
| `whisper-large-v3-turbo` | Fast audio transcription | Faster Whisper |
| `meta-llama/llama-prompt-guard-2-86m` | Content safety filtering | Moderation use |
| `meta-llama/llama-prompt-guard-2-22m` | Fast content safety | Smaller safety model |

> ⚠️ `llama-3.3-70b-versatile`, `llama-3.1-8b-instant`, `llama-3.2-11b-vision-preview` — **DEPRECATED as of Aug 2026**

---

## ✅ Cerebras — Available Models (Live Verified)

| Model ID | Best For | Notes |
|---|---|---|
| `gpt-oss-120b` | Study plan generation, long reasoning, vision | **128K context — use for study plans** |
| `gemma-4-31b` | Vision (Image Doubt fallback), document understanding | Best vision model on Cerebras |

---

## 🎯 Quovex Model Assignment

| Feature | Provider | Model | Routing |
|---|---|---|---|
| **AI Chat** | Groq | `openai/gpt-oss-20b` | Primary |
| **AI Chat (Groq fallback)** | Groq | `qwen/qwen3.6-27b` | If gpt-oss-20b hits limit |
| **AI Chat (Cerebras failover)** | Cerebras | `gpt-oss-120b` | If all Groq keys exhausted |
| **Learning Material Classification** | Groq | `openai/gpt-oss-20b` | Infer subject + topic from content |
| **Note Summarization** | Groq | `openai/gpt-oss-20b` | Summary + key points + formulas |
| **Flashcard Generation** | Groq | `openai/gpt-oss-20b` | JSON schema mode — fast, structured |
| **Quiz Generation** | Groq | `openai/gpt-oss-20b` | JSON schema mode — MCQ generation |
| **Study Plan Generation** | Cerebras | `gpt-oss-120b` | 128K context for full plans |
| **Study Plan Replan** | Cerebras | `gpt-oss-120b` | Same — needs full context |
| **Image Doubt Solver** | Groq | `openai/gpt-oss-120b` | Vision support + most capable |
| **Image Doubt (fallback)** | Cerebras | `gemma-4-31b` | Also supports vision |
| **Motivational Quotes** | Groq | `openai/gpt-oss-20b` | Quick, simple generation |
| **AI Level Assessment Quiz** | Groq | `groq/compound` | Adaptive difficulty reasoning |
| **Content Safety** | Groq | `meta-llama/llama-prompt-guard-2-22m` | Fast moderation on all inputs |
| **Audio (future v2)** | Groq | `whisper-large-v3-turbo` | Voice notes transcription |

---

## 🔄 Key Rotation Strategy Per Feature

```
AI Chat requests:
  → Try Groq gpt-oss-20b (Key 1 → 2 → 3 → 4 round-robin)
  → On 429: cooldown 60 min, try next key
  → All gpt-oss-20b exhausted → Groq qwen3.6-27b
  → All Groq keys exhausted → Cerebras gpt-oss-120b

Image Doubt (vision — most expensive):
  → Groq gpt-oss-120b (dedicated pool, 4 keys)
  → On 429: Cerebras gemma-4-31b

Classification + Summarization + Flashcards + Quiz:
  → Groq gpt-oss-20b (round-robin)
  → On failure: next key in pool

Study Plan (Cerebras first — needs 128K context):
  → Cerebras gpt-oss-120b (Key 1 → 2 → 3 → 4)
  → On failure: Groq groq/compound (fallback, lower context)
```

---

## 📊 Free Tier Limits (Approximate)

| Provider | Free Tier Limit | With 4 Keys |
|---|---|---|
| Groq | ~14,400 req/day per key | ~57,600 req/day total |
| Cerebras | ~1,000 req/day per key (est.) | ~4,000 req/day total |

> These limits may change. Monitor via `/config/ai_key_usage` in Firestore and the Admin Panel AI Key Manager.

---

## 🔐 API Keys Location

Keys are stored **exclusively in Firebase Cloud Functions environment variables** — never in the Android APK, never in `secrets.properties` (which is used only for build-time references, not runtime AI keys).

```bash
# Cloud Functions environment (set via Firebase CLI)
firebase functions:secrets:set GROQ_API_KEY_1
firebase functions:secrets:set GROQ_API_KEY_2
firebase functions:secrets:set GROQ_API_KEY_3
firebase functions:secrets:set GROQ_API_KEY_4
firebase functions:secrets:set CEREBRAS_API_KEY_1
firebase functions:secrets:set CEREBRAS_API_KEY_2
firebase functions:secrets:set CEREBRAS_API_KEY_3
firebase functions:secrets:set CEREBRAS_API_KEY_4
```

> [!CAUTION]
> After sharing keys in any chat or document, **immediately regenerate them** from the Groq and Cerebras consoles. Treat exposed keys as compromised.

---

## ⚙️ AI System Prompt Design Philosophy

All Quovex AI prompts follow these principles. These are **behavior requirements**, not literal prompt text (internal prompt text is not published in documentation):

### For AI Chat (Contextual Study Tutor)
- **Teach**, not just answer — explain reasoning behind every response
- **Step-by-step** for any problem requiring derivation or calculation
- **Context-aware** — use student's subject, topic, and material if provided
- **Adaptive difficulty** — calibrate to student's known level
- **Acknowledge uncertainty** — never invent facts; say "I don't know" when appropriate
- **No hallucination** — do not claim to have read material not in context
- **Concise** — avoid verbose preambles; get to the answer
- **LaTeX for math** — `$$formula$$` for rendered equations
- **Follow-up prompts** — ask ONE clarifying question if the query is ambiguous
- **Connect to source** — relate answers to the student's current topic when possible

### For Summarization + Classification
- **Preserve structure** — headings, formulas, definitions from original material
- **Identify subject confidently** — provide confidence score
- **Extract formulas in LaTeX** — do not lose mathematical notation
- **Prioritize exam-relevant concepts** if exam context is known

### For Flashcard Generation
- **Anki-style** — atomic: one concept per card
- **Front = question / problem** — not just a term
- **Back = complete explanation** — not just a one-word answer
- **Include formula in LaTeX** when applicable
- **Tag accurately** — subject + topic + difficulty

### For Quiz Generation
- **Distractors must be plausible** — not obviously wrong
- **One correct answer only** — no ambiguous MCQs
- **Include explanation** for every correct answer
- **Map to relatedConcept** — enables remedial flashcard creation
- **Difficulty calibrated** to student level (1-5)

---

## 🏛️ Content Studio & Multi-Agent Model Assignment (`PLANNED`)

| Pipeline Role | Provider / Engine | Primary Model | Fallback Model | Purpose |
|---|---|---|---|---|
| **Demand Intelligence Analyzer** | Backend Aggregator | Algorithmic + `openai/gpt-oss-20b` | N/A | Groups student doubt friction & mistake clusters |
| **Evidence Pack Assembler** | Search / Scraper API | Sanitized Scraper + `openai/gpt-oss-20b` | `gpt-oss-120b` | Gathers verified facts, definitions, and real-world examples |
| **Reasoning Agent A (Architect)** | Groq | `openai/gpt-oss-120b` | `gpt-oss-120b` (Cerebras) | Proposes chapter structure, analogies, and difficulty curve |
| **Reasoning Agent B (Challenger)** | Cerebras | `gpt-oss-120b` | `openai/gpt-oss-120b` (Groq) | Challenges misconceptions, rigor, and pedagogical gaps |
| **Synthesis & Editorial Agent** | Groq | `groq/compound` | `openai/gpt-oss-20b` | Combines debate outcomes into a unified book blueprint |
| **Original Educational Writer** | Groq | `openai/gpt-oss-20b` | `qwen/qwen3.6-27b` | Authors fresh explanations, worked examples, and summaries |
| **Multi-Tier Validation Engine** | Cross-Model | `gemma-4-31b` / `groq/compound` | `openai/gpt-oss-20b` | Independent validation (Fact, Math, Curriculum, Pedagogy) |

---

## 🚫 GLOBAL BRAND IDENTITY & REDACTION RULES

> [!IMPORTANT]
> **Provider and model names are strictly INTERNAL IMPLEMENTATION DETAILS.**
> 
> Under NO circumstances should `Groq`, `Cerebras`, `OpenAI`, `Qwen`, `Llama`, `Gemma`, model IDs, API keys, or fallback routing details ever be exposed in:
> - Android UI (chips, headers, progress bars, dialogs)
> - AI responses or explanations
> - Error messages, retry notifications, or loaders
> - Internal debate logs or system prompts
> 
> The student-facing AI brand identity is ALWAYS: **`Quovex AI`**.

