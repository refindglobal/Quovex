# Quovex — Technical Deep Dive
## How AI Features Work Internally

**Version:** 3.0 | **Date:** 2026-08-22

> [!IMPORTANT]
> This document describes the **intended product behavior** for the next implementation phase.
> Not all flows described here are fully implemented. Refer to phase implementation reports for current implementation status.

---

## 0. 🧭 Core Architecture Principle

```
Android (Room-first local) → Firebase Cloud Functions (AI Gateway) → AI Providers
                           ↓                                        (Groq / Cerebras)
                    Firebase (Firestore + Storage)
```

**Data flow rule:**
- All local reads/writes go to **Room DB first** (offline-first)
- All AI calls route through **Firebase Cloud Functions** (no client-side AI keys)
- Firestore sync is **best-effort background** — app is fully functional offline
- Firebase Storage holds binary data (PDFs, scanned images) — never stored in Room

---

## 1. 📚 Learning Material System — Full Pipeline

### 1.1 What is a Learning Material?

A Learning Material is the **fundamental unit of knowledge** in Quovex. It is NOT a note.

```
A Learning Material has:
  ├── source (PDF / scan / YouTube / web / quick text)
  ├── subject (inferred by AI, confirmed by student)
  ├── topic (inferred by AI)
  ├── title
  ├── summary (AI-generated)
  ├── keyPoints (AI-generated list)
  ├── formulas / definitions (extracted by AI)
  ├── flashcards (auto-generated deck linked to this material)
  └── quizQuestions (auto-generated, linked to this material)
```

### 1.2 The Learning Pipeline (End-to-End)

```
STUDENT INPUT
    ↓
INGEST (one of 5 methods — see Section 2)
    ↓
CONTENT EXTRACTION (on-device or Cloud Function)
    ↓
AI CLASSIFICATION (Groq gpt-oss-20b):
  → subject inference
  → topic inference
  → exam relevance
    ↓
STUDENT CONFIRMATION (confirm or override inferred metadata)
    ↓
AI SUMMARIZATION (Groq gpt-oss-20b):
  → summary
  → key points
  → definitions / formulas
    ↓
FLASHCARD GENERATION (Groq gpt-oss-20b, JSON schema):
  → Anki-style cards from key points
    ↓
QUIZ GENERATION (Groq gpt-oss-20b, JSON schema):
  → 5 MCQs from material content
    ↓
PERSIST:
  → Firestore: material metadata + summary + cards + quiz
  → Firebase Storage: original file (PDF / scan images)
  → Room DB: local cache of material + cards
    ↓
STUDENT STUDIES:
  → Reviews summary
  → Studies flashcard deck (SM-2)
  → Takes quiz
    ↓
QUIZ MISTAKES:
  → Auto-create remedial flashcards in the deck
    ↓
MASTERY TRACKED
```

---

## 2. 📥 Input Methods — Technical Detail

### Method 1 — 📷 Camera Scan (Expected Most-Used)

```
Student taps "Scan Notes"
  → CameraX opens with document edge detection
  → Up to 10 pages captured per session
  → ML Kit Document Scanner (on-device OCR) — works OFFLINE
  → Text extracted per page, merged into one text blob
  → Structure recognition: headings, lists, formulas identified
  → Editable review screen (student can correct OCR errors)
  → Text sent to Cloud Function: POST /notes/classify-and-summarize
  → AI classifies → AI summarizes → Flashcards + Quiz generated
  → Original scan images → Firebase Storage: /materials/{uid}/{materialId}/pages/
  → Firestore + Room updated
```

**Why this is the primary method:** Students already photograph notes for WhatsApp. This is a familiar behavior.

**What Scan Notes is NOT:**
- It is NOT the Image Doubt Solver (different purpose — see Section 3)
- It is NOT a generic photo app — it specifically processes study material

---

### Method 2 — 📄 PDF Upload

```
Student taps "Upload PDF"
  → File picker (Google Drive, Downloads, WhatsApp files)
  → PDF uploaded to Firebase Storage: /materials/{uid}/{materialId}/original.pdf
  → Progress bar shown (upload can take time on slow connections)
  → Firestore doc created: status = "processing"
  → Cloud Function triggers:
      a. Download PDF from Storage
      b. Extract text (pdf-parse library)
      c. Split into chunks (max 4000 tokens each)
      d. Classify: POST /ai/classify (subject + topic inference)
      e. Summarize each chunk, merge
      f. Generate flashcards
      g. Generate quiz
      h. Update Firestore: status = "complete"
  → Android app listens to Firestore doc (real-time listener)
  → When complete → show Subject Inference Confirmation screen
  → Student confirms/corrects subject + topic
  → Room DB updated with local cache
```

**Error handling:**
- Upload fail → retry with exponential backoff (max 3)
- PDF extraction fail → show "Could not read this PDF" error
- AI fail → save extracted text raw, allow manual review

