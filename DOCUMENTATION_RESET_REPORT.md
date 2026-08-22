# DOCUMENTATION RESET REPORT
**Quovex — v3.0.0-docs**  
**Date:** 2026-08-22  
**Executed by:** Antigravity  
**Phase:** Documentation Correction Only

---

> [!IMPORTANT]
> **No source code was modified in this phase.**
> No Kotlin files, Compose UI, Firebase Functions, database schema, or Gradle files were changed.
> This report documents documentation changes only.

---

## FILES UPDATED

| File | Action | Version |
|---|---|---|
| [`docs/PRD.md`](file:///d:/Quovex%20APP/docs/PRD.md) | Full rewrite | v2.1 → v3.0 |
| [`docs/UI_UX_SPEC.md`](file:///d:/Quovex%20APP/docs/UI_UX_SPEC.md) | Full rewrite | v1.0 → v2.0 |
| [`docs/TECHNICAL_DEEP_DIVE.md`](file:///d:/Quovex%20APP/docs/TECHNICAL_DEEP_DIVE.md) | Full rewrite | v2.0 → v3.0 |
| [`docs/ARCHITECTURE_MAP.md`](file:///d:/Quovex%20APP/docs/ARCHITECTURE_MAP.md) | Full rewrite | v1.x → v2.0 |
| [`docs/AI_MODELS.md`](file:///d:/Quovex%20APP/docs/AI_MODELS.md) | Updated | v1.x → v2.0 |
| [`docs/DESIGN_AND_FEATURES.md`](file:///d:/Quovex%20APP/docs/DESIGN_AND_FEATURES.md) | Targeted corrections | v1.0 → v2.1 |
| [`docs/CHANGELOG.md`](file:///d:/Quovex%20APP/docs/CHANGELOG.md) | Created | NEW |

**Files NOT modified (intentional):**
- `docs/RESEARCH.md` — research data remains valid; no product changes affect it
- `docs/ASSETS_REQUIRED.md` — asset list unchanged; Learning Material icon set addendum below
- `docs/PROJECT_FILES.md` — file structure not changed by docs reset; update in next implementation phase
- `docs/ADMIN_PANEL.md` — admin spec unchanged; no documentation corrections required
- `PHASE4_LIBRARY_FLASHCARDS_REPORT.md`, `PHASE5_FOCUS_ENGINE_REPORT.md`, `PHASE6_NOTES_REPORT.md` — phase reports are historical records; not modified

---

## PRODUCT CHANGES

**Core product identity corrected:**

| Before (Wrong) | After (Correct) |
|---|---|
| "Quovex is a study app with a note summarizer" | "Quovex is a learning transformation system" |
| "Library is a knowledge base" | "Library is the Knowledge Hub" |
| "Notes are text items a student writes" | "Learning Materials are structured AI study assets built from any source" |
| "Student types notes → AI summarizes" | "Student imports material → AI reads, classifies, summarizes, generates tools" |

---

## NOTES / LEARNING MATERIAL CHANGES

**Old concept (removed):**
> Student opens Notes → taps Add Note → types content → AI summarizes

**New concept (documented):**
```
SOURCE MATERIAL (PDF / Scan / YouTube / Web / Quick Text)
    ↓
AI reads and understands content
    ↓
AI identifies: subject · topic · exam relevance
    ↓
Student confirms or corrects metadata
    ↓
Quovex builds Learning Material:
    - Summary
    - Key Concepts
    - Definitions / Formulas
    - Flashcard Deck (linked)
    - Quiz Questions (linked)
    ↓
Student studies → tracks mastery
```

**Changes documented:**
- Quick Text input explicitly marked as "short inputs only — not for full chapters"
- Quick Text UI must include explanation: "For short notes only — definitions, formulas, key concepts"
- Learning Material = the fundamental unit of knowledge in Quovex

---

## SUBJECT INFERENCE CHANGES

**Old behavior (wrong):**
> Student must select subject before system can process material

**New behavior (documented):**
```
Student uploads/scans/imports material
    ↓
System processes content first
    ↓
AI classifies: subject + topic + subtopic + exam relevance + confidence score
    ↓
Subject Inference Confirmation screen:
    "Looks like Physics · Newton's Laws"
    [✓ Confirm]  [✎ Change]
    ↓
Student can confirm (one tap) or correct (subject dropdown + topic field)
```

**Rules:**
- Subject selection is NEVER a prerequisite for processing
- If confidence ≥ 0.80: show clear inference ("Looks like...")
- If confidence < 0.80: show tentative inference ("We think this might be...")
- Student is never blocked from proceeding

---

## IMPORT FLOW CHANGES

**Old (too simplistic):**
> "Paste URL → loading... → summary shown"

**New (properly specified):**

| Stage | Description |
|---|---|
| URL validation | Client-side format check before any request |
| Source validation | Server identifies if site is extractable |
| Content extraction | Scrape with readability algorithm |
| Transcript fetch | YouTube-specific — transcript availability checked |
| Classification | AI infers subject + topic |
| Summarization | AI generates summary + key concepts |
| Flashcard generation | AI generates deck |

**All error states documented:**
- Invalid URL
- Unsupported source
- No transcript (YouTube-specific)
- No extractable content
- Backend extraction failure
- AI failure
- Rate limit

**Explicit disclaimer documented:** URL import is NOT guaranteed for every website.

---

## SCAN NOTES CHANGES

**Old (wrong):**
> "Scan Notes: take a photo, get OCR text"

**New (correct):**
> "Scan Notes is the primary way students import handwritten and printed study material. It is not just OCR — it is study material reconstruction."

**Full pipeline documented:**
```
CameraX multi-page capture (up to 10 pages)
    ↓
ML Kit on-device OCR (offline capable)
    ↓
Structure recognition: headings, bullets, formulas, definitions
    ↓
Editable review (student can correct OCR errors)
    ↓
AI classification + summarization
    ↓
Flashcards + Quiz generated
    ↓
Scan images → Firebase Storage
    ↓
Learning Material created
```

---

## IMAGE DOUBT CHANGES

**Old (wrong):**
> Image Doubt described alongside Scan Notes as another camera-based note feature

**New (correct):**
> Image Doubt Solver is a **problem-solving tutoring tool** — architecturally and conceptually separate from Scan Notes.

| Feature | Scan Notes | Image Doubt |
|---|---|---|
| Purpose | Import study material | Solve a specific problem |
| What student has | Notebook page, textbook | A problem they cannot solve |
| Output | Learning Material (summary, cards, quiz) | Step-by-step solution + explanation |
| Navigation | Library flow | AI section |
| Follow-up | Full material detail screen | Mini chat thread below solution |

**Documented AI response structure:**
1. Problem identified
2. Relevant concept
3. Applicable law / formula (LaTeX)
4. Step-by-step solution (numbered)
5. Final answer (highlighted)
6. Common mistake to avoid

**Optional post-solve actions:** Save as Material | Create Flashcard | Ask Follow-up

---

## AI CHAT CHANGES

**Old (wrong):**
> "Chat UI for study doubts" — generic chatbot framing

**New (correct):**
> AI Chat is a **context-aware study tutor** that knows the student's current learning context.

**Context injected server-side:**
- User profile (exam, subjects, level)
- Current subject and topic
- Active Learning Material (summary/key concepts)
- Recent quiz mistakes
- Active focus session context
- Last 10 conversation turns

**Behavior requirements documented:**
- Teach rather than simply answer
- Explain reasoning step-by-step
- Adapt difficulty to student level
- Use LaTeX for math
- Ask ONE clarifying question when ambiguous
- Never claim to have read material not in context
- Distinguish inference from source facts
- No hallucination

**Provider routing documented:**
```
Groq gpt-oss-20b → Groq qwen3.6-27b → Cerebras gpt-oss-120b
```

---

## QUIZ CHANGES

**Old status:** Optional, disconnected from flashcards

**New status:** **First-class learning module** in Quovex

**4 quiz types defined:**
1. Daily Quiz (auto-generated from today's topics)
2. Topic Quiz (from a specific Learning Material)
3. Learning Material Quiz (auto-generated on material creation)
4. Deck Quiz (from a flashcard deck)

**Quiz result now shows:**
- Score + accuracy
- Correct answers list
- Incorrect answers with full explanation
- Remedial action: "N cards added to your deck for review"

**XP explicitly de-fabricated:** Do not show XP unless actual XP tracking system exists.

---

## FLASHCARD INTEGRATION

**Learning Material ↔ Flashcard link documented:**
- Every Learning Material has a `flashcardDeckId` field
- Every deck has a `sourceMaterialId` back-reference
- Material Detail screen: Flashcards tab shows linked deck
- Deleting material prompts for linked flashcard deletion

**4 generation pipelines documented:**
1. Auto-generated from Learning Material (primary)
2. Direct AI prompt generation
3. Quiz mistake remedial cards (auto, no AI needed)
4. Manual card creation

**Remedial card flow:**
```
Quiz answer marked wrong
    ↓
Local: FlashcardEntity created (front=question, back=answer+explanation)
    ↓
state = "LEARNING" (high SM-2 priority)
    ↓
Added to linked deck
    ↓
Appears in next daily review queue
```

---

## KNOWLEDGE HUB

**Old name:** "Library" / "Knowledge Base"  
**New name:** "Knowledge Hub"

**Old structure:** Flat list of notes

**New structure:** Subject-grouped Learning Materials with linked resources

```
📐 Physics
  ├── Newton's Laws of Motion
  │   ├── Summary (AI)
  │   ├── 24 Flashcards
  │   └── 5 Quiz Questions
  │   [Study Cards] [Take Quiz] [Review Summary]
  │
  └── Thermodynamics
      ├── Summary (AI)
      ├── 18 Flashcards
      └── 0 Quiz Questions
      [Study Cards] [Generate Quiz] [Review Summary]
```

Students see all three representations of the same knowledge in one place.

---

## ARCHITECTURE CHANGES

**ARCHITECTURE_MAP.md v2.0:**
- Added full Learning Pipeline flowchart (mermaid)
- Added **Processing Locations Map** — table showing exactly which component runs each operation:
  - On-device (Android): OCR, SM-2, face detection, image compression, timer, blocker, quiz mistake cards
  - Cloud Function: PDF extraction, URL scraping, YouTube transcript, all AI operations
  - Firebase Storage: PDF files, scan images
- Added Knowledge Hub data relationship diagram
- Clarified Firestore path: `users/{uid}/notes/` (user-scoped, not root-level)

---

## AI MODEL DOCUMENTATION

**New assignments documented:**

| Feature | Provider | Model |
|---|---|---|
| AI Chat | Groq | `openai/gpt-oss-20b` |
| AI Chat (Groq fallback) | Groq | `qwen/qwen3.6-27b` |
| AI Chat (Cerebras failover) | Cerebras | `gpt-oss-120b` |
| Classification | Groq | `openai/gpt-oss-20b` |
| Note Summarization | Groq | `openai/gpt-oss-20b` |
| Flashcard Generation | Groq | `openai/gpt-oss-20b` (JSON schema mode) |
| Quiz Generation | Groq | `openai/gpt-oss-20b` (JSON schema mode) |
| Study Plan | Cerebras | `gpt-oss-120b` |
| Image Doubt | Groq | `openai/gpt-oss-120b` (vision) |
| Image Doubt (fallback) | Cerebras | `gemma-4-31b` |

**AI System Prompt Design Philosophy added** — behavior requirements for:
- AI Chat (contextual tutor)
- Summarization + Classification
- Flashcard Generation
- Quiz Generation

---

## CONTRADICTIONS REMOVED

| Contradiction | Resolution |
|---|---|
| "Student types full notes" vs "5 input methods" | Explicit: typing is last resort, not primary |
| "Select subject before import" vs "AI understands content" | AI-first inference; selection is confirmation only |
| Scan Notes described similarly to Image Doubt | Clearly separated: import tool vs problem-solving tool |
| "URL import works for any site" (implied) | Documented: NOT guaranteed; full error states listed |
| "AI Chat is a generic chat" | Redefined as contextual study tutor with context injection |
| "Quiz is an optional feature" | Promoted to first-class module; integrated with flashcards |
| "XP for quiz completion" | Only document XP if XP tracking actually exists |
| "Notes and flashcards are separate systems" | Explicitly linked: Material → Flashcards → Quiz → Mastery |

---

## CODE MODIFIED

```
NONE
```

---

## NEXT STEPS (NOT STARTED)

The following implementation work is identified but has NOT begun. Do not start without explicit approval:

1. **Subject Inference UI** — Subject Inference Confirmation screen (new Compose screen)
2. **Processing Pipeline Screen** — staged AI processing display (new Compose screen)
3. **Learning Material Detail redesign** — Summary/Key Concepts/Formulas/Flashcards/Quiz tabs
4. **Quiz Engine implementation** — Groq-based quiz generation + result screen with remedial card creation
5. **Add Learning Material picker** — new bottom sheet or screen replacing current "Add Note" flow
6. **Knowledge Hub UI** — subject-grouped material list replacing flat notes list
7. **Context injection in AI Chat** — server-side system prompt builder using material + mistakes context
8. **Image Doubt mini-chat** — follow-up thread below solution (not full screen redirect)

**STOP. Await implementation phase approval.**
