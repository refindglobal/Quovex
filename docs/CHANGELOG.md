# Quovex — Changelog

---

## [v3.7.0] — 2026-08-25 — Phase 12: Retention Engine, Dashboard Analytics & Gamification

**Type:** Native Android Retention, Spaced Repetition Gamification, Advanced Telemetry & RPG Progression.

### Highlights
- **Anti-Duolingo Streak System & Room DB Migration v4**:
  - Migrated Room DB to version 4 with `user_stats` table tracking atomic streak days, longest streak record, rescue tokens count, and last study timestamp.
  - Implemented 1 weekly free rescue token protection saving streaks from accidental resets.
  - Added dynamic streak flame chip and rescue token display (`🪙 1 Rescue Token`) directly in the Dashboard header.
  - Dynamic "Streak at Risk!" alert banner with instant 1-tap rescue restore when `canUseRescueToken` triggers.
- **Deep Work & Consistency Analytics**:
  - **28-Day Consistency Heatmap Grid**: Visual 4-week calendar heatmap showing intensity levels (0 to 4) based on real focus minutes logged, weekday columns (M–S), and current day focus border.
  - **30-Day Subject Breakdown**: Visual segmented distribution bar and ranked subject summary calculating real proportion of time dedicated across Physics, Chemistry, Mathematics, Biology, and General Focus.
  - **Dynamic Exam Countdown Card**: Live countdown widget for target competitive exams (e.g., JEE Advanced, NEET, CBSE Class 12) with motivational guidance.
- **Scholar Rank RPG Progression & XP Rewards**:
  - 5 Tier Scholar Rank progression system: **Novice Scholar** (0–499 XP), **Apprentice Scholar** (500–1499 XP), **Scholar** (1500–3499 XP), **Expert Scholar** (3500–7499 XP), and **Master Scholar** (7500+ XP).
  - XP awards natively calculated and granted across learning milestones: +2 XP/min for focus sessions, +10 XP for correct quiz answers, +5 XP for flashcard reviews, and streak milestone bonuses.
  - Animated progress bar to next scholar rank rendered in Profile and Settings screen.
- **Scholar Achievements Engine**:
  - 7 core achievement badges evaluated from local Room DB entities:
    - *First Step*: Complete first deep focus study session (Unlocked).
    - *Week of Fire*: Maintain a 7-day active study streak (1/7 days).
    - *Iron Discipline*: Maintain a 30-day active study streak (1/30 days).
    - *10-Hour Scholar*: Log 10 hours of focused deep study.
    - *Deep Work Titan*: Log 50 hours of focused deep study.
    - *Deck Architect*: Create or transform 3 flashcard decks (Unlocked).
    - *Active Recall Master*: Complete 5 active recall quizzes.
  - Dynamic progress indicators for in-progress badges and emerald badge highlights for unlocked achievements.
- **Automated Morning Briefing Service**:
  - Integrated `MorningBriefingWorker.kt` scheduled via Android Jetpack `WorkManager` at 07:00 AM daily.
  - Emits local system notifications with daily target focus hours, streak status, and motivational study guidance.
- **Comprehensive Unit Testing Suite**:
  - Added 27 new tests for Phase 12 logic with 100% pass rate (**218 total unit tests passing in `:app:testDebugUnitTest`**).
  - Live verified on Android emulator with debug APK installation and high-res screen captures.

---

## [v3.6.0] — 2026-08-25 — Phase 11.2: Admin Storage Layer Hardening & Firestore Persistence

**Type:** Production Persistence, Storage Architecture Hardening, Audit Trail Security.

### Highlights
- **Production Google Cloud Firestore Verification (`quovex-f3104`)**:
  - Validated live connectivity via Google Cloud Application Default Credentials (ADC) without emulators.
  - Deployed production Firestore Security Rules (`match /quovex_originals/{bookId} { allow read: if resource.data.approvalStatus == 'PUBLISHED'; }`) and composite indexes.
  - Executed full 16-stage multi-agent synthesis, 5-tier validation, editorial review, and live publishing of *"Rotational Motion and Angular Momentum"* (CBSE Class 11 Physics) to cloud collection `quovex_originals`.
  - Proved persistence across server restart (`GET /api/content-studio/books?status=PUBLISHED` returned cloud data).
  - Verified native Android client reading directly from production Cloud Firestore in real-time (`OriginalsBrowserScreen` → `OriginalBookDetailScreen` → `OriginalChapterReaderScreen` with LaTeX math).
- **Immutable Security Audit Trail**:
  - `adminStore.logAudit()` writes immutable audit events directly to Firestore collection `admin_audit_logs` in `quovex-f3104`.
- **Persistent Feature Flag Engine**:
  - `adminStore.flags` migrated to Firestore collection `feature_flags` in `quovex-f3104`; state changes survive restarts and server deploys with automatic default seeding on initial boot.