---

### Method 3 — 🎥 YouTube Lecture URL

> [!NOTE]
> YouTube import requires transcript availability. Not all videos have transcripts. This must be communicated clearly to the student.

```
Student pastes YouTube URL
  → App validates URL format client-side
  → POST /notes/extract-youtube {videoUrl}
  → Cloud Function:
      a. Validate YouTube URL
      b. Attempt transcript fetch via YouTube Data API or third-party service
      c. If transcript unavailable → return specific error (not generic)
      d. If transcript available → chunk transcript
      e. Classify: subject + topic inference
      f. Summarize → Key points → Flashcards → Quiz
  → Show Subject Inference Confirmation
  → Persist to Firestore + Room
```

**Error states:**
- Invalid URL → inline error before submit
- No transcript → "No transcript available for this video. Try a different lecture or use Scan Notes."
- Unsupported video → "This video type is not supported."
- AI failure → "Summarization failed. Raw transcript saved."

---

### Method 4 — 🌐 Web Article / URL

```
Student pastes URL
  → App validates URL format
  → POST /notes/extract-url {url}
  → Cloud Function:
      a. Fetch page server-side (bypasses CORS)
      b. Strip HTML → extract readable text (readability algorithm)
      c. If no readable content → return specific error
      d. Classify → Summarize → Flashcards → Quiz
  → Show Subject Inference Confirmation
  → Persist
```

**Error states:**
- Invalid URL → inline error
- No extractable content → "Couldn't extract readable content from this site."
- Paywalled/blocked → "This site blocks content extraction."
- Backend failure → "Extraction failed. Try a different source."

> [!WARNING]
> URL import is NOT guaranteed for every website. Never document or display this as a universal capability.

---

### Method 5 — ✍️ Quick Text (Short Input)

```
Student types or pastes short text
  → TextField: max 10,000 characters
  → Label shown: "For short notes only — definitions, formulas, key concepts"
  → Classify → Summarize → Flashcards → Quiz (same pipeline)
  → If text > 2000 chars → suggest using PDF upload instead
```

**Quick Text is NOT for typing full chapters.** The UI must set this expectation.

---

## 3. 🔍 Image Doubt Solver — Full Technical Flow

> [!IMPORTANT]
> Image Doubt Solver is a **problem-solving tutoring tool**. It is architecturally and conceptually SEPARATE from the Learning Material Scanner.
>
> - **Scan Notes:** "I want to study this material → build learning tools from it"
> - **Image Doubt:** "I cannot solve this specific problem → explain it step by step"

### User Flow

```
Student opens "Image Doubt Solver"
  → Takes photo of problem (math, physics diagram, chemistry, handwritten question)
  → Optional: adds text question ("What formula do I apply here?")
  → Optional: selects subject hint (not mandatory — AI infers from image)
  → Taps "Solve Step by Step"
  → Image compressed → JPEG max 512KB → base64
  → POST /ai/image-doubt {base64Image, question, subject}
  → Cloud Function:
      a. Validate Firebase JWT
      b. Validate image size
      c. Build vision request for Groq gpt-oss-120b
      d. Stream response back (SSE)
  → Android receives SSE stream
  → Solution rendered in real-time with Markdown
```

### AI Response Structure (Expected)

```
1. Problem identified: [description]
2. Relevant concept: [concept name]
3. Applicable law/formula: [law + LaTeX formula]
4. Step-by-step solution:
   Step 1: ...
   Step 2: ...
   Step N: [Final Answer]
5. Common mistake to avoid: [specific pitfall]
```

### Optional Actions After Solution

- **Save as Learning Material** → creates a full material from the problem + solution
- **Create Flashcard** → saves concept to deck (student chooses deck)
- **Ask Follow-up** → opens mini-chat thread below solution (not full chat screen)

### Technical Implementation

```
Android:
  1. CameraX or gallery pick
  2. Bitmap → compress to JPEG, max 512KB:
      - Start quality = 90
      - Decrement by 10 until size ≤ 512KB or quality < 20
  3. Base64 encode (java.util.Base64)
  4. POST to backend (not directly to Groq)

Cloud Function (/ai/image-doubt):
  5. Verify Firebase JWT
  6. Check user quota (free: 2/day)
  7. Build Groq vision payload:
     {
       model: "openai/gpt-oss-120b",
       messages: [{
         role: "user",
         content: [
           { type: "text", text: "Solve this step by step. Subject: {subject}" },
           { type: "image_url", image_url: { url: "data:image/jpeg;base64,..." }}
         ]
       }]
     }
  8. Stream Groq response → SSE to Android
  9. Fallback: if Groq 429 → Cerebras gemma-4-31b (also supports vision)
```

---

## 4. 🤖 AI Chat — Contextual Study Tutor

