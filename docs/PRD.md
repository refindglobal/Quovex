# Quovex — Product Requirements Document (PRD)

**Version:** 3.0  
**Date:** 2026-08-22  
**Status:** Living Document  
**Platform:** Android (Native — Kotlin)  
**AI Providers:** Groq (primary chat/summarization/quiz), Cerebras (study planning/fallback)  
**Vision:** A complete Student Operating System — not just a study app

> [!IMPORTANT]
> **Quovex is NOT a traditional note-taking app.**
> Quovex is a **learning transformation system**. It takes raw study material in any form — handwritten notes, PDFs, YouTube videos, web articles — and transforms them into structured, AI-enriched learning assets: summaries, key concepts, flashcard decks, quizzes, and a mastery tracker.
> Manual text typing is supported but is the **last resort input method, not the primary one.**

---

## 1. Product Vision

> **"Quovex is the last study app a student will ever need — an AI-powered learning transformation system that reads your study material, understands it, and builds your learning tools automatically."**

Quovex eliminates the need for Forest + Anki + Notion + ChatGPT + a distraction blocker. It replaces all of them with one unified, intelligent, gamified platform built for students worldwide.

---

## 2. Problem Statement (Research-Backed)

Based on Reddit research across r/studytips, r/productivity, r/GetStudying, and r/adhd_anxiety:

