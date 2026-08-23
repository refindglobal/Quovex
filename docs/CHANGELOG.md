# Quovex — Changelog

---

## [v3.1.0-docs] — 2026-08-22 — Content Ecosystem & Content Studio Specification

**Type:** Product & Architecture Documentation Update.

### Highlights
- **3 Unified Content Ecosystems in Knowledge Hub**:
  - `NCERT / OFFICIAL RESOURCES`: Official curriculum assets with Class → Subject → Book → Chapter hierarchy. Supports `[ Read Official NCERT ]` (official source portal URL linking without unpermitted PDF redistribution) and `[ Study with Quovex AI ]` (transformation into active learning assets).
  - `QUOVEX ORIGINALS`: High-yield, multi-agent reasoned educational books authored via Demand Signals → Research → Evidence Pack → Multi-Agent Debate → Synthesis → Original Writing → Multi-Tier Validation → Human Approval.
  - `MY MATERIALS`: User-imported private study materials (Scan, PDF, YouTube, Web, Quick Text).
- **Canonical Content Type Model**: Defined strict metadata separation across `OFFICIAL_RESOURCE`, `QUOVEX_ORIGINAL`, and `USER_MATERIAL`.
- **Content Studio for Admin Panel**: Added specification for Demand Signals, Book Requests, Generation Jobs, Draft Books, Review Queue, and Published Books within the existing Next.js Admin Panel.
- **Strict Governance & Brand Safety**:
  - Confirmed: Student activity NEVER auto-generates books (only creates demand signals).
  - Confirmed: Admin explicitly triggers book drafting; human editorial approval is mandatory.
  - Confirmed: Provider and model names (`Groq`, `Cerebras`, `OpenAI`) are internal implementation details only. Public identity is always **`Quovex AI`**.
- **Implementation Status**: Explicitly marked NCERT Library, Quovex Originals, Content Studio, Demand Signals, and AI Debate Pipeline as `PLANNED / NOT YET IMPLEMENTED`.

---

## [v3.0.0] — 2026-08-22 — Clean In-Place Rebuild & Device Verification Complete

**Type:** Production Rebuild & Live Device Verification (Phase 1–5 Complete).

### Highlights
- **In-Place Rebuild**: Rebuilt the existing Quovex repository cleanly with 100% adherence to latest v3 specifications, without creating external duplicate projects.
- **Room Database Migration v2→v3**: Bumped schema to version 3 with `MIGRATION_2_3`. Added exactly 15 new columns (notes: 9, decks: 1, flashcards: 4, sessions: 1) and 4 new tables (`subjects`, `quiz_questions`, `quiz_results`, `quiz_mistakes`).
- **Clean Architecture & Domain Layer**: Zero Android dependencies in domain models and 7 new UseCases (`ClassifyMaterialUseCase`, `GenerateQuizUseCase`, `RecordQuizResultUseCase`, `GetQuizResultsUseCase`, `CreateRemedialFlashcardsUseCase`, `ProcessScanAndSummarizeUseCase`, `ConfirmMaterialSubjectUseCase`).
- **Cloud Functions v2 Deployment**: Deployed 11 production endpoints to `https://api-dopkbhqrgq-uc.a.run.app` on Firebase project `quovex-f3104` with 4-key rotation pool and automatic failover.
- **UI & UX Complete**:
  - `KnowledgeHubScreen`: Dynamic subject chips, material cards with "Needs Processing" legacy badge, and central action state.
  - `AddMaterialScreen`: Multi-modal intake (Scan Notes, Web/YouTube, Quick Text).
  - `ProcessingScreen`: Real-time AI analysis progress status.
  - `SubjectInferenceScreen`: Confidence percentage badge and confirmation.
  - `MaterialDetailScreen`: 4 tabs (Summary, Key Concepts, Flashcards, Quiz).
  - `AiChatScreen`: Full AI Tutor with `Quovex AI` branding and clean LaTeX/Unicode mathematical notation ($x^2 \to x²$, roots $\sqrt{x}$, Greek symbols $\theta, \alpha, \beta$).
  - `QuizScreen` & `QuizResultScreen`: Full active recall taking flow with remedial flashcard generation from mistakes.
- **Brand Protection & Privacy**: Strictly redacted all internal LLM provider names (Groq, Cerebras, OpenAI) and model IDs across all UI, errors, and loaders.
- **Verification**: 100% unit tests passed (`testDebugUnitTest`), successful APK build and install, live device verification on `emulator-5554`.

---

## [v3.0.0-docs] — 2026-08-22 — Documentation Reset

**Type:** Documentation only — no code changes.

### Product Concept Changes
- Redefined Quovex as a **Learning Transformation System**, not a note-taking app
- Notes are now formally called **Learning Materials** — structured AI-enriched study assets
- Explicitly stated that **manual typing is NOT the primary input method**
- Primary material inputs ranked: Scan → PDF → YouTube → Web → Quick Text