### 4.1 What It Is

AI Chat in Quovex is a **context-aware study tutor**, not a generic chatbot. The difference is in the system prompt and context injection.

### 4.2 Context Injection (Server-Side)

The Cloud Function builds the system prompt dynamically using available context:

```javascript
const systemPrompt = `
You are Quovex AI — an expert study tutor, NOT a generic assistant.

Student context:
- Name: ${user.name}
- Exam: ${user.examName}
- Subjects: ${user.subjects.join(", ")}
- Current subject: ${context.subject || "unknown"}
- Current topic: ${context.topic || "unknown"}
- Student level: ${user.subjectLevels[context.subject] || "intermediate"}

${context.materialSummary ? `Current study material summary:\n${context.materialSummary}\n` : ""}
${context.recentMistakes.length > 0 ? `Recent quiz mistakes: ${context.recentMistakes.join(", ")}\n` : ""}

Behavior rules:
- Teach, don't just answer — explain the reasoning behind every answer
- Use numbered step-by-step explanations for problems
- Use LaTeX for math: $$formula$$
- If the question is ambiguous, ask ONE clarifying question before answering
- Adapt language and difficulty to the student's level
- Connect your answer to the student's current topic when possible
- If a question is outside your knowledge, say so — do not invent facts
- Do not claim to have read material that was not provided in context
- Keep answers focused — avoid unnecessary verbosity
- Use examples and memory tricks when they aid understanding
`;
```

### 4.3 Context Available for Injection

| Context Item | Source | Always Available? |
|---|---|---|
| User profile (exam, subjects, level) | Firestore / local | Yes |
| Current subject | From session or last Library view | Often |
| Current topic | From active material | Sometimes |
| Material key points | From current Learning Material | When material is open |
| Recent quiz mistakes | Room DB | When quizzes taken |
| Active focus session subject | TimerForegroundService | When session active |
| Last 10 chat turns | Room DB (chat_messages) | Yes |

### 4.4 Suggested Question Chips

Generated client-side based on current context:
- When subject = "Physics", topic = "Newton's Laws":
  - "Explain Newton's Third Law simply"
  - "Give me a harder example"
  - "Quiz me on this topic"
  - "Why is my answer wrong?"
  - "Give me a memory trick"

### 4.5 Provider Routing

```
POST /ai/chat {message, subject, topic, materialContext, history}
  → Try Groq gpt-oss-20b (round-robin key pool)
  → On 429: try next Groq key
  → All Groq exhausted: → Groq qwen3.6-27b (fallback within Groq)
  → All Groq keys exhausted: → Cerebras gpt-oss-120b
  → Stream SSE to Android
```

---

## 5. 🃏 Flashcard Engine — Full Technical Flow

### 5.1 Generation Pipelines

Flashcards are generated through 4 routes:

| Pipeline | Trigger | Provider |
|---|---|---|
| **From Learning Material** | Auto-generated after every material is processed | Groq gpt-oss-20b |
| **Direct prompt** | "Create 20 flashcards for Organic Chemistry" | Groq gpt-oss-20b |
| **Quiz mistake remedial** | Quiz answer marked wrong → remedial card auto-created | Local (no AI needed — card derived from quiz Q&A) |
| **Manual creation** | Student types front/back | No AI |

### 5.2 AI Generation Request (JSON Schema)

```json
{
  "model": "openai/gpt-oss-20b",
  "response_format": { "type": "json_object" },
  "messages": [
    {
      "role": "system",
      "content": "You are an expert tutor creating Anki-style flashcards. Output JSON containing an array of 'cards'. Each card needs 'front', 'back', 'formula_latex' (if applicable, else null), 'key_takeaway', 'difficulty' (1-5), and 'tags'."
    },
    {
      "role": "user",
      "content": "Generate flashcards based on these key points from the student's study material:\n[key_points]"
    }
  ]
}
```

### 5.3 SM-2 Spaced Repetition Algorithm

Runs 100% locally in Room DB — zero network required.

**Card parameters:**
- `easeFactor (EF)`: starts at 2.5
- `intervalDays (I)`: days until next review
- `repetitions (N)`: consecutive correct answers

**4 Review buttons:**

| Button | Score | EF Change | Interval |
|---|---|---|---|
| **Again** (forgot) | 0 | `max(1.3, EF - 0.2)` | < 1 min (show again this session) |
| **Hard** (barely) | 3 | `EF - 0.15` | `I * 1.2` |
| **Good** (recalled well) | 4 | unchanged | `I * EF` |
| **Easy** (perfect) | 5 | `EF + 0.15` | `I * EF * 1.3` |

### 5.4 Learning Material → Flashcard Link

Every flashcard deck generated from a Learning Material stores:
- `sourceMaterialId` → links back to the parent material
- On Learning Material Detail screen, the Flashcards tab shows the linked deck
- Deleting the material prompts: "Also delete [N] linked flashcards?"

