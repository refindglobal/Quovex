# Quovex — Complete UI/UX Specification

**Version:** 2.0 | **Date:** 2026-08-22  
**Tone:** Motivating, Direct, Premium (No overly casual "Oops/Uh oh" errors; we use "Focus lost", "Action required", "Ready").

> [!IMPORTANT]
> **Notes / Library is a Learning Material system, not a text editor.**
> The UI must communicate that Quovex reads your study material — it does not expect you to type it. Subject selection is a confirmation step, not a prerequisite.

---

## 1. 🧱 Atomic Component Library

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
- **Shadow:** Elevation 4dp, but rely heavily on border strokes for separation in Dark Mode.

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
  - **Tabs (5):** Home, Timer, Library, Community, Profile.
  - **Active State:** Icon filled, text `#00C896`.
  - **Inactive State:** Icon outlined, text `#8AAFA3`.

---

## 2. 📏 Spacing & Grid System

- **Screen Edges:** Always `24dp` horizontal padding (prevents content from touching bezels).
- **Vertical Gaps:**
  - `space_xs` (4dp): Between an icon and its label.
  - `space_sm` (8dp): Between a title and subtitle.
  - `space_md` (16dp): Between stacked cards in a list.
  - `space_lg` (24dp): Between major sections on a screen.
  - `space_xl` (32dp): Bottom padding above the Bottom Navigation Bar.

---

## 3. 📱 Screen Breakdown

### Section 1: Onboarding (7 Screens)
**Universal Layout:** Top logo, center content, persistent bottom "Next" button.

| Screen | Human Readable Copy / Text | Core UI Components |
|---|---|---|
| **1. Splash** | (No text, just Q logo fading in) | Logo SVG, Fade animation |
| **2. Welcome** | "Welcome to Quovex." / "The complete ecosystem for extreme focus." / Button: "Continue with Google" | Full screen illustration, Google Sign-In button |
| **3. Personal** | "Let's set up your profile." / "What should we call you?" | Avatar grid (12 options), TextField (Name) |
| **4. Exam** | "What are you preparing for?" / "Search exams..." | Search bar, Exam Grid (JEE, NEET, Class 10, etc.), DatePicker |
| **5. Subjects** | "Select your subjects." / "Assess your current level." | Chip group (Physics, Maths), Dropdown for level (1-5) |
| **6. Schedule** | "Commit to your daily goal." / "How many hours per day?" | Slider (1h-10h), Chip group (Morning/Evening) |
| **7. Permissions** | "Enable features for strict focus." / "Notifications", "Exact Alarms" | Permission toggle cards |
| **8. Ready** | "You're all set, [Name]." / "Target: JEE Advanced • 4 hours/day" / Button: "Enter Quovex" | Summary Card, Confetti Lottie |

### Section 2: Main Dashboard (Bottom Nav Root)
| Screen | Human Readable Copy / Text | Core UI Components |
|---|---|---|
| **9. Home** | "Good Evening, [Name]." / "🔥 14 Day Streak" / "Today's Goal: 2.5 / 4.0 hrs" | Progress Ring, Weekly Heatmap, "Jump Back In" card, Recent Flashcard due alert |
| **10. Timer** | "Ready to focus?" / "Select Subject" / Button: "Start Session" | Large circular time picker, Subject dropdown, Toggle "Strict Blocker" |
| **11. Library** | "Knowledge Hub" / Tabs: [Materials] [Flashcards] [Plans] | Sticky header tabs, Card list, FAB: "Add Material" |
| **12. Community** | "Study Rooms" / "Active now: 1,240 studying" | Search bar, Filter chips (JEE, Class 10), List of active rooms |
| **13. Profile** | "[Name]" / "Level 24 • 12,400 XP" / "Refer a friend, get Premium free." | Avatar header, Stat Grid, Settings list |

### Section 3: Study Session Flow
| Screen | Human Readable Copy / Text | Core UI Components |
|---|---|---|
| **14. Active Session** | "Physics - Thermodynamics" / [01:45:22] / "Stay focused." | Huge Monospace Timer, subtle breathing animation, Button: "End early" |
| **15. Blocked App** | "Focus Lost." / "You are in an active session." / Button: "Return to Quovex" | Fullscreen red/dark overlay, strict tone |
| **16. Session Summary**| "Session Complete." / "Duration: 2h 15m" / "Focus Score: 94%" | XP increment animation, Stat breakdown, "Done" button |

---

## 4. 📚 Learning Material System — Screens

> [!IMPORTANT]
> Every screen in this section must communicate the concept of **transformation**: Quovex takes raw material and transforms it into learning tools. The UI must NOT look or feel like a generic note-taking app.

### 4A. Add Learning Material Screen

**Purpose:** The student selects how to import study material. Subject is NOT required at this step.