- **Accurate Storage Architecture Matrix**:
  - **Firestore Backed (Verified Production `quovex-f3104` on 2026-08-25):** Content Studio (Books & Jobs), Security Audit Logs (`admin_audit_logs`), Feature Flags (`feature_flags`).
  - **In-Memory Local Session Store:** Users (`adminStore.users`), Moderation Reports (`adminStore.moderationReports`), Notification Campaigns (`adminStore.notifications`), and Demand Signals (`signalsStore`).
- **Build & Test Verification**:
  - Re-verified Next.js admin tests (34/34 pass, production build 50/50 routes) and Android clean test suite (191/191 pass).

---

## [v3.5.0] — 2026-08-23 — Phase 11: Real Production E2E Integration & Hardening

**Type:** Production Hardening, End-to-End Verification, Security & Data Integrity.

### Highlights
- **End-to-End Learning Ecosystem Verification**: Verified complete real-data workflow:
  - Admin Content Studio → Book Request → 16-Stage Multi-Agent Authoring → 5-Tier Validation → Human Editorial Sign-Off → Publishing to Firestore `quovex_originals`.
  - Android Knowledge Hub → Originals Browser → Book Overview → Chapter Reader with rich Unicode math (`QuovexMathText`).
  - Seamless Practice Ingestion: Created `PrepareOriginalChapterStudyAidsUseCase.kt` ingesting chapter flashcards and quiz questions into Room DB for instant execution in `FlashcardPlayerScreen` (SM-2 spaced repetition) and `QuizScreen` without code duplication.
- **Zero Mock Data Standard**: 100% real event calculations across all platform telemetry. Zero fake users, fake books, fake analytics, or fake revenue.
- **Security & RBAC Enforcement**:
  - `verifyAdminSession` enforces HTTP-only sessions and RBAC (`SUPER_ADMIN`, `ADMIN`, `EDITOR`, `MODERATOR`, `ANALYST`) returning 401/403.
  - Server-Side Approval Invariant strictly rejects unapproved publishing attempts.
  - Firestore Security Rules enforce public read restrictions exclusively on `approvalStatus == 'PUBLISHED'`.
  - Provider LLM API keys masked as `••••••••a92f`. Student UI strictly branded as **Quovex AI**.
- **Automated Test Suites & Builds**:
  - Android: 191+ unit tests PASS 100%, `./gradlew assembleDebug` completes with 0 errors.
  - Next.js Admin: 34 / 34 tests PASS 100%, production build compiles 50/50 routes with 0 errors.
- **Phase 11 Documentation Suite**: Created `docs/PHASE11_REALITY_BASELINE.md`, `docs/PHASE11_E2E_VERIFICATION_REPORT.md`, `docs/PHASE11_DATA_INTEGRITY_REPORT.md`, `docs/PHASE11_SECURITY_AUDIT.md`, and `docs/PHASE11_PRODUCTION_READINESS.md`.

---

## [v3.4.0] — 2026-08-23 — Phase 10: Quovex Originals Student Experience

**Type:** Native Android Feature, UI/UX, Content Delivery & Student Learning Experience.

### Highlights
- **Quovex Originals Student Experience**: Seamless catalog and reading experience within the native Android application:
  - **Knowledge Hub Entry**: Prominent emerald-accented Quovex Originals banner alongside NCERT Official Library.
  - **Originals Browser (`OriginalsBrowserScreen.kt`)**: Multi-filter catalog browsing by Subject (Physics, Chemistry, Mathematics, Biology), Curriculum (CBSE, JEE, NEET, AP, IB), and real-time conceptual search.
  - **Book Overview (`OriginalBookDetailScreen.kt`)**: Syllabus alignment, estimated reading time, learning objectives checklist, and interactive Table of Contents.
  - **Chapter Reader (`OriginalChapterReaderScreen.kt`)**: Section-by-section reader with rich Unicode math formatting (`QuovexMathText`), visual analogies, step-by-step worked numericals, real-world case studies, and common student misconceptions/traps.
  - **Contextual Learning Integrations**: One-tap navigation to Chapter Flashcards, Practice Quizzes, and Quovex AI Chat with pre-populated chapter context.
- **Strict Zero-Mock Data & Security Compliance**:
  - Direct Firestore real-time integration querying `quovex_originals` where `approvalStatus == 'PUBLISHED'`.
  - Non-published books (Draft, Generating, Rejected) remain completely invisible to student clients.
  - Strict **Quovex AI** student-facing branding preserving backend provider abstraction.
- **Verification & Test Suite**:
  - Full suite of Android unit tests for `OriginalsViewModel` and `QuovexOriginalsRepository` passing 100%.
  - Full Android debug build compilation (`assembleDebug`) verified with 0 errors.
  - Full Next.js Admin test suite (25/25) and production build (50/50 routes) verified.

---

## [v3.3.0] — 2026-08-23 — Phase 9: Admin Control Center Completion

