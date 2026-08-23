# Quovex — Complete UI/UX Specification

**Version:** 3.1 | **Date:** 2026-08-22  
**Tone:** Motivating, Direct, Premium (No overly casual "Oops/Uh oh" errors; we use "Focus lost", "Action required", "Ready").

> [!IMPORTANT]
> **Knowledge Hub is a Unified Content Ecosystem, not a generic text editor.**
> The UI clearly distinguishes between **Official Resources (NCERT)**, **Quovex Originals**, and **My Materials**, while enabling all three to flow into the unified Quovex learning loops (Summary, Flashcards, Quiz, AI Tutor, Mastery).

---

## 1. Atomic Component Library

### 1.1 Buttons (`QuovexButton`)
| Variant | Style | Usage | Height |
|---|---|---|---|
| **Primary** | Solid `#00C896`, Text `#000000` | Main actions (Start Session, Next, Save) | 48dp |
| **Secondary** | Outline `#2D4438`, Text `#E8F5F0` | Alternative actions (Skip, Cancel) | 48dp |
| **Ghost** | No bg, Text `#00C896` | Low priority (Read more, Edit) | 40dp |
| **Danger** | Solid `#FF5252`, Text `#FFFFFF` | Destructive (Delete Account, End Session) | 48dp |
**Border Radius:** 16dp for standard buttons, 100dp for Floating Action Buttons (FAB).

### 1.2 Cards & Elevated Surfaces (`QuovexCard`)
- **Background:** `#111917` (Surface color against `#0A0F0D` app background).
- **Border:** Subtle stroke of `#2D4438` (1dp width).
- **Corner Radius:** 24dp for large containers (Home widgets), 16dp for standard cards (Learning Material list).
- **Shadow:** Elevation 4dp, relying heavily on border strokes for crisp separation in Dark Mode.

### 1.3 Inputs & Forms (`QuovexTextField`)
- **Idle State:** Background `#1C2B24`, Border Transparent.
- **Focused State:** Background `#1C2B24`, Border `#00C896` (2dp).
- **Text:** `#E8F5F0` (Input), `#8AAFA3` (Placeholder).
- **Corner Radius:** 16dp.

### 1.4 Global App Bars
- **TopAppBar (`QuovexTopAppBar`):**
  - **Back Arrow:** Phosphor `ic_caret_left` (24dp, Stroke 2dp). ALWAYS identical across the app.
  - **Title:** Centered, Inter SemiBold, 18sp.
  - **Background:** Transparent `#0A0F0D`.
- **BottomNavBar (`QuovexBottomNav`):**
  - **Tabs (5):** Home, Timer, Hub, Community, Profile.
  - **Active State:** Icon filled, text `#00C896`.
  - **Inactive State:** Icon outlined, text `#8AAFA3`.

---

## 2. Spacing & Grid System

- **Screen Edges:** Always `24dp` horizontal padding (prevents content from touching bezels).
- **Vertical Gaps:**
  - `space_xs` (4dp): Between an icon and its label.
  - `space_sm` (8dp): Between a title and subtitle.
  - `space_md` (16dp): Between stacked cards in a list.
  - `space_lg` (24dp): Between major sections on a screen.
  - `space_xl` (32dp): Bottom padding above the Bottom Navigation Bar.

---

## 3. Screen Breakdown

### Section 1: Onboarding (8 Screens)
**Universal Layout:** Top logo, center content, persistent bottom "Next" button.

| Screen | Human Readable Copy / Text | Core UI Components |
|---|---|---|
| **1. Splash** | (Logo fading in) | Logo SVG, Fade animation |
| **2. Welcome** | "Welcome to Quovex." / "The complete ecosystem for extreme focus." / Button: "Continue with Google" | Full screen illustration, Google Sign-In button, Guest Bypass footer |
| **3. Personal** | "Let's set up your profile." / "What should we call you?" | Avatar grid (12 options), TextField (Name) |
| **4. Exam** | "What are you preparing for?" / "Search exams..." | Search bar, Exam Grid (JEE, NEET, Class 10, etc.), DatePicker |
| **5. Subjects** | "Select your subjects." / "Assess your current level." | Chip group (Physics, Maths), Dropdown for level (1-5) |
| **6. Schedule** | "Commit to your daily goal." / "How many hours per day?" | Slider (1h-10h), Chip group (Morning/Evening) |
| **7. Permissions** | "Enable features for strict focus." / "Notifications", "Exact Alarms" | Permission toggle cards |
| **8. Ready** | "You're all set, [Name]." / "Target: JEE Advanced • 4 hours/day" / Button: "Enter Quovex" | Summary Card, Confetti Lottie |

### Section 2: Main Dashboard (Bottom Nav Root)
| Screen | Human Readable Copy / Text | Core UI Components |
|---|---|---|
| **9. Home** | "Good Evening, [Name]." / "🔥 14 Day Streak" / "Today's Goal: 2.5 / 4.0 hrs" | Progress Ring, Weekly Heatmap, "Jump Back In" card, Quick Action cards (AI Doubt Tutor, AI Note Parser, Knowledge Base) |
| **10. Timer** | "Ready to focus?" / "Select Subject" / Button: "Start Session" | Large circular time picker, Subject dropdown, Toggle "Strict Blocker" |
| **11. Knowledge Hub** | "Knowledge Hub" / Tabs: [Official Resources] [Quovex Originals] [My Materials] | Top ecosystem selector, horizontal subject chips, Material/Book cards, FAB: "Add Material" |
| **12. Community** | "Study Rooms" / "Active now: 1,240 studying" | Search bar, Filter chips (JEE, Class 10), List of active rooms |
| **13. Profile** | "[Name]" / "Level 24 • 12,400 XP" / "Refer a friend, get Premium free." | Avatar header, Stat Grid, Settings list |