**Header:** "Add Study Material"  
**Subheader:** "Quovex will read it and build your study tools."

**Primary Actions (large icon cards, 2-column grid):**

| Action | Icon | Title | Short Explanation |
|---|---|---|---|
| Scan Notes | 📷 `ic_camera_scan` | "Scan Handwritten Notes" | "Photograph your notebook pages" |
| Upload PDF | 📄 `ic_file_pdf` | "Upload PDF" | "From Drive, Downloads, or WhatsApp" |
| YouTube | 🎥 `ic_youtube` | "YouTube Lecture" | "Paste a lecture URL" |
| Web Link | 🌐 `ic_globe` | "Web / Article" | "Any Wikipedia, NCERT, or blog page" |
| Quick Note | ✏️ `ic_pencil` | "Quick Text" | "Short definitions or formulas only" |

> [!NOTE]
> "Quick Text" card must include the subtitle: *"For short inputs only — not for full chapters."* This is intentional design to set correct expectations.

---

### 4B. Processing Screen

**Purpose:** Shown while the backend/AI is processing the imported material.

**Header:** Processing stages are shown as a vertical stepper list. Each stage activates in sequence.

**Stages (only show stages that the backend actually performs for this input type):**

| Stage | Icon | Label | Shown For |
|---|---|---|---|
| Reading source | 🔍 | "Reading source" | PDF, URL, YouTube, Scan |
| Extracting content | 📤 | "Extracting content" | PDF, URL, YouTube |
| Recognizing text | 📖 | "Recognizing text" | Scan only |
| Identifying subject & topic | 🧠 | "Identifying subject & topic" | All types |
| Building summary | 📝 | "Building summary" | All types |
| Preparing flashcards | 🃏 | "Preparing flashcards" | All types |

> [!WARNING]
> Do NOT show a "Preparing quiz" stage unless the backend actually generates quiz questions in this step. Do not fabricate pipeline stages.

**Bottom:** Cancel button (ghost style). Processing state uses `anim_ai_thinking.json`.

---

### 4C. Subject Inference Confirmation

**Purpose:** After processing, Quovex presents its inference. Student confirms or corrects.

**Layout:**

```
┌────────────────────────────────────────────────┐
│  ✨  Quovex identified this as:                │
│                                                │
│     📚 Physics                                 │
│     📌 Newton's Laws of Motion                 │
│     🎯 Relevant to: JEE Advanced               │
│                                                │
│  [ ✓ Looks Right — Continue ]                  │
│  [ ✎ Change Subject / Topic ]                  │
└────────────────────────────────────────────────┘
```