**Type:** Administrative Control Plane, Operations & Security Architecture.

### Highlights
- **Quovex Admin Control Center (`quovex-admin`)**: Full command center spanning 50 static & dynamic routes across 6 primary operational domains:
  - **Overview**: Unified `/dashboard` (real platform KPIs, study session statistics, active jobs), `/users` & `/users/[uid]` (student management, suspension controls, exam/class filters), `/analytics` (DAU/WAU/MAU, session retention), and `/system` (real-time service telemetry).
  - **Learning**: `/content` (tripartite separation: Official NCERT vs Quovex Originals vs private User Materials), `/ncert` (Classes 9–12 catalog inspection & URL validator), and `/content-studio` (Phase 8 multi-agent authoring pipeline).
  - **AI Infrastructure**: `/ai` (real-time latency tracking, provider health, and masked key pool `••••••••a92f`).
  - **Operations**: `/notifications` (FCM push notification campaign composer with audience targeting), `/moderation` (user and study room report queue with dismiss/warn/suspend actions), `/feature-flags` (server-side toggle center with rollout percentages), and `/audit-logs` (immutable security audit trail).
  - **Business**: `/monetization` (factual billing status with zero fake revenue).
  - **Settings**: `/settings` (platform maintenance mode, global limits) and `/login` (secure admin authentication).
- **Server-Side Role-Based Access Control (RBAC)**: Role hierarchy (`SUPER_ADMIN`, `ADMIN`, `EDITOR`, `MODERATOR`, `ANALYST`) enforced across all mutations and sensitive endpoints (`lib/auth/rbac.ts`).
- **Zero Mock Data Guarantee**: 100% real event calculations across all platform metrics; empty states and explicit "unavailable" notices rendered when data is not yet present.
- **Automated Verification Suite**: 25/25 automated unit and security tests passed (19.4s), Next.js production build succeeded with 0 errors across 50 routes, and 191/191 Android unit tests passed.

---

## [v3.2.0] — 2026-08-23 — Phase 8: Content Studio, Demand Intelligence & Quovex Originals Complete

**Type:** Backend, Control Plane & Content Platform Architecture.

### Highlights
- **Quovex Admin Control Plane (`quovex-admin`)**: Complete Next.js 15 App Router administrative dashboard with 8 dedicated sub-routes:
  - `/content-studio` (Studio Overview & KPI Metrics)
  - `/content-studio/demand` (Demand Signals & Explainable Score Breakdown)
  - `/content-studio/requests` & `/requests/new` (Book Request Creation Wizard)
  - `/content-studio/jobs` (Asynchronous 16-Stage Multi-Agent Worker Monitor & Live Logs)
  - `/content-studio/drafts` (Manuscripts in progress)
  - `/content-studio/books/[bookId]` (Draft Editor, LaTeX math preview, surgical section regenerator)
  - `/content-studio/review` (Human Editorial Review Queue & Mandatory Approval Sign-off)
  - `/content-studio/published` (Published Catalog with Staging vs Production isolation)
  - `/content-studio/analytics` (Post-publication engagement, retention, and accuracy delta metrics)
- **Zero Mock Data Guarantee**: Zero mock demand signals, fake books, fake analytics, or hardcoded KPI counters in production runtime. 100% real event calculations and dedicated empty states.
- **Demand Intelligence Engine**: Deterministic normalization (0–100) across question volume, quiz mistakes, accuracy penalties, flashcard failure rates, image doubt frequency, and student breadth.
- **16-Stage Multi-Agent Authoring Pipeline**:
  - Controlled Research & Evidence Pack generation with source provenance tracking.
  - Multi-Agent Debate between Agent A (Architect) and Agent B (Challenger), synthesized into an immutable Editorial Blueprint.
  - Original Educational Writer producing chapters, step-by-step worked numericals, real-world engineering case studies, common student traps, integrated SM-2 flashcard decks, and concept-reinforcing quizzes.
  - 5-Tier Quality Inspection: Fact, Math, Curriculum, Pedagogy, and Consistency validation tiers.
- **Server-Side Approval Invariant**: Publish endpoint strictly rejects publication unless `approvalStatus == 'APPROVED' && approvedBy != null && approvedAt != null`. Client cannot bypass approval fields.
- **Public Android Content Contract**: Pure Kotlin domain models (`QuovexOriginalModels.kt`) and repository interface (`QuovexOriginalsRepository.kt`) allowing students to consume only approved and published originals.
- **Verification & Staging Test Book**: Successfully executed end-to-end authoring workflow for *"Newton's Laws — Made Simple"* through research, debate, synthesis, writing, validation (98/100), human review, approval, staging publication, and unpublish verification.
- **Automated Test Suite**: 12/12 Node.js/tsx tests passed (22.7s), Next.js production build succeeded with 0 errors across 21 routes, and 191/191 Android unit tests passed.

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