### Subject Inference
- Changed subject selection to **AI-first inference**: Quovex infers subject/topic from content
- Subject selection is now an **optional correction step**, not a prerequisite for processing
- Defined the Subject Inference Confirmation UI: "Looks like Physics · Newton's Laws" + [Confirm] / [Change]
- Documented confidence threshold behavior (≥0.80 = confident display, <0.80 = tentative display)

### Notes / Learning Material System
- Replaced "note summarizer" concept with **full Learning Pipeline**:
  `Source → Classify → Summarize → Flashcards → Quiz → Study → Mastery`
- Every Learning Material now has: Summary, Key Concepts, Formulas, linked Flashcards, linked Quiz
- Learning Material → Flashcard → Quiz link is explicit and bidirectional

### Scan Notes Clarification
- Clearly separated **Scan Notes** (study material import) from **Image Doubt Solver** (problem tutoring)
- Scan Notes: multi-page capture → OCR → structure recognition → study material
- Document Scanner is NOT just OCR — it is study material reconstruction

### Image Doubt Solver Clarification
- Redefined Image Doubt as **problem-solving tutoring tool**, not a note import tool
- Defined structured AI response format: problem identification → concept → formula → steps → answer → common mistakes
- Documented optional follow-up chat thread (mini, not full screen)
- Documented optional actions: Save as Material, Create Flashcard

### Import Link / YouTube
- Replaced simplistic "paste URL" concept with validated, staged import flow
- Documented all error states: invalid URL, unsupported source, no transcript, no content, backend failure, AI failure, rate limit
- Stated explicitly: "URL import is NOT guaranteed for every website"
- YouTube: transcript-dependent — documented no-transcript error state

### AI Chat
- Upgraded AI Chat from generic chatbot to **contextual study tutor**
- Documented context injection: subject, topic, material summary, quiz mistakes, session, last 10 turns
- Defined AI Chat behavior requirements: teach, step-by-step, LaTeX, adaptive, no hallucination, clarify ambiguity
- Documented suggested question chips based on current topic
- Defined provider routing: Groq gpt-oss-20b → qwen3.6-27b → Cerebras gpt-oss-120b

### Quiz Engine
- Promoted Quiz to **first-class learning module**
- Defined 4 quiz types: Daily, Topic, Learning Material, Deck
- Defined quiz result: score, accuracy, correct answers, incorrect concepts, explanations
- Defined: Quiz mistakes → automatically create remedial flashcards in linked deck
- Explicitly stated: do not fabricate XP unless XP system is implemented

### Flashcard Integration
- Defined 4 flashcard generation pipelines: from material, direct prompt, quiz mistake remedial, manual
- Documented Learning Material ↔ Flashcard Deck link via `sourceMaterialId`
- Defined remedial flashcard creation: auto-added to deck with `state = LEARNING` (high priority)

### Knowledge Hub
- Renamed Library tab from "Knowledge Base" to **Knowledge Hub**
- Defined subject-grouped view: Learning Material + Flashcard count + Quiz count per topic
- Quick actions from hub: Study Cards, Take Quiz, Review Summary

### Architecture
- Updated ARCHITECTURE_MAP.md with Learning Pipeline flowchart
- Added Processing Locations Map (which operations run on Android vs Cloud Function vs AI)
- Added Knowledge Hub data relationship diagram
- Updated all Firestore path references: `notes/` → `users/{uid}/notes/` (user-scoped)

### AI Models
- Added explicit model assignments for: Classification, Flashcard Generation, Quiz Generation
- Added AI System Prompt Design Philosophy section (behavior requirements, not literal prompts)
- Clarified key storage: Cloud Functions env vars only — never in Android APK

### Contradictions Removed
- Removed assumption: "student types full chapters" — replaced with ingestion pipeline
- Removed requirement: "select subject before import" — replaced with AI-first inference
- Removed conflation of Scan Notes and Image Doubt Solver — clearly separated
- Removed implication: URL import always works — now documents failure modes
- Removed generic chatbot framing for AI Chat — replaced with contextual tutor
- Removed optional/disconnected Quiz framing — now first-class module

---

## [v2.1.0] — 2026-08-21

- Added 4-key rotation pool for Groq + Cerebras
- Added AdMob ads in free tier (banner / interstitial / rewarded)
- Added Admin Panel specification
- Removed parental control features

---

## [v2.0.0] — 2026-08-21

- Major expansion: Reddit research, global strategy
- Complete ecosystem: 15+ features documented
- Backend architecture defined
- Retention mechanics (Streak Rescue, RPG, Study Rooms)
- AI cost optimization strategy

---

## [v1.0.0] — 2026-08-21

- Initial documentation draft
- Core focus timer + AI chat + flashcard concepts
- Basic architecture outline