**[Change Subject / Topic]** opens an inline form with:
- Subject dropdown (filtered to student's subjects)
- Topic text field
- Title text field

**The student is never blocked from proceeding.** If they skip the correction, the AI inference is used.

---

### 4D. Learning Material Detail Screen

**Purpose:** The structured view of a processed Learning Material. Organized around learning, not raw source.

**Header Section:**
```
[Back Arrow]     [Subject Chip: Physics]    [Options ⋮]
                 Newton's Laws of Motion
                 📄 PDF  •  Aug 22, 2026  •  5 pages
```

**Body (TabRow with 4 tabs):**

| Tab | Content |
|---|---|
| **Summary** | 3–5 paragraph AI summary, Markdown rendered |
| **Key Concepts** | Numbered list of key points, definitions, formulas (LaTeX rendered) |
| **Flashcards** | Card count + "Study Now" CTA; preview of first 3 cards |
| **Quiz** | Question count + "Take Quiz" CTA; quiz result history if taken |

**Bottom Action Bar (sticky):**

```
[ 🃏 Study Cards ]   [ 📝 Take Quiz ]   [ 💬 Ask AI ]
```

**"Ask AI" opens the AI Chat with context pre-loaded:**  
*"I'm reviewing Newton's Laws of Motion (Physics). Help me understand this."*

---

### 4E. Scan Notes Screen

**Purpose:** Camera capture interface for handwritten/printed study material.

**Header:** "Scan Study Material"  
**Subheader:** "Position the page within the frame."

**Layout:**
- Full-screen CameraX viewport with document edge detection overlay
- Page counter: "Page 1 of 10 max"
- Bottom bar:
  - [Thumbnail gallery] — tap to review captured pages
  - [Capture Button] — large center FAB
  - [Done Scanning] — right side, activates when ≥1 page captured

**After capture:**
- Show grid of scanned pages with checkmarks
- Allow delete/re-scan per page
- Button: "Process [N] Pages" → navigates to Processing Screen

---

### 4F. Image Doubt Solver Screen

> [!IMPORTANT]
> This screen is separate from the Scan Notes flow. It is a problem-solving tool, not a note import tool. The language must be different.

**Header:** "Solve a Problem"  
**Subheader:** "Show Quovex the problem you're stuck on."

**Layout:**
1. Image capture area (tap to take photo or pick from gallery)
2. Image preview after capture
3. Optional text: "Anything specific you want to know?" (TextField)
4. Subject chip row: Physics / Chemistry / Maths / Biology / Other (optional hint, not mandatory)
5. "Solve Step by Step" primary button

**After submit:**
- Loading: "Analyzing your problem..." with `anim_ai_thinking.json`
- Result screen:

```
┌──────────────────────────────────────┐
│ [Image preview thumbnail]            │
├──────────────────────────────────────┤
│ 🔍 Problem identified: Projectile    │
│    Motion — finding maximum height   │
├──────────────────────────────────────┤
│ 📚 Concept: Projectile Motion        │
│ ⚖️  Law: Kinematics equations        │
├──────────────────────────────────────┤
│ Step 1: Identify initial conditions  │
│ Step 2: Apply vy = u·sinθ - g·t     │
│ Step 3: At max height, vy = 0        │
│ Step 4: t = u·sinθ / g               │
│ Step 5: H = (u²·sin²θ) / (2g)       │
├──────────────────────────────────────┤
│ ✅ Final Answer: H = 20m             │
├──────────────────────────────────────┤
│ ⚠️  Common Mistake: Forgetting to    │
│    use sinθ for the vertical component│
├──────────────────────────────────────┤
│ [💬 Ask Follow-up]  [Save as Note]   │
│                     [Create Flashcard]│
└──────────────────────────────────────┘
```

**Ask Follow-up** opens a mini chat thread below the solution (not a full screen switch).

---

### 4G. Import Link / URL Screen

**Header:** "Import from the Web"

**Input:**
```
[  Paste URL here...                    ] [→]
```

**Validation states (shown inline below input field):**

| State | Message |
|---|---|
| Valid URL format | Green check: "URL looks valid" |
| Invalid URL | Error: "Please enter a valid URL (start with https://)" |
| Processing | "Fetching content from source…" |
| Unsupported source | Warning: "This site may not be extractable. We'll try our best." |
| No content found | Error: "Couldn't extract readable content from this URL." |
| YouTube detected | Info: "YouTube URL detected — extracting lecture transcript." |
| No transcript | Warning: "No transcript available for this video. Try a different lecture." |
| Backend failure | Error: "Extraction failed. Please try again or use a different source." |
| AI failure | Error: "AI summarization failed. Your raw content was saved." |
| Rate limit | Error: "Import limit reached for today. Upgrade to Premium." |

> [!NOTE]
> URL import is NOT guaranteed for every website. The UI must set realistic expectations. Do not show a generic "loading…" spinner with no error feedback.

**After successful extraction:** Proceeds to Processing Screen and then Subject Inference Confirmation.

---

### 4H. Knowledge Hub (Library Tab)

**Purpose:** The top-level view of all a student's learning material, organized by subject.

**Header:** "Knowledge Hub"

**Layout (per subject group):**

```
┌──────────────────────────────────────┐
│ 📐 Physics                           │
│                                      │
│  Newton's Laws of Motion             │
│  Summary · 24 Cards · 5 Questions   │
│  [Study Cards]  [Take Quiz]          │
│                                      │
│  Thermodynamics                      │
│  Summary · 18 Cards · 0 Questions   │
│  [Study Cards]  [Generate Quiz]      │
└──────────────────────────────────────┘
```

**Empty state:**  
Illustration: `ill_empty_library`  
Title: "Your Knowledge Hub is empty."  
Body: "Add your first study material and Quovex will build your learning tools."  
CTA: "Add Study Material" → Primary button

---

### 4I. AI Chat Screen

**Header:** "Study Tutor"  
**Context pill (when context is active):** `[📐 Physics · Newton's Laws]` — tappable to change context

**Input bar:**
- TextField: "Ask about your study material…"
- Send button
- Camera button → Image Doubt Solver

**Behavior notes (not visible to student):**
- When context is active, system prompt includes current subject, topic, and learning material key concepts
- Suggested chips refresh based on current topic
- Error state for rate limit: "You've reached your 10 query limit for today."
- Offline: "No connection. Showing cached answers for common questions."

---

### 4J. Daily Quiz Screen

**Header:** "Question [N] of 5"  
**Progress bar:** Linear, `#00C896` fill

**MCQ Card:**
```
┌───────────────────────────────────────┐
│ Q2. What does Newton's Second Law     │
│     state about force?                │
│                                       │
│  ○  Force equals mass times velocity  │
│  ●  Force equals mass times           │  ← selected
│     acceleration                      │
│  ○  Force equals weight               │
│  ○  Force is proportional to distance │
└───────────────────────────────────────┘
           [ Submit Answer ]
```

**Result after submit (same card):**
- Correct: Card turns green, check icon, brief explanation
- Incorrect: Card shows red, X icon, shows correct answer + explanation
- "Next Question" → advances progress bar

**Quiz Complete Screen:**
```
┌───────────────────────────────────────┐
│  ✅ Quiz Complete!                    │
│                                       │
│  Score: 4 / 5  (80%)                 │
│                                       │
│  Incorrect:                           │
│  → Newton's Third Law concept        │
│                                       │
│  📌 1 remedial card added to your    │
│     Physics deck for review.          │
│                                       │
│  [ Review Flashcards ]  [ Done ]      │
└───────────────────────────────────────┘
```

---

## 5. 🚦 UI State Management (Loading, Empty, Error)

### 5.1 Loading States
- **Avoid Spinners:** Do not use standard Android circular progress indicators.
- **Use Shimmer:** Subtle dark gray `#1C2B24` to `#2D4438` shimmer gradient block for loading lists.
- **AI Generating:** Use `anim_ai_thinking.json` (sparkle) next to "Analyzing…" or "Generating…" text.
- **Processing stages:** Use the Processing Screen (4B) for multi-step AI operations.

### 5.2 Empty States
Every list must have a designed empty state.
- **Illustration:** Center an SVG illustration (`ill_empty_notes`, `ill_empty_deck`).
- **Text:** SemiBold title + Regular body.
- **Call to Action (CTA):** A primary button directly under the text.

### 5.3 Error States
- **Inline Errors:** Red text (`#FF5252`) directly below a TextField or action area.
- **Full Screen Errors (e.g., No Internet):** Central icon (`ic_warning`), text: "Connection lost. Checking for signal…", Ghost button: "Retry".
- **AI Rate Limit:** "Query limit reached for today. Upgrade to Premium for unlimited access." with "View Plans" primary button.
- **URL extraction failure:** Clear inline error with specific reason (see 4G error states table).
- **No transcript:** Specific message — do not show generic error.
- **Backend failure:** Do not show raw error codes. Use human-readable messages.

### 5.4 Permission Rationale States
- Camera for Scan Notes: "Quovex needs camera access to photograph your study material."
- Camera for Image Doubt: "Quovex needs camera access to analyze your problem."
- Storage for PDF: "Quovex needs storage access to pick your PDF file."
- Each permission rationale must be shown BEFORE the system dialog.

---

## 6. 🧭 Screen Index

| Screen | Description | Route |
|---|---|---|
| Splash | Logo fade | `splash` |
| Welcome | Google Sign-In | `welcome` |
| Onboarding x7 | Personal, Exam, Subjects, Schedule, Permissions, Notifications, Ready | `onboarding/*` |
| Home | Dashboard | `home` |
| Timer | Focus timer setup | `timer` |
| Active Session | Running session | `session/active` |
| Session Summary | Post-session | `session/summary` |
| Library / Knowledge Hub | All materials by subject | `library` |
| Add Learning Material | Input method picker | `library/add` |
| Processing | AI processing pipeline | `library/processing` |
| Subject Inference | Confirm/correct inferred subject | `library/classify` |
| Learning Material Detail | Summary/Cards/Quiz tabs | `library/{materialId}` |
| Edit Material Metadata | Correct subject/topic/title | `library/{materialId}/edit` |
| Scan Notes | Camera capture | `library/scan` |
| Image Doubt Solver | Problem photo + AI solution | `ai/image-doubt` |
| Import URL | Web/YouTube link import | `library/import-url` |
| AI Chat | Contextual study tutor | `ai/chat` |
| Flashcards | Deck list | `flashcards` |
| Deck Overview | Cards + SM-2 queue | `flashcards/{deckId}` |
| Flashcard Study | Active review session | `flashcards/{deckId}/study` |
| Deck Complete | Review complete | `flashcards/{deckId}/complete` |
| Daily Quiz | 5-question quiz | `quiz/daily` |
| Topic Quiz | Material-specific quiz | `quiz/{materialId}` |
| Quiz Result | Score + mistakes | `quiz/result` |
| AI Study Planner | Generate + view plan | `ai/planner` |
| Analytics | Session history + heatmap | `analytics` |
| Leaderboard | Weekly rankings | `social/leaderboard` |
| Study Rooms | Room list | `social/rooms` |
| Room Detail | Live room view | `social/rooms/{roomId}` |
| My Profile | User profile | `profile/me` |
| User Profile | Other user | `profile/{uid}` |
| Settings | App settings | `settings` |
| Premium | Subscription paywall | `premium` |
| Subscription Success | Post-purchase | `premium/success` |
| Focus Music | Audio player | `music` |
| Referrals | Referral code share | `referrals` |