### Section 3: Study Session Flow
| Screen | Human Readable Copy / Text | Core UI Components |
|---|---|---|
| **14. Active Session** | "Physics - Thermodynamics" / [01:45:22] / "Stay focused." | Huge Monospace Timer, subtle breathing animation, Button: "End early" |
| **15. Blocked App** | "Focus Lost." / "You are in an active session." / Button: "Return to Quovex" | Fullscreen red/dark overlay, strict tone |
| **16. Session Summary**| "Session Complete." / "Duration: 2h 15m" / "Focus Score: 94%" | XP increment animation, Stat breakdown, "Done" button |

---

## 4. Learning Material & Content Ecosystem Screens

### 4A. Add Learning Material Screen
**Purpose:** Multi-modal intake selector. Subject is NOT required at this step.
- Scan Handwritten Notes (CameraX + ML Kit OCR)
- Upload PDF Document (Document Picker)
- Import Web / YouTube Lecture (URL extraction)
- Quick Text Input (Definitions, formulas, key snippets)

### 4B. AI Processing Screen
**Purpose:** Multi-stage animated progress during server-side understanding.
- "Quovex AI is analyzing and classifying your material..."
- Steps: Reading content → Inferring subject & topic → Structuring concepts

### 4C. Subject Inference Confirmation Screen
**Header:** "Confirm Classification"  
**Subheader:** "Quovex AI inferred the following subject and topic."  
- Confidence match badge (e.g. `✦ 94% Match`)
- Inferred Subject (e.g. Physics)
- Inferred Topic (e.g. Newton's Laws of Motion)
- Inferred Title (Editable TextField)
- Buttons: `[ Confirm & Save ]` (Primary) and `[ Change Subject ]` (Secondary)

### 4D. Learning Material Detail Screen
**Header:** Material Title + Subject Tag  
**Tabs (4):**
1. **Summary Tab:** AI concise synthesis + Exam relevance
2. **Key Concepts Tab:** Concept cards with mathematical formula rendering ($F = ma$, $x²$, $\sqrt{x}$)
3. **Flashcards Tab:** Spaced repetition deck linked to material with `[ Study Deck ]` CTA
4. **Quiz Tab:** 5-question active recall practice with `[ Take Quiz ]` CTA

### 4E. Image Doubt Solver Screen
**Purpose:** Photo problem-solving tutoring interface (distinct from Document Scanner).
- Camera capture / gallery picker
- Step-by-step reasoning breakdown (Problem Identification → Relevant Law → Step-by-Step Derivation → Final Answer → Common Mistakes)
- Follow-up question mini-thread

### 4F. Knowledge Hub (Unified Content Ecosystem)
**Header:** "Knowledge Hub"  
**Subheader:** "Study materials, concepts & active recall"

**Top Ecosystem Switcher:**
- `[ Official Resources (NCERT) ]` (`PLANNED / NOT YET IMPLEMENTED`): Browse Class 9–12 → Subjects → Books → Chapters. Actions: `[ Read Official NCERT ]` (opens official portal URL) and `[ Study with Quovex AI ]` (generates full active study assets).
- `[ Quovex Originals ]` (`PLANNED / NOT YET IMPLEMENTED`): High-yield, multi-agent reasoned books with chapter breakdowns, worked examples, flashcards, and quizzes.
- `[ My Materials ]` (`IMPLEMENTED v3.0`): User imported materials with subject filter chips, "Needs Processing" legacy badges, and instant learning tabs.

### 4G. AI Chat Screen (AI Doubt Tutor)
- Top bar badge: `✦ Quovex AI`
- Subject filter chips: `Physics`, `Chemistry`, `Maths`, `Biology`
- Context-aware system prompt injecting active material summary and recent quiz mistakes
- LaTeX / Unicode mathematical notation ($x^2 \to x²$, roots $\sqrt{x}$, Greek symbols $\theta, \alpha, \beta$, chemistry $\text{H}_2\text{O}$)

### 4H. Practice Quiz & Quiz Result Screen
- 5-question MCQ interface with instant feedback
- Score breakdown (Score, Accuracy %, Concepts to Review)
- `[ Create Remedial Flashcards ]` CTA automatically generating cards for missed concepts

---

## 5. Screen Route Index

| Screen | Route | Implementation Status |
|---|---|---|
| Splash | `splash` | Implemented |
| Welcome / Auth | `auth` | Implemented |
| Onboarding Wizard | `onboarding` | Implemented |
| Dashboard / Home | `dashboard` | Implemented |
| Focus Timer | `timer` | Implemented |
| Knowledge Hub | `knowledge_hub` | Implemented (My Materials live; NCERT/Originals planned) |
| Add Learning Material | `add_material` | Implemented |
| Processing | `processing` | Implemented |
| Subject Inference | `inference` | Implemented |
| Material Detail | `material_detail/{materialId}` | Implemented |
| Image Doubt Solver | `image_doubt` | Implemented |
| AI Doubt Tutor | `ai_chat` | Implemented |
| Flashcard Deck Overview | `deck_overview/{deckId}` | Implemented |
| Flashcard Study | `flashcard_study/{deckId}` | Implemented |
| Quiz Screen | `quiz/{materialId}` | Implemented |
| Quiz Result | `quiz_result/{materialId}` | Implemented |
| NCERT Chapter Browser | `official/ncert/{classId}/{subjectId}` | Planned |
| Quovex Original Reader | `originals/{bookId}/{chapterId}` | Planned |