| Problem | Percentage of Students Affected |
|---|---|
| Phone distraction during study | ~90% |
| Passive study (re-reading, highlighting) with no recall | ~75% |
| Motivation loss after missing one day | ~70% |
| Using 4+ disconnected study apps | ~65% |
| Decision paralysis (don't know where to start) | ~60% |
| Study anxiety and burnout | ~55% |
| Students do NOT type out full chapters — they use PDFs, scans, YouTube | ~85% |

**Core insight:** Students don't need another tool. They need a system that reads their material, understands it for them, and builds learning tools automatically.

---

## 3. Target Users

### Primary — "The Aspirant" (India + Global South)
- Age: 16–24, preparing for competitive exams
- JEE, NEET, UPSC (India) / SAT, MCAT, A-Levels (Global)
- Has PDFs, scanned notes, YouTube lectures — does NOT type full chapters
- Heavy Android user

### Secondary — "The College Student" (Global)
- Age: 18–25, university coursework
- Overwhelmed by lecture slides, PDFs, and information overload
- Wants AI to organize and transform material into study tools

### Tertiary — "The Lifelong Learner"
- Age: 25–35, working professional upskilling
- Limited time, needs maximum efficiency per session

---

## 4. Goals & Success Metrics

| Goal | Metric | Target |
|---|---|---|
| Daily habit formation | Day 7 retention | > 40% |
| Monthly engagement | Day 30 retention | > 15% |
| AI satisfaction | In-app rating | > 4.2/5 |
| Session completion rate | Sessions completed vs started | > 70% |
| Freemium conversion | Free → Premium | > 5% |
| Global reach | Countries with DAU > 100 | > 20 countries by month 6 |

---

## 5. Core Feature Modules

---

### MODULE A — FOCUS ENGINE

#### A1. Smart Focus Timer
**Priority:** P0

| ID | Requirement |
|---|---|
| F-001 | Customizable Pomodoro / Deep Work / custom intervals |
| F-002 | Presets: 25/5, 50/10, 90/20 |
| F-003 | Runs as Foreground Service — survives app kill |
| F-004 | Session labeled by subject |
| F-005 | Auto-save session to Room DB |
| F-006 | Audio + haptic alerts |
| F-007 | Timer ring widget on home screen |

#### A2. Multi-Layer Distraction Blocker
**Priority:** P0

| ID | Requirement |
|---|---|
| F-010 | Select apps to block per session |
| F-011 | Blocked apps → fullscreen overlay with time remaining |
| F-012 | AccessibilityService intercepts blocked app launches |
| F-013 | UsageStatsManager tracks app attempts (even if blocked) |
| F-014 | **Distraction Log** — "You resisted Instagram 7 times today" |
| F-015 | **Strict Mode (Premium):** Force-exit = broken session logged |
| F-016 | **Website Blocker (Premium):** DNS-based local VPN blocking |
| F-017 | Permission setup wizard (step-by-step UI for Accessibility) |
| F-018 | Pre-session "Distraction Cleanse" ritual UI |

#### A3. Focus Detection (Camera AI)
**Priority:** P2 (Premium)

| ID | Requirement |
|---|---|
| F-020 | ML Kit Face Detection — detects drowsiness (eyes closed >3s) |
| F-021 | Detects repeated away-from-screen head turns |
| F-022 | Gentle alert (sound + screen flash) when distraction detected |
| F-023 | Focus Score per session (0–100) based on detection events |
| F-024 | Camera runs ONLY during active session, fully on-device |
| F-025 | Explicit opt-in; zero data leaves device |

---

### MODULE B — LEARNING MATERIAL SYSTEM

> [!IMPORTANT]
> This module is the **core of Quovex's differentiation**. Notes are NOT a generic notebook.
> Every item in this module is a **Learning Material** — a structured study asset built from a source.

#### The Learning Pipeline

```
SOURCE MATERIAL
    ↓
Quovex reads / understands the content
    ↓
AI identifies: subject · topic · exam relevance
    ↓
Quovex presents inferred metadata
    ↓
Student confirms or corrects (subject / topic / title)
    ↓
Quovex builds a LEARNING MATERIAL:
    • Summary
    • Key Concepts
    • Definitions / Formulas
    • Flashcard Deck
    • Quiz Questions
    ↓
Student studies → tracks mastery
```

**Subject inference is AI-first.** The student does NOT need to select a subject before the system can understand the material. Subject selection is an optional correction mechanism, not a prerequisite.

#### B0. Learning Material Inputs
**Priority:** P0

Primary inputs (in order of expected usage):

| ID | Input Method | Description |
|---|---|---|
| L-001 | 📷 Scan Handwritten Notes | Camera + ML Kit OCR → structure recognition → study material |
| L-002 | 📄 Upload PDF | Upload → backend extracts → AI summarizes + classifies |
| L-003 | 🎥 YouTube Lecture | URL → transcript extraction → AI summarizes + classifies |
| L-004 | 🌐 Web / Article URL | URL → scrape → AI summarizes + classifies |
| L-005 | ✍️ Quick Text | Short definitions, formulas, small text — NOT full chapters |

> [!WARNING]
> Quick Text (L-005) is intentionally limited. It is designed for short inputs — definitions, formulas, short concepts. It is **not** the primary method for ingesting study material. The UI must communicate this clearly.

#### B1. AI Subject & Topic Inference
**Priority:** P0

After any material is processed, the AI must:

| ID | Requirement |
|---|---|
| L-010 | Infer subject (Physics, Chemistry, Maths, Biology, History, etc.) |
| L-011 | Infer chapter/topic (e.g., "Newton's Laws", "Thermodynamics") |
| L-012 | Infer subtopic where possible |
| L-013 | Infer exam relevance where possible (JEE, NEET, etc.) |
| L-014 | Present inferred metadata to student: "Looks like **Physics · Newton's Laws**" |
| L-015 | Provide **[Confirm]** and **[Change]** actions — not mandatory re-selection |
| L-016 | Subject selection on import is optional — student may import without pre-selecting |

#### B2. Document Scanner / Study Material Import
**Priority:** P0

> [!NOTE]
> Document Scanner is NOT just "take a photo and OCR text." It is the primary way students import handwritten and printed study material into Quovex.

| ID | Requirement |
|---|---|
| L-020 | CameraX multi-page capture (up to 10 pages per session) |
| L-021 | ML Kit on-device OCR — works offline |
| L-022 | Structure recognition: headings, bullet points, formulas, definitions |
| L-023 | Editable OCR result before processing |
| L-024 | Send to AI for summarization + classification |
| L-025 | Original scan images saved to Firebase Storage |
| L-026 | Result: Learning Material with summary, key concepts, flashcards |

**Document Scanner is a study material import tool. It is distinct from Image Doubt Solver.**

#### B3. Image Doubt Solver
**Priority:** P1 (Premium)

> [!NOTE]
> Image Doubt Solver is a **problem-solving tool**, not a note import tool. Its purpose is to help students understand a specific problem they cannot solve.

| ID | Requirement |
|---|---|
| L-030 | Student photos a problem they cannot solve |
| L-031 | Groq vision model (gpt-oss-120b) identifies the problem |
| L-032 | AI provides: problem identification → relevant concept → step-by-step reasoning → formula/law → calculation → final answer |
| L-033 | Highlights common mistakes related to the problem type |
| L-034 | Student can ask follow-up questions in a mini-chat thread |
| L-035 | Optional: Save solution as Learning Material |
| L-036 | Optional: Create Flashcard from the concept |
| L-037 | Supports: math equations, physics numericals, chemistry problems, diagram-based questions, handwritten problems |

**Image Doubt is a tutoring tool. It does not create note summaries — it explains specific problems.**

#### B4. AI Chat Assistant — Contextual Study Tutor
**Priority:** P0

> [!IMPORTANT]
> AI Chat is NOT a generic ChatGPT-style chat. Quovex AI is a **context-aware study tutor** that knows the student's current subject, topic, learning material, flashcard deck, quiz history, and session.

| ID | Requirement |
|---|---|
| F-030 | Chat UI for study doubts — tutor-style, not generic |
| F-031 | Groq gpt-oss-20b (primary) → Groq fallback → Cerebras (failover) |
| F-032 | **Context injection:** AI knows current subject, topic, learning material, active quiz, recent mistakes |
| F-033 | Markdown rendering (math equations, code, tables, lists) |
| F-034 | Free: 10 queries/day (enforced via DataStore) |
| F-035 | Premium: Unlimited + extended context |
| F-036 | Suggested question chips based on current topic |
| F-037 | Chat history saved locally in Room |
| F-038 | Retry with exponential backoff (max 3 retries) |
| F-039 | Offline: cached responses for 50 common study FAQs |

**AI Chat behavior requirements:**
- Teach rather than simply answer
- Explain reasoning clearly and step-by-step
- Adapt explanation difficulty to student's level
- Use student's current topic as default context
- Connect answers to source learning material where available
- Identify and address misconceptions
- Ask a clarifying question when the question is ambiguous
- Distinguish known source facts from AI inference
- Avoid pretending it read content that it did not receive
- Use examples and memory tricks when helpful
- Handle formulas/math with LaTeX rendering
- Refuse to invent facts; acknowledge when it does not know

**Context injected into AI system prompt (server-side):**
- User profile (exam, subjects, level)
- Current subject and topic
- Active learning material (summary/key concepts if available)
- Recent quiz mistakes
- Current focus session context
- Last 10 conversation turns

#### B5. AI Study Planner
**Priority:** P1 (Premium)

| ID | Requirement |
|---|---|
| F-050 | User inputs: subjects, topics, exam date, daily available hours |
| F-051 | Cerebras gpt-oss-120b generates day-by-day study schedule |
| F-052 | Schedule adapts based on actual session completion history |
| F-053 | Manual override: drag-and-drop topic reordering |
| F-054 | Daily task list on home screen derived from plan |
| F-055 | "What should I study today?" — one-tap AI recommendation |

---

### MODULE C — FLASHCARD & QUIZ ENGINE

> [!IMPORTANT]
> Flashcards and Quizzes are NOT separate utilities. They are part of a single mastery tracking loop:
>
> **Learning Material → Flashcards → Quiz → Mistakes → Remedial Flashcards → Mastery**

#### C1. AI Flashcard Generator
**Priority:** P1

| ID | Requirement |
|---|---|
| F-060 | Auto-generated from every Learning Material processed |
| F-061 | Direct prompt generation: "Create 20 flashcards for Organic Chemistry" |
| F-062 | SM-2 spaced repetition algorithm (pure Kotlin, domain layer) |
| F-063 | Daily flashcard review queue (auto-scheduled) |
| F-064 | Subject-wise decks — linked to parent Learning Material |
| F-065 | Free: 50 cards/month. Premium: unlimited |
| F-066 | Manual card creation (front/back + optional image) |
| F-067 | Quiz mistake cards automatically added to remedial review queue |

#### C2. Quiz Engine
**Priority:** P1

> [!IMPORTANT]
> Quiz is a **first-class learning module** in Quovex, not an optional add-on.

| ID | Requirement |
|---|---|
| Q-001 | **Daily Quiz** — 5 questions from today's studied topics |
| Q-002 | **Topic Quiz** — quiz on a specific topic from Learning Material |
| Q-003 | **Learning Material Quiz** — auto-generated from a specific note/material |
| Q-004 | **Deck Quiz** — quiz generated from a flashcard deck |
| Q-005 | Primary question type: Multiple Choice (4 options) |
| Q-006 | Secondary: Short Answer where reliable evaluation exists |
| Q-007 | Questions generated by Groq gpt-oss-20b with JSON schema enforcement |
| Q-008 | Quiz result shows: score, accuracy, correct answers, incorrect concepts, explanation |
| Q-009 | Quiz mistakes → automatically queued as remedial flashcards |
| Q-010 | No XP awarded unless an actual XP/points system is implemented and tracked |
| Q-011 | Mistakes log saved locally (Room) for AI context injection |

---

### MODULE D — KNOWLEDGE HUB & UNIFIED CONTENT ECOSYSTEM

> [!IMPORTANT]
> The **Knowledge Hub** is the central content area of Quovex. It is NOT a generic file list or basic note list.
> It unifies three major, logically and visually distinct content ecosystems under a single roof:
> 
> ```
> KNOWLEDGE HUB
> │
> ├── 📚 NCERT / OFFICIAL RESOURCES (Official Curriculum Assets)
> │
> ├── ✦ QUOVEX ORIGINALS (Original Multi-Agent Reasoned Books)
> │
> └── 📁 MY MATERIALS (User-Imported Study Materials: Scan, PDF, YouTube, Web, Text)
> ```
> 
> All three ecosystems connect directly to the Quovex learning tools: **Read → Summary → Key Concepts → Flashcards → Quiz → Mistakes → Remedial Flashcards → Mastery Tracker**.

#### D0. Canonical Content Type & Metadata Model

Every item in the Knowledge Hub conforms to a unified content model with strict source separation:

```typescript
type ContentType = 'OFFICIAL_RESOURCE' | 'QUOVEX_ORIGINAL' | 'USER_MATERIAL';

interface BaseContentMetadata {
    id: string;
    contentType: ContentType;
    title: string;
    subject: string;
    topic: string;
    language: string; // 'en' | 'hi' | 'es' | etc.
    countryRegion: string; // 'IN' | 'US' | 'UK' | 'GLOBAL'
    curriculum: string; // 'CBSE' | 'ICSE' | 'StateBoard' | 'AP' | 'IB' | 'GCSE' | 'A-Level'
    gradeClass: string; // 'Class 11' | 'Grade 12' | 'Undergraduate'
    exam?: string; // 'JEE' | 'NEET' | 'SAT' | 'UPSC'
    sourceAuthority: string; // 'NCERT' | 'Quovex Studio' | 'User'
    sourceUrl?: string;
    createdAt: number;
    updatedAt: number;
}

// 1. OFFICIAL_RESOURCE Specific Metadata
interface OfficialResourceMetadata extends BaseContentMetadata {
    contentType: 'OFFICIAL_RESOURCE';
    publisher: string; // e.g. "National Council of Educational Research and Training (NCERT)"
    officialSourceUrl: string; // Official portal URL (e.g. ncert.nic.in)
    bookTitle: string;
    chapterNumber: number;
    rightsNotice: string;
    isStudyTransformAvailable: boolean;
}

// 2. QUOVEX_ORIGINAL Specific Metadata
interface QuovexOriginalMetadata extends BaseContentMetadata {
    contentType: 'QUOVEX_ORIGINAL';
    generationJobId: string;
    version: number; // e.g. 1, 2, 3
    approvalStatus: 'REQUESTED' | 'GENERATING' | 'DRAFT' | 'READY_FOR_REVIEW' | 'REVISION_REQUESTED' | 'APPROVED' | 'PUBLISHED' | 'UNPUBLISHED' | 'ARCHIVED';
    approvedBy?: string;
    approvedAt?: number;
    createdBy: string; // Admin UID or "Content Studio Engine"
    difficulty: 'Simple' | 'Intermediate' | 'Advanced';
    targetReadingTimeMinutes: number;
    chapterCount: number;
}

// 3. USER_MATERIAL Specific Metadata
interface UserMaterialMetadata extends BaseContentMetadata {
    contentType: 'USER_MATERIAL';
    ownerUid: string;
    inputType: 'SCAN' | 'PDF' | 'YOUTUBE' | 'WEB' | 'QUICK_TEXT';
    storageRef?: string; // Firebase Storage reference for original scan/PDF
    syncStatus: 'LOCAL_ONLY' | 'SYNCED' | 'PENDING_SYNC';
}
```

#### D1. NCERT / Official Resource Library
**Status:** `PLANNED / NOT YET IMPLEMENTED`  
**Priority:** P1  

- **Structure**: Browse hierarchy by `Class → Subject → Book → Chapter` (e.g., *NCERT → Class 11 → Physics → Part 1 → Chapter 5: Laws of Motion*).
- **Display Card**: Shows official metadata badge (`NCERT`), Class, Subject, and Chapter Title.
- **Actions**:
  1. **`[ Read Official NCERT ]`**: Directs the student to the official source portal (e.g., `ncert.nic.in`). Quovex does NOT assume redistribution rights or mirror official PDFs without verified licenses.
  2. **`[ Study with Quovex AI ]`**: Transforms the official curriculum topic into the full Quovex learning pipeline: Summary, Key Concepts, Formulas, Flashcards, and Practice Quiz.
- **Copyright & Provenance**: NCERT content is strictly classified as `OFFICIAL_RESOURCE` and is never claimed as a Quovex Original.

#### D2. Quovex Originals (Original Multi-Agent Reasoned Books)
**Status:** `PLANNED / NOT YET IMPLEMENTED`  
**Priority:** P1  

- **Concept**: Original, high-yield educational books synthesized to eliminate learning bottlenecks.
- **Creation Flow**:
  1. **Demand Intelligence**: Aggregates anonymized student friction signals (AI Tutor doubt frequency, quiz mistake clusters, flashcard failure rates).
  2. **Admin Initiation**: Admin reviews demand and clicks `Create Book Draft`. **Student activity never automatically generates published books.**
  3. **Evidence Pack**: Fact-checking pipeline gathers verified evidence with source provenance.
  4. **Multi-Agent Reasoning Debate**: Internal Reasoning Agent A and Agent B challenge pedagogy, structure, and depth; Synthesis Agent produces editorial plan.
  5. **Original Educational Writing**: Authors new explanations, real-world examples, and step-by-step intuition (Zero mechanical rewriting/paraphrasing).
  6. **Multi-Tier Validation**: Fact, Math, Terminology, Curriculum, and Consistency checks.
  7. **Human Review & Approval**: Admin reviews, edits, and explicitly approves. **AI is advisory; human approval is mandatory before public release.**
- **Quovex Original Book Structure**:
  - Cover & Overview
  - Learning Objectives
  - Concept Breakdown (Simple → Intermediate → Advanced)
  - Worked Examples & Real-World Context (sports, space, medicine, tech)
  - Common Pitfalls & Misconceptions
  - Quick Revision Summary
  - Integrated Spaced Repetition Flashcards & Practice Quiz

#### D3. My Materials (User-Owned Learning Materials)
**Status:** `CURRENTLY IMPLEMENTED IN v3.0`  
**Priority:** P0  

- Ingested via Multi-Modal selector (Scan Notes, PDF, YouTube, Web URL, Quick Text).
- AI-first classification and subject inference.
- Generates 4 integrated learning views: Summary, Key Concepts, Spaced Repetition Flashcards, and Practice Quiz.

#### D4. Knowledge Hub Unified UI & Navigation
- **Top Ecosystem Selector**: Switch between `[ Official Resources ]`, `[ Quovex Originals ]`, and `[ My Materials ]`.
- **Subject & Topic Filtering**: Dynamic horizontal subject chips (`All`, `Physics`, `Chemistry`, `Maths`, `Biology`, etc.).
- **Card Distinction Badges**: High-contrast pill badges clearly identifying `NCERT`, `QUOVEX ORIGINAL`, or `MY MATERIAL`.
- **Unified Action**: Instant access to Study Flashcards, Take Quiz, or Ask Quovex AI across all three content sources.

---

### MODULE E — ANALYTICS & PROGRESS

#### E1. Dashboard
**Priority:** P0

| ID | Requirement |
|---|---|
| F-090 | Today's study hours, weekly total, current streak |
| F-091 | GitHub-style heatmap calendar |
| F-092 | Subject-wise time breakdown (pie chart) |
| F-093 | Productivity curve — best hours of the day |
| F-094 | Focus Score trend graph |
| F-095 | "Days until exam" countdown |

#### E2. Session History
**Priority:** P1

| ID | Requirement |
|---|---|
| F-100 | Full log: date, subject, duration, focus score, distraction count |
| F-101 | Weekly PDF/image report share (Premium) |
| F-102 | AI weekly insight: "Your best study day was Wednesday" |

---

### MODULE F — MOTIVATION & RETENTION ENGINE

#### F1. Streak System (Anti-Duolingo Design)
**Priority:** P0

| ID | Requirement |
|---|---|
| F-110 | Daily streak counter with flame icon |
| F-111 | **Streak Rescue Tokens** — 1 free token/week; spend to protect streak on missed day |
| F-112 | Tokens earned through consistent study (not purchasable) |
| F-113 | Streak Cemetery — visual tombstones of past broken streaks |
| F-114 | Milestone celebrations: 7, 30, 100, 365-day streak |

#### F2. Morning Briefing (Daily Hook)
**Priority:** P0

| ID | Requirement |
|---|---|
| F-120 | Personalized AM notification at user's set time |
| F-121 | Shows: today's plan, streak, friend activity |
| F-122 | AI-generated motivational message (Groq, personalized) |
| F-123 | One-tap to start first session |

#### F3. Smart Nudges
**Priority:** P1

| ID | Requirement |
|---|---|
| F-130 | Streak protection alert before midnight if not studied |
| F-131 | "3 friends are studying right now" social FOMO notification |
| F-132 | AI re-engagement after 2 missed days (warm, non-judgmental tone) |
| F-133 | Smart timing: nudges sent at user's historically productive hours |

#### F4. RPG Progression System
**Priority:** P1

| ID | Requirement |
|---|---|
| F-140 | Study XP earned per session (based on duration + focus score) |
| F-141 | Global Scholar Level (Novice → Apprentice → Scholar → Expert → Master) |
| F-142 | Subject mastery levels (per-subject XP) |
| F-143 | Cosmetic Avatar — unlockable outfits/accessories via XP milestones |
| F-144 | Achievement badges (100hr total, 30-day streak, 500 flashcards reviewed, etc.) |
| F-145 | All rewards earned through study — never purchasable |

#### F5. Built-in Focus Music
**Priority:** P2

| ID | Requirement |
|---|---|
| F-150 | Lo-fi / white noise / nature sounds radio |
| F-151 | Plays during session, pauses on break |
| F-152 | No need to open YouTube/Spotify — keeps user in Quovex |

---

### MODULE G — SOCIAL & COMMUNITY

#### G1. Leaderboard
**Priority:** P1 (Premium)

| ID | Requirement |
|---|---|
| F-160 | Weekly study hours leaderboard — Friends + Global |
| F-161 | Subject-specific leaderboards |
| F-162 | Friend study battle — 1v1 weekly study hour challenge |
| F-163 | Reset weekly; Top 3 get special badge for the week |

#### G2. Study Rooms (Live Co-Study)
**Priority:** P2 (Premium)

| ID | Requirement |
|---|---|
| F-170 | Create/join virtual study rooms |
| F-171 | See who's in the room + their timer running |
| F-172 | Ambient presence: profile picture + timer only (no video/audio) |
| F-173 | Public rooms (join strangers) + private (friends only) |
| F-174 | Room chat during breaks only |

#### G3. User Profiles
**Priority:** P1

| ID | Requirement |
|---|---|
| F-180 | Username, avatar, level, total study hours, top subject |
| F-181 | Public profile shareable via link |
| F-182 | Friend system: add by username or QR code |

---

### MODULE H — ONBOARDING & PERSONALIZATION

| ID | Requirement |
|---|---|
| F-190 | Name, age, exam type (select from global list) |
| F-191 | Subjects + exam date input |
| F-192 | Daily available hours + preferred study time |
| F-193 | AI generates first study plan on day 1 |
| F-194 | First session achievable in < 2 minutes of setup |
| F-195 | 7-day Premium trial on first install |

---

## 6. AI Provider Strategy

| Provider | Use Case | Model | Why |
|---|---|---|---|
| **Groq** | Chat, summarization, quiz gen, quotes, daily briefing, flashcards | gpt-oss-20b / gpt-oss-120b | Ultra-fast LPU inference (<1.5s) |
| **Cerebras** | Study plan generation, long-context reasoning, fallback | gpt-oss-120b / gemma-4-31b | Large 128K context, complex outputs |

### Key Rotation Architecture (4 Keys Each = Zero Cost)

Quovex uses **4 Groq API keys + 4 Cerebras API keys** in a rotating pool via a **backend proxy (Firebase Cloud Functions)** — never embedded in the Android app.

```
Android App → Firebase Auth Token → Cloud Function (AI Gateway)
                                          ↓
                              AI Key Pool Manager
                         ┌────────────────────────┐
                         │  groq_key_1  (active)  │
                         │  groq_key_2  (active)  │  → Round-Robin
                         │  groq_key_3  (active)  │
                         │  groq_key_4  (cooldown)│
                         └────────────────────────┘
                                          ↓
                              If all Groq keys hit 429
                                          ↓
                         ┌────────────────────────┐
                         │ cerebras_key_1 (active) │
                         │ cerebras_key_2 (active) │  → Failover
                         │ cerebras_key_3 (active) │
                         │ cerebras_key_4 (cooldown│
                         └────────────────────────┘
```

### Rotation Rules
| Rule | Detail |
|---|---|
| Strategy | Round-Robin (least-used key first) |
| On 429 error | Mark key as cooldown (60 min), try next key |
| All keys exhausted | Show friendly "AI resting, try in X min" message |
| Daily limit tracking | Stored in backend (Firestore counter per key) |
| Key security | Stored as Cloud Functions env vars — NEVER in Android APK |
| Auth guard | Firebase Auth JWT required on every proxy request |

### Cost Control
- Prompt caching for system prompts (60–80% token savings)
- Pre-cache 100 motivational quotes and study tips locally
- Smart routing: short queries → Groq; long planning → Cerebras
- Retry with exponential backoff, max 3 retries
- AI queries tracked per user in DataStore (free tier daily limit enforcement)

---

## 7. Backend Architecture

### Phase 1 (MVP — 0 to 50K MAU)
- **Firebase Auth + Firestore + FCM + Storage + Cloud Functions**
- Estimated: $0–$80/month
- Zero ops overhead, ships fast

### Phase 2 (Growth — 50K to 500K MAU)
- Firebase for: Auth, real-time social, FCM
- **Supabase (PostgreSQL)** for: analytics, session history, complex queries
- Estimated: $200–$600/month

### Phase 3 (Scale — 500K+ MAU)
- Move heavy analytics to self-hosted PostgreSQL (Railway/Fly.io)
- Keep Firebase for Auth + real-time
- Estimated: $800–$2000/month (vs $5000+ on Firebase-only)

---

## 8. Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose (dark-first, Material 3) |
| Architecture | MVVM + Clean Architecture |
| Local DB | Room (SQLite) — Room-first, best-effort Firestore sync |
| Networking | Retrofit + OkHttp |
| AI Gateway | Firebase Cloud Functions (all AI calls — no client-side keys) |
| AI Chat | Groq gpt-oss-20b (primary), Cerebras gpt-oss-120b (fallback) |
| Backend | Firebase (Auth, Firestore, FCM, Storage, Cloud Functions) |
| Analytics DB | Supabase (Phase 2) |
| Focus Detection | ML Kit Face Detection (on-device) |
| OCR | ML Kit Document Scanner (on-device, offline capable) |
| App Blocking | AccessibilityService + UsageStatsManager |
| Background | WorkManager + TimerForegroundService |
| Payments | Google Play Billing Library v6 |
| Analytics | Firebase Analytics + Crashlytics |
| DI | Hilt |
| Image Loading | Coil |
| Spaced Repetition | Custom SM-2 algorithm (pure Kotlin, domain layer) |

---

## 9. Global Strategy

### Language Support
| Phase | Languages |
|---|---|
| v1.0 | English, Hindi |
| v1.5 | Spanish, Portuguese, Arabic |
| v2.0 | French, German, Bahasa Indonesia, Turkish |

### Exam Catalog
| Region | Pre-configured Exams |
|---|---|
| India | JEE, NEET, UPSC, CA Foundation, GATE, Class 10/12 Boards |
| USA/Canada | SAT, ACT, AP, GRE, MCAT, LSAT, USMLE |
| UK | A-Levels, GCSE, UCAT |
| Middle East | Tawjihi, MOE Exams |
| Global | IELTS, TOEFL, PTE, GMAT |

### Pricing (PPP-Adjusted)
| Country | Monthly | Annual |
|---|---|---|
| India | ₹149 | ₹999 |
| USA | $4.99 | $34.99 |
| Brazil | R$14.99 | R$99.99 |
| Indonesia | Rp 29,000 | Rp 199,000 |
| Nigeria | ₦2,000 | ₦13,000 |

---

## 10. Monetization Model

### 10A — Free Tier (Ad-Supported)

Free users see ads via **Google AdMob**. Ads are placed at natural, non-intrusive moments.

| Ad Format | Placement | Expected eCPM |
|---|---|---|
| **Banner Ad** | Bottom of Home + Analytics screen | $0.10–$1.15 |
| **Interstitial Ad** | After every 3rd completed session | $1.00–$6.50 |
| **Rewarded Ad** | User watches ad to unlock 5 extra AI queries | $10–$50 |

**Ad Rules:**
- No ads during an active focus session
- No ads on the timer screen
- Rewarded ads are always opt-in
- Premium users: **zero ads**, ever

### 10B — Premium Subscription

| Plan | Price |
|---|---|
| Free | ₹0 / $0 |
| Premium Monthly | ₹149 / $4.99 |
| Premium Annual | ₹999 / $34.99 |
| 7-day Free Trial | On first install |

### 10C — Feature Map

| Feature | Free (Ad-Supported) | Premium |
|---|---|---|
| Focus Timer (unlimited) | ✅ | ✅ |
| Basic App Blocker | ✅ | ✅ |
| AI Chat | ✅ 10/day | ✅ Unlimited |
| Daily Quiz | ✅ | ✅ |
| Basic Analytics / Streak | ✅ | ✅ |
| AI Flashcard Generator | ✅ 50 cards/mo | ✅ Unlimited |
| Scan Notes (up to 3/day) | ✅ | ✅ Unlimited |
| AdMob Ads | ✅ Shown | ❌ No ads |
| AI Learning Material (summarize) | ✅ 3/day | ✅ Unlimited |
| Image Doubt Solver | ✅ 2/day | ✅ Unlimited |
| YouTube Import | ❌ | ✅ |
| AI Study Planner | ❌ | ✅ |
| Strict Mode Blocker | ❌ | ✅ |
| Website Blocker | ❌ | ✅ |
| Focus Camera Detection | ❌ | ✅ |
| Leaderboard & Study Rooms | ❌ | ✅ |
| RPG Avatar Unlocks | ✅ (limited) | ✅ Full |
| Focus Music | ✅ 2 tracks | ✅ All tracks |
| Analytics PDF Export | ❌ | ✅ |
| Extra AI queries (Rewarded Ad) | ✅ Watch ad | ✅ No need |

---

## 11. Non-Functional Requirements

| Requirement | Target |
|---|---|
| App cold start | < 2 seconds |
| AI response time (Groq) | < 1.5 seconds |
| Battery per hour (focus session) | < 5% |
| Offline support | Timer, Blocker, Flashcards, local Analytics, local notes |
| Min Android version | Android 8.0 (API 26) |
| Target Android version | Android 14 (API 34) |
| Crash-free rate | > 99% |
| App size | < 35 MB |
| Accessibility | TalkBack support, minimum contrast ratios |

---

## 12. Security & Privacy

- API keys in Cloud Functions env vars — never in Android APK, never hardcoded
- `google-services.json` — gitignored
- `secrets.properties` — gitignored
- Camera data — fully on-device, never uploaded
- No PII sent to Groq/Cerebras APIs
- Premium validation via Firebase Functions (server-side)
- GDPR compliant for EU users
- COPPA compliant (no data collection for under-13)
- Explicit consent for camera, notifications, accessibility service
- All AI calls authenticated via Firebase Auth JWT

---

## 13. Open Questions

- [ ] **Q1:** Should YouTube import support auto-fetching transcript via YouTube Data API, or should we rely on third-party transcript services?
- [ ] **Q2:** Should Focus Music have offline caching or stream-only?
- [ ] **Q3:** Should "Study Rooms" support optional audio (lo-fi ambient)?
- [ ] **Q4:** Should the app support Google Calendar sync for study plan?
- [ ] **Q5:** Referral system for user growth (earn premium days)?
- [ ] **Q6:** Should avatar system have social sharing (share your scholar card)?
- [ ] **Q7:** Should URL import show a clear disclaimer that not all websites are extractable?

## 14. Admin Panel & Content Studio

Quovex includes a dedicated internal administration environment built on Next.js / React + Firebase Admin SDK. See [ADMIN_PANEL.md](./ADMIN_PANEL.md) and [CONTENT_STUDIO_SPEC.md](./CONTENT_STUDIO_SPEC.md) for full specifications.

**Key admin capabilities:**
- **Executive Operations**: Real-time DAU/MAU, revenue, crash-free rate dashboard
- **User Management**: Ban, grant premium, delete accounts, inspect usage
- **AI Key & Gateway Manager**: Real-time monitoring and failover for rotating provider key pools
- **Push Notification Center**: Targeted broadcast announcements to segments
- **Feature Flags**: Remote toggles without requiring app updates
- **Content Studio (`PLANNED / NOT YET IMPLEMENTED`)**:
  - **Overview**: High-level content pipeline health and catalog inventory.
  - **Demand Signals**: Aggregates anonymized student friction (AI Tutor doubt frequencies, quiz mistake clusters, flashcard failure rates).
  - **Book Requests**: Admin drafting parameters (Subject, Topic, Curriculum, Class, Exam, Language, Target Reading Time).
  - **Generation Jobs**: Multi-agent research, evidence pack assembly, and debate tracking.
  - **Review Queue**: Human editorial review, fact-checking, math validation, diff comparison, and approval.
  - **Published Books Catalog**: Public catalog management and version tracking.
  - **Content Analytics**: Chapter completion rates, student quiz accuracy before/after, and AI tutor helpfulness.

---

## 15. Competitive Advantage Summary

| Quovex Has | Competitors Don't |
|---|---|
| 3 Unified Content Ecosystems (NCERT + Originals + User Materials) | Fragmented across disconnected apps |
| Reads your PDFs/scans and builds study tools | Other apps require manual input |
| AI infers subject/topic automatically | Others require manual tagging |
| Learning Material → Flashcards → Quiz → Mastery loop | Siloed tools |
| Demand-driven Quovex Originals with multi-agent debate | Generic unverified AI text dumps |
| Image Doubt Solver (problem tutoring) | Separate expensive apps |
| Distraction blocker INSIDE study app | Forest/Freedom are separate apps |
| Spaced repetition built-in | Requires Anki separately |
| Contextual AI tutor (knows your material) | Generic ChatGPT-style bots |
| Gamified RPG progression | Most study apps are dry |
| Global PPP pricing & Multi-Curriculum support | Most apps India-only or US-only |
| Morning briefing AI | Generic notifications only |
| Streak Rescue Tokens | Duolingo punishes you harshly |
| Social Study Rooms | No study app has ambient co-study |

---

## 16. Revision History

| Version | Date | Changes |
|---|---|---|
| 1.0 | 2026-08-21 | Initial draft |
| 2.0 | 2026-08-21 | Major expansion: Reddit research, global strategy, complete ecosystem (15+ features), backend architecture, retention mechanics, AI cost optimization |
| 2.1 | 2026-08-21 | Added: 4-key rotation for Groq + Cerebras, AdMob ads in free tier (banner/interstitial/rewarded), Admin Panel spec |
| 3.0 | 2026-08-22 | **Documentation Reset:** Redefined Notes as Learning Material System. Made subject inference AI-first (not mandatory pre-selection). Promoted Quiz to first-class module. Separated Scan Notes from Image Doubt. Upgraded AI Chat to contextual study tutor with context injection. Defined Knowledge Hub as unified learning space. Linked Learning Material → Flashcards → Quiz → Mistakes → Remedial loop. Removed manual typing as primary input assumption. |
| 3.1 | 2026-08-22 | **Content Ecosystem Update:** Added 3 major ecosystems to Knowledge Hub (NCERT / Official Resources, Quovex Originals, My Materials). Defined canonical metadata model with source separation. Added Content Studio, Demand Intelligence, Evidence Pack, Multi-Agent Debate, Multi-Tier Validation, Human Approval workflow, and Versioning. Explicitly documented `PLANNED / NOT YET IMPLEMENTED` status tags. |