---

## 6. 📝 Quiz Engine — Full Technical Flow

### 6.1 Quiz Types

| Type | Trigger | Questions |
|---|---|---|
| **Daily Quiz** | Auto-generated from today's studied topics | 5 MCQs |
| **Topic Quiz** | "Take Quiz" on a Learning Material | 5–10 MCQs |
| **Deck Quiz** | "Quiz from this deck" | 5 MCQs |
| **Remedial Quiz** | After review of mistake flashcards | 3–5 MCQs |

### 6.2 Generation Request

```
POST /ai/quiz/generate
Body: {
  materialId: string,
  keyPoints: string[],  // from Learning Material
  subject: string,
  topic: string,
  difficulty: 1-5,      // from student's subject level
  questionCount: 5
}
Response: { questions: QuizQuestion[] }
Provider: Groq gpt-oss-20b (JSON schema mode)
```

**Question schema:**
```json
{
  "question": "Which law states F = ma?",
  "options": ["Newton's First Law", "Newton's Second Law", "Newton's Third Law", "Hooke's Law"],
  "correctIndex": 1,
  "explanation": "Newton's Second Law: Force = mass × acceleration.",
  "relatedConcept": "Newton's Laws of Motion"
}
```

### 6.3 Quiz Mistake → Remedial Flashcard

```
Student answers incorrectly
  → Quiz records: { questionText, correctAnswer, studentAnswer, concept }
  → App creates remedial FlashcardEntity:
      front: questionText
      back: correctAnswer + "\n\nExplanation: " + explanation
      deckId: linked deck for this material
      state: "LEARNING" (high priority in SM-2)
      tags: [subject, topic, "remedial"]
  → Saved to Room DB
  → Best-effort sync to Firestore
  → Shows in daily review queue with HIGH priority
```

### 6.4 Quiz Result Screen

```
Score: 4/5 (80%)

✅ Correct: Newton's First Law, Second Law, Gravity, Momentum
❌ Incorrect: Newton's Third Law
   Your answer: "Action and reaction are not always equal"
   Correct: "Action and reaction forces are equal in magnitude, opposite in direction"
   Explanation: [full explanation]

📌 1 remedial flashcard added to Physics deck.

[ Review Flashcards ]  [ Done ]
```

---

## 7. 🗺️ Subject Inference — Technical Flow

### 7.1 Why AI-First Classification

Students should NOT need to select a subject before Quovex can process their material.

**Old flow (wrong):**
```
Upload PDF → Select Subject → Process
```

**Correct flow:**
```
Upload PDF → Process → AI Infers Subject → Student Confirms/Corrects
```

### 7.2 Classification Request

```
POST /ai/classify
Body: {
  textSample: string,  // first 2000 chars of extracted content
  filename?: string,   // "Newton_Laws.pdf" is a strong hint
  userId: string
}
Response: {
  subject: "Physics",
  topic: "Newton's Laws of Motion",
  subtopic: "Laws of Motion",
  examRelevance: ["JEE", "NEET", "Class 11"],
  confidence: 0.92
}
Provider: Groq gpt-oss-20b
```

### 7.3 Android Confirmation Screen

```
if (confidence >= 0.80) {
  // Show clear inference card
  "Looks like Physics · Newton's Laws"
  [✓ Confirm]  [✎ Change]
} else {
  // Show tentative inference with edit encouraged
  "We think this might be Physics · Newton's Laws"
  [✓ Looks Right]  [✎ Correct Subject]
}
```

If student taps **Change**, show inline form with:
- Subject dropdown (pre-populated with student's subjects)
- Topic text field (pre-filled with AI inference, editable)
- Title text field

Subject is saved to the Learning Material after confirmation. It is never a hard prerequisite.

---

## 8. 🔐 Authentication Flow

**Method:** Google Sign-In only (via Firebase Auth)  
**Account:** Mandatory — no guest mode

```
App Launch
    ↓
Check Firebase Auth session
    ↓
┌─────────────────────────┐    ┌───────────────────────────┐
│  Session exists?        │    │  No session               │
│  → Check Firestore for  │    │  → SplashScreen           │
│    user profile         │    │  → WelcomeScreen          │
│  → Profile exists       │    └───────────────────────────┘
│    → HomeScreen         │                ↓
│  → No profile           │    User: "Continue with Google"
│    → Onboarding         │                ↓
└─────────────────────────┘    Firebase Google Sign-In
                                            ↓
                               Firebase creates/fetches Auth user
                                            ↓
                               Check Firestore: users/{uid} exists?
                                    ↓                 ↓
                               YES: profile       NO: new user
                               exists             → Onboarding
                                    ↓
                               HomeScreen
```

### Token Refresh
- Firebase Auth token auto-refreshes every 1 hour
- All Cloud Function requests: `Authorization: Bearer {idToken}`
- Cloud Function verifies token with Firebase Admin SDK before processing

---

## 9. 🌐 Backend API Endpoints (Firebase Cloud Functions)

**Base URL:** `https://api-dopkbhqrgq-uc.a.run.app` (us-central1-quovex-f3104)  
**Auth:** All endpoints require `Authorization: Bearer {Firebase ID Token}`  
**Runtime:** Node.js 20, Firebase Cloud Functions (Gen 2)

### AI — Chat
```
POST /ai/chat
Body: {
  message: string,
  subject: string,
  topic: string,
  materialContext: { summary: string, keyPoints: string[] } | null,
  recentMistakes: string[],
  history: ChatMessage[]  // last 10 turns
}
Response: StreamingResponse (SSE)
Provider: Groq gpt-oss-20b → qwen3.6-27b → Cerebras gpt-oss-120b
Free limit: 10/day
```

### AI — Classify Material
```
POST /ai/classify
Body: { textSample: string, filename?: string }
Response: { subject, topic, subtopic, examRelevance, confidence }
Provider: Groq gpt-oss-20b
```

### AI — Summarize + Generate Learning Material
```
POST /ai/summarize
Body: {
  text: string,
  subject: string,
  topic: string,
  mode: "full"  // returns summary + keyPoints + formulas + flashcards + quiz
}
Response: {
  summary: string,
  keyPoints: string[],
  formulas: FormulaItem[],
  flashcards: FlashcardItem[],
  quizQuestions: QuizQuestion[]
}
Provider: Groq gpt-oss-20b
Free limit: 3/day
```

### AI — Study Plan Generation
```
POST /ai/plan/generate
Body: { examName, examDate, subjects, dailyHours, sessionHistory }
Response: { plan: DayPlan[], summary: string }
Provider: Cerebras gpt-oss-120b
Free limit: Premium only
```

### AI — Image Doubt Solver
```
POST /ai/image-doubt
Body: { base64Image: string, question: string, subject: string }
Response: StreamingResponse (SSE)
Provider: Groq gpt-oss-120b → Cerebras gemma-4-31b (fallback)
Free limit: 2/day
```

### AI — Quiz Generation
```
POST /ai/quiz/generate
Body: { keyPoints, subject, topic, difficulty, questionCount }
Response: { questions: QuizQuestion[] }
Provider: Groq gpt-oss-20b (JSON schema mode)
```

### Notes — PDF Extraction
```
POST /notes/extract-pdf
Body: { storageRef: string }
Response: { text: string, pageCount: number }
```

### Notes — URL Extraction
```
POST /notes/extract-url
Body: { url: string }
Response: { text: string, title: string, wordCount: number }
Error responses: INVALID_URL | NO_CONTENT | BLOCKED | EXTRACTION_FAILED
```

### Notes — YouTube Transcript
```
POST /notes/extract-youtube
Body: { videoUrl: string }
Response: { transcript: string, title: string, duration: number, language: string }
Error responses: INVALID_URL | NO_TRANSCRIPT | UNSUPPORTED_VIDEO | EXTRACTION_FAILED
```

### AI — Motivational Quote
```
GET /ai/quote?userId={uid}&streak={n}&subject={subject}
Response: { quote: string, author: string }
Provider: Groq gpt-oss-20b
```

### Health Check
```
GET /health
Response: { status: "online", groqKeysAvailable: 4, cerebrasKeysAvailable: 4 }
No auth required
```

### Error Response Format
```json
{
  "error": true,
  "code": "RATE_LIMIT_EXCEEDED | AI_UNAVAILABLE | INVALID_TOKEN | NO_CONTENT | NO_TRANSCRIPT | EXTRACTION_FAILED | ...",
  "message": "Human-readable description for display",
  "retryAfter": 3600
}
```

---

## 10. 🗄️ Data Models

### Firestore Collections

```
Firestore Root
├── users/
│   └── {uid}/
│       ├── (profile fields)
│       └── subcollections:
│           ├── sessions/
│           ├── flashcard_decks/
│           ├── notes/              ← Learning Materials metadata
│           └── friends/
│
├── study_plans/
│   └── {planId}/
│       └── tasks/
│
├── study_rooms/
│   └── {roomId}/
│       └── participants/
│
├── leaderboard/
│   └── weekly_{YYYY-WW}/
│
└── config/
    └── feature_flags
    └── exam_catalog
    └── ai_key_usage
```

### Room DB Tables (Local / Offline)

| Table | Purpose |
|---|---|
| `sessions` | Local copy of study sessions |
| `study_plans` | Active study plan JSON |
| `study_tasks` | Daily tasks from plan |
| `chat_messages` | AI chat history (last 100 per session) |
| `flashcard_decks` | Flashcard deck metadata |
| `flashcards` | Individual cards (SM-2 fields) |
| `notes` | Learning Material cache (summary + key points; NOT raw files) |
| `subjects` | User's subject list with mastery level |
| `quiz_results` | Local quiz history + mistake log |

### Learning Material Entity (Firestore + Room)

```kotlin
// Firestore: users/{uid}/notes/{materialId}
// Room: notes table
{
  id: "mat_abc123",
  userId: "user_xyz",
  inputType: "pdf" | "scan" | "youtube" | "url" | "text",
  title: "Newton's Laws of Motion",
  subject: "Physics",
  topic: "Newton's Laws",
  subtopic: "Laws of Motion",
  sourceUrl: "https://...",            // if URL/YouTube
  storageRef: "materials/uid/mat_abc123/original.pdf",  // if binary
  summary: "Newton's three laws of...",
  keyPoints: ["Inertia is...", "F=ma means..."],
  formulas: [{ name: "F=ma", latex: "F = m \\cdot a" }],
  flashcardDeckId: "deck_xyz",         // linked deck
  quizGenerated: true,
  createdAt: timestamp,
  syncStatus: "SYNCED" | "DRAFT" | "PENDING_SYNC"
}
```

---

## 11. 🗺️ Navigation Graph

```
NavHost(startDestination = "splash")

splash → welcome | home | onboarding/*

home (Bottom Nav Root)
  ├── library
  │     ├── library/add                  ← Add Learning Material picker
  │     │     ├── library/scan           ← Camera scan
  │     │     ├── library/import-url    ← URL/YouTube import
  │     │     └── library/quick-text    ← Quick text input
  │     ├── library/processing          ← AI processing pipeline
  │     ├── library/classify            ← Subject inference confirmation
  │     └── library/{materialId}        ← Material detail
  │           └── library/{materialId}/edit
  ├── ai/chat
  ├── ai/image-doubt                    ← separate from scan
  ├── ai/planner
  ├── flashcards
  │     └── flashcards/{deckId}
  │           └── flashcards/{deckId}/study
  ├── quiz/daily
  ├── quiz/{materialId}
  ├── quiz/result
  ├── timer
  │     ├── session/active
  │     └── session/summary
  ├── analytics
  ├── social/leaderboard
  ├── social/rooms → social/rooms/{roomId}
  ├── profile/me | profile/{uid}
  ├── settings → premium → premium/success
  └── music
```

---

## 12. 🔐 Android Permissions

| Permission | When Requested | Why | Mandatory? |
|---|---|---|---|
| `POST_NOTIFICATIONS` (Android 13+) | Onboarding | Daily reminders, streak alerts | Critical |
| `SCHEDULE_EXACT_ALARM` | Onboarding | Precise reminder timing | Critical |
| `INTERNET` | Auto (manifest) | All API calls | Always |
| `FOREGROUND_SERVICE` | Auto (manifest) | Timer foreground service | Always |
| `BIND_ACCESSIBILITY_SERVICE` | First use of Blocker | App blocking | Optional |
| `PACKAGE_USAGE_STATS` | First use of Blocker | Track app opens | Optional |
| `CAMERA` | First use of Scan Notes OR Image Doubt | Camera capture | Optional |
| `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE` | First PDF/image pick | PDF upload | Optional |

**Camera permission rationale must distinguish context:**
- Scan Notes: "To photograph your study material for import"
- Image Doubt: "To photograph the problem you want explained"

---

## 13. 📊 Full End-to-End Flow Reference

### A. Scan Notes (Handwritten Study Material)
```
Student → opens Scan Notes screen
Android → CameraX multi-page capture + ML Kit OCR (offline)
Android → structure recognition + editable review
Android → POST /notes/classify-and-summarize (text blob)
Cloud Function → classify → summarize → flashcards → quiz
Firestore → material created (status: complete)
Firebase Storage → scan images stored
Room → material cached locally
Android → Subject Inference Confirmation shown
Student → confirms/corrects subject + topic
Android → Learning Material Detail shown
```

### B. PDF Import
```
Student → picks PDF
Android → uploads to Firebase Storage (progress bar)
Firestore → status: "processing"
Cloud Function → extract text (pdf-parse) → classify → summarize → flashcards → quiz
Firestore → status: "complete"
Android → real-time listener fires → Subject Inference Confirmation
Student → confirms → Material Detail shown
Room → cached locally
```

### C. YouTube Import
```
Student → pastes YouTube URL
Android → validates URL format
Cloud Function → fetch transcript → if no transcript: specific error returned
Cloud Function → classify → summarize → flashcards → quiz
Firestore + Room → persist
Android → Subject Inference Confirmation → Material Detail
```

### D. Web URL Import
```
Student → pastes URL
Android → validates format
Cloud Function → scrape page → extract readable text → if no content: specific error
Cloud Function → classify → summarize → flashcards → quiz
Firestore + Room → persist
Android → Subject Inference Confirmation → Material Detail
```

### E. Quick Text
```
Student → types/pastes short text (max 10,000 chars)
Android → if text > 2000 chars: suggest PDF upload
Android → POST /ai/summarize (text)
Cloud Function → classify → summarize → flashcards → quiz
Room → persist locally
Best-effort → Firestore sync
Android → Subject Inference Confirmation → Material Detail
```

### F. Image Doubt Solver
```
Student → photos problem (or gallery pick)
Android → compress to JPEG max 512KB
Android → POST /ai/image-doubt (base64Image + optional question + subject hint)
Cloud Function → verify JWT → Groq gpt-oss-120b (vision) → stream SSE
Android → render solution in real-time (Markdown + LaTeX)
Student → optionally: Save as Material | Create Flashcard | Ask Follow-up
```

### G. AI Chat (Contextual Tutor)
```
Student → opens Chat (with optional subject/topic context active)
Android → sends: message + current subject + topic + materialContext + history
Cloud Function → builds dynamic system prompt with all context
Cloud Function → Groq gpt-oss-20b → stream SSE
Android → renders response in real-time (Markdown + LaTeX)
Android → saves turn to Room (chat_messages)
```

### H. Flashcard Generation
```
Trigger: Learning Material processed OR direct prompt OR quiz mistake
Cloud Function → POST /ai/summarize (mode: flashcards) OR direct prompt
Groq gpt-oss-20b → returns JSON array of cards
Android → insert FlashcardEntity rows to Room
Android → link deckId to parent materialId
Best-effort → Firestore sync
```

### I. Quiz Generation
```
Trigger: Learning Material processed OR manual "Generate Quiz"
Cloud Function → POST /ai/quiz/generate (keyPoints + subject + topic)
Groq gpt-oss-20b (JSON schema mode) → returns QuizQuestion[]
Android → store quiz locally (Room: quiz_results)
```

### J. Quiz Mistakes → Remedial Flashcards
```
Student answers quiz question incorrectly
Android → record: { questionText, correctAnswer, explanation, concept }
Android → create FlashcardEntity (front=question, back=answer+explanation)
Android → set SM-2 state = "LEARNING" (high priority review)
Android → insert to linked deck in Room
Best-effort → Firestore sync
→ Card appears in next daily review queue
```

---

## 14. DataStore Keys

```kotlin
object PreferenceKeys {
    val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
    val AI_QUERIES_TODAY = intPreferencesKey("ai_queries_today")
    val AI_QUERY_DATE = stringPreferencesKey("ai_query_date")
    val IMAGE_QUERIES_TODAY = intPreferencesKey("image_queries_today")
    val SUMMARIZE_COUNT_TODAY = intPreferencesKey("summarize_count_today")
    val STREAK_RESCUE_TOKENS = intPreferencesKey("streak_rescue_tokens")
    val LAST_SESSION_DATE = stringPreferencesKey("last_session_date")
    val CURRENT_STREAK = intPreferencesKey("current_streak")
    val REMINDER_TIME = stringPreferencesKey("reminder_time")
    val PREFERRED_STUDY_TIME = stringPreferencesKey("preferred_study_time")
    val THEME = stringPreferencesKey("theme")
    val SELECTED_LANGUAGE = stringPreferencesKey("language")
}
```

---

## 15. Content Ecosystem & Studio Architecture (v3.1)

### 15.1 Ecosystem Separation & Data Schema
**Status:** `NCERT & ORIGINALS PLANNED / MY MATERIALS IMPLEMENTED`

Quovex strictly segregates content ownership and rights into three discrete Firestore collections:

```
Firestore
├── official_resources/      # NCERT & Government Curriculum assets (read-only)
├── quovex_originals/        # Multi-agent synthesized books (published catalog)
├── user_materials/          # Student-imported private study materials
├── content_studio/          # Internal editorial pipelines & demand intelligence
│   ├── demand_signals/      # Anonymized student difficulty & doubt frequency
│   ├── book_requests/       # Admin drafting specifications
│   ├── generation_jobs/     # Research, debate & validation jobs
│   └── draft_books/         # Staged revisions (v1, v2) awaiting human approval
```

### 15.2 NCERT Official Resource Flow
```
Browse: Class (9-12) → Subject (Physics) → Book → Chapter (Laws of Motion)
  ├── Action 1: [ Read Official NCERT ]
  │     → Resolves officialSourceUrl (e.g. ncert.nic.in/textbook/pdf/kepy105.pdf)
  │     → Launches Custom Tab (No unauthorized PDF mirroring/re-hosting)
  └── Action 2: [ Study with Quovex AI ]
        → Ingests chapter concepts into Quovex AI transformation engine
        → Generates: Summary + Key Concepts + Spaced Repetition Cards + Quiz
        → Stored under user's learning assets (Originality in learning synthesis)
```

### 15.3 Quovex Originals Multi-Agent Production Pipeline
```
[ Anonymized Demand Signals ]
  │ (High doubt volume, low quiz accuracy on "Integration by Parts")
  ▼
[ Admin Reviews in Content Studio ] → Clicks [ Create Book Draft ]
  │
  ▼
[ 1. Research & Evidence Pack Assembly ]
  │ → Fetches verified facts, definitions, real-world examples, misconceptions
  │ → Embeds provenance metadata (sourceUrl, publisher, retrievedAt, evidenceId)
  │ → Strictly respects robots directives, terms of service, and copyright
  ▼
[ 2. Multi-Agent Reasoning Debate ]
  │ ├── Reasoning Agent A (Proposes chapter progression, analogies, worked examples)
  │ ├── Reasoning Agent B (Challenges misconceptions, rigor, pedagogy, edge cases)
  │ └── Synthesis Agent (Generates balanced editorial blueprint)
  ▼
[ 3. Original Educational Writing ]
  │ → Authors new explanations (Understand → rethink → reorganize → teach)
  │ → Zero copy/paraphrasing of external texts
  │ → Mathematical readability ($x^2 \to x²$, roots $\sqrt{x}$, Greek symbols $\theta, \alpha, \beta$, chemistry $\text{H}_2\text{O}$)
  ▼
[ 4. Multi-Tier Validation Engine ]
  │ ├── Fact Validation
  │ ├── Math / Formula Accuracy Validation
  │ ├── Curriculum Alignment Validation
  │ └── Pedagogical Consistency Validation
  ▼
[ 5. Human Editorial Review & Approval ]
  │ → Staged in Content Studio Review Queue
  │ → Admin can preview, edit chapters, compare versions (v1 vs v2), and approve
  │ → Only APPROVED books transition to PUBLISHED in the public catalog
  ▼
[ 6. Public Catalog Release ]
  → Visible under Knowledge Hub > Quovex Originals
  → Connects to Spaced Repetition Flashcards, Quizzes, and Quovex AI Tutor
```

### 15.4 Quovex Originals Native Student Experience Architecture
```
KnowledgeHubScreen
    │ (Tap Originals Banner)
    ▼
OriginalsBrowserScreen (OriginalsViewModel)
    ├── Filter Chips: Subject (Physics, Math, Chemistry, Biology)
    ├── Filter Chips: Curriculum (CBSE, JEE, NEET, AP, IB)
    ├── Real-time Search Text Field
    └── Book Card List (Title, Subject, Class, Chapters, Target Time)
            │ (Select Book Card)
            ▼
    OriginalBookDetailScreen
        ├── Overview & Learning Objectives
        ├── Prerequisites & Difficulty
        └── Table of Contents (Chapter Directory)
                │ (Select Chapter / Start Reading)
                ▼
        OriginalChapterReaderScreen
            ├── Section Navigation Bar (§ 1.1, § 1.2...)
            ├── Conceptual Explanation (QuovexMathText LaTeX/Unicode rendering)
            ├── Visual Analogy Callout Box
            ├── Step-by-Step Worked Problems (Formulas + Step Reasoning)
            ├── Real-World Engineering / Scientific Case Studies
            ├── Common Student Misconceptions & Traps (Cautionary Red Callouts)
            ├── Key Takeaway Summary Bullets
            └── Action Bar:
                  ├── "Ask Quovex AI" → Launches Contextual AI Chat
                  ├── "Cards (N)" → PrepareOriginalChapterStudyAidsUseCase → Launches SM-2 Flashcard Player
                  └── "Quiz (N)" → PrepareOriginalChapterStudyAidsUseCase → Launches Practice Quiz
```

### 15.5 Phase 11 — Real Production Study Aids Ingestion Pipeline
To eliminate mock data and avoid engine duplication, `PrepareOriginalChapterStudyAidsUseCase` bridges published Originals chapters directly into the student's local Room database:
1. **Material Record Generation**: Creates/finds a `NoteItem` entry corresponding to `"${book.title}: Ch ${chapter.chapterNumber} - ${chapter.title}"`.
2. **Flashcards Persistence**: Creates a `DeckItem` linked to `sourceMaterialId` and inserts all `OriginalFlashcard` items into Room DB.
3. **Quiz Ingestion**: Ingests all `OriginalQuizQuestion` items into Room DB with options, correct answer indices, and pedagogical explanations.
4. **Zero-Mock Execution**: Passes dynamic `materialId` and `deckId` to `QuovexNavGraph`, launching standard `QuizScreen` and `FlashcardPlayerScreen` with zero simulated data.



