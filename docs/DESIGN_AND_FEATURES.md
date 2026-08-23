# Quovex — Design System & Feature Specs

**Version:** 2.1 | **Date:** 2026-08-25

> [!IMPORTANT]
> Quovex is a **Learning Transformation System**, not a notebook app. The Knowledge Hub is where students manage Learning Materials — not notes. Design language throughout the app must reinforce this concept.

---

## 1. 🎨 Design System

### 1.1 Theme
- **Default:** **Dark mode** — Quovex is dark-first by design
- **User override:** Can switch to light mode manually in Settings
- **Dark mode:** Deep dark backgrounds, vibrant emerald accents (primary experience)
- **Light mode:** Clean white/grey backgrounds, same accents

---

### 1.2 Color Palette

**Primary Brand:** Emerald Green — represents growth, progress, success

#### Dark Mode Colors
| Token | Hex | Usage |
|---|---|---|
| `primary` | `#00C896` | Buttons, active states, streak flame, CTA |
| `primaryVariant` | `#00A87A` | Pressed/darker primary |
| `primaryContainer` | `#003D2E` | Chips, tag backgrounds |
| `onPrimary` | `#000000` | Text on primary buttons |
| `secondary` | `#34D399` | Secondary actions, icons |
| `background` | `#0A0F0D` | Main app background |
| `surface` | `#111917` | Cards, bottom sheets, dialogs |
| `surfaceVariant` | `#1C2B24` | Input fields, elevated cards |
| `outline` | `#2D4438` | Borders, dividers |
| `onBackground` | `#E8F5F0` | Primary text |
| `onSurface` | `#C4DDD5` | Secondary text |
| `onSurfaceVariant` | `#8AAFA3` | Placeholder, caption text |
| `error` | `#FF5252` | Errors, warnings |
| `success` | `#00C896` | Same as primary |
| `warning` | `#FFB800` | Streak rescue, cautions |
| `xp` | `#FFD700` | XP gold, badges, coins |

#### Light Mode Colors
| Token | Hex | Usage |
|---|---|---|
| `primary` | `#00915F` | Buttons, active states |
| `primaryVariant` | `#007A50` | Pressed state |
| `primaryContainer` | `#D6F5EB` | Chips, tag backgrounds |
| `onPrimary` | `#FFFFFF` | Text on primary buttons |
| `background` | `#F5FAF8` | Main app background |
| `surface` | `#FFFFFF` | Cards |
| `surfaceVariant` | `#EBF5F0` | Input fields, secondary cards |
| `outline` | `#B2D4C8` | Borders, dividers |
| `onBackground` | `#0A1A14` | Primary text |
| `onSurface` | `#1C3A2D` | Secondary text |
| `onSurfaceVariant` | `#4A7A64` | Placeholder, caption |
| `error` | `#D32F2F` | Errors |

#### Semantic Colors (Both Modes)
| Token | Purpose |
|---|---|
| `timerActive` | `#00C896` — timer ring fill |
| `streakFire` | `#FF6B35` — streak flame |
| `focusScore` | Green gradient: `#00C896` → `#FFD700` → `#FF5252` |
| `premium` | `#FFD700` — gold for premium badge |
| `quovexAiBadge` | `#00C896` — Quovex AI Emerald Branding |

---

### 1.3 Typography

**Font:** [Inter](https://fonts.google.com/specimen/Inter) (Google Fonts — free)

```kotlin
// Type.kt
val QuovexTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)
```

**Typography Rules:**
- Headings: `SemiBold` / `Bold` — never Thin or Light
- Body text: `Regular` — no italic in UI (only in AI responses)
- Numbers/stats: `Bold` with tabular figures for alignment
- Timer digits: Custom large `Bold` with monospace rendering

---

### 1.4 Shapes & Radius

```kotlin
// Shape.kt
val QuovexShapes = Shapes(
    small = RoundedCornerShape(8.dp),       // chips, tags, small buttons
    medium = RoundedCornerShape(16.dp),     // cards, inputs, dialogs
    large = RoundedCornerShape(24.dp),      // bottom sheets, large cards
    extraLarge = RoundedCornerShape(32.dp)  // full-round FABs, avatar rings
)
```

---

### 1.5 Spacing System (8dp grid)

| Token | Value | Usage |
|---|---|---|
| `space_xs` | 4dp | Icon padding |
| `space_sm` | 8dp | Inner element gap |
| `space_md` | 16dp | Standard padding |
| `space_lg` | 24dp | Section spacing |
| `space_xl` | 32dp | Screen padding top |
| `space_xxl` | 48dp | Large section gaps |

---

### 1.6 App Assets Required

| Asset | Type | Where Used |
|---|---|---|
| `ic_launcher.png` | App icon (adaptive) | Home screen |
| `ic_quovex_logo.svg` | Logo (text + icon) | Splash, onboarding, settings |
| `ic_quovex_mark.svg` | Icon only (no text) | Notification icon, small spaces |
| `avatar_1.png` → `avatar_12.png` | 12 preset avatars | Onboarding avatar picker |
| `badge_*.png` (×20) | Achievement badges | Profile, milestones |
| `exam_*.png` (×15) | Exam type icons | Onboarding exam selection |
| `ic_timer.svg` | Timer icon | Bottom nav |
| `ic_ai.svg` | AI sparkle icon | Bottom nav |
| `ic_stats.svg` | Stats/chart icon | Bottom nav |
| `anim_confetti.json` | Lottie animation | Milestone celebrations |
| `anim_streak.json` | Lottie animation | Streak fire animation |
| `anim_focus.json` | Lottie animation | Session start countdown |
| `anim_ai_thinking.json` | Lottie animation | AI loading state |
| `onboarding_*.png` (×3) | Illustration | Welcome/onboarding screens |

**Asset Sources (free):**
- Icons: [Phosphor Icons](https://phosphoricons.com) (MIT license)
- Avatars: AI-generated (use `generate_image` tool)
- Lottie animations: [LottieFiles.com](https://lottiefiles.com) (free library)
- Illustrations: [Storyset.com](https://storyset.com) (free with attribution)

---

### 1.7 App Icon Concept

- **Shape:** Rounded square (standard Android adaptive icon)
- **Background:** Deep dark green `#0A1F16`
- **Symbol:** Stylized letter "Q" formed by a circular timer ring with a lightning bolt through it — representing focus + speed
- **Foreground color:** `#00C896` (brand primary)

---

## 2. 🏫 Student Classification System

### The Problem
A 5th grader studying Maths is in a completely different world from a JEE aspirant studying Calculus. The system must classify students so:
- Study Rooms show relevant peers
- Leaderboards are fair (5th grader vs 5th grader)
- AI generates age/level-appropriate content
- Flashcard difficulty scales correctly

### Classification Model: 3-Layer System

```
Layer 1: STAGE          → Who are they? (broad category)
Layer 2: EXAM/GOAL      → What are they studying for?
Layer 3: AI LEVEL       → How good are they? (dynamically assessed)
```

#### Layer 1 — Stage (Selected during Onboarding)

| Stage ID | Label | Ages | Includes |
|---|---|---|---|
| `primary` | Primary School | 9–11 | Class 4, 5, 6 |
| `middle` | Middle School | 11–14 | Class 7, 8, 9 |
| `secondary` | Secondary / High School | 14–17 | Class 10, 11 |
| `competitive` | Competitive Exam Prep | 16–24 | JEE, NEET, UPSC, SAT, etc. |
| `college` | College / University | 18–25 | UG, PG coursework |
| `professional` | Professional / Upskilling | 22+ | IELTS, GMAT, certifications |

#### Layer 2 — Exam/Goal (Selected during Onboarding)

Each Stage has a pre-set exam list:

| Stage | Available Exams |
|---|---|
| Primary | School exams (Olympiad, general) |
| Middle | School boards, Math Olympiad, Science Olympiad |
| Secondary | CBSE Class 10, ICSE, State boards, NTSE |
| Competitive | JEE Main/Advanced, NEET, UPSC CSE, CA Foundation, GATE, NDA, SAT, ACT, A-Levels, IELTS, TOEFL |
| College | Semester exams (custom subjects), GATE, GRE, GMAT, MCAT, LSAT |
| Professional | IELTS, TOEFL, PTE, AWS/GCP certs, CFA, ACCA |

#### Layer 3 — AI Level Assessment (Dynamic, ongoing)

After onboarding, Quovex runs a **3-minute subject assessment quiz** (optional, but recommended):
- 10 adaptive MCQs per subject
- Difficulty auto-adjusts based on answers (binary search algorithm)
- AI (Groq) generates questions from the syllabus for that exam
- Result: **Level 1–5 per subject**

| Level | Label | Description |
|---|---|---|
| 1 | Beginner | Knows basics, needs fundamentals |
| 2 | Elementary | Has foundation, gaps in concepts |
| 3 | Intermediate | Understands most topics, needs practice |
| 4 | Advanced | Strong concepts, needs exam-level speed |
| 5 | Expert | Ready for exam, needs mock tests only |

**Ongoing re-assessment:**
- Level recalculates every 2 weeks based on: quiz scores + flashcard accuracy + session completion
- User can manually request re-assessment anytime

### Classification in Action

```
Student: "Priya"
Stage: Competitive
Exam: JEE Advanced 2027
Subjects: Physics (Level 2), Maths (Level 3), Chemistry (Level 1)

→ Study Room: "JEE 2027 Aspirants"
→ Leaderboard: "JEE 2027" leaderboard only
→ AI Chat context: JEE-level questions, Physics fundamentals focus
→ Flashcard difficulty: Level 2 cards for Physics, Level 1 for Chemistry
→ Study Plan: Extra time on Chemistry (weakest), revision mode for Maths
```

```
Student: "Aarav"
Stage: Primary
Exam: Class 6 School Exams
Subjects: Maths (Level 3), Science (Level 2)

→ Study Room: "Class 6 Students"
→ Leaderboard: "Primary School" leaderboard only
→ AI Chat: Age-appropriate language, simpler explanations
→ No JEE/NEET content ever shown to this user
```

---

## 3. 🏠 Study Rooms — Full Spec

### Architecture Decision: Hybrid Model (Best + Most Cost-Optimized)

| Type | Access | Created By | Cost |
|---|---|---|---|
| **Public Exam Rooms** | Free users | System (auto-created) | Firestore listeners only — very cheap |
| **Private Friend Rooms** | Premium users | Users | Same cost, fewer participants |

**Why Hybrid is best:**
- No empty room problem (public rooms always have people)
- No spam/abuse from user-created public rooms
- Students instantly find peers matching their exam
- Private rooms give premium users extra value

---

### Public Room Auto-Creation

System auto-creates rooms for every active exam + stage combo:

```
"JEE 2027 — Aspirants"        → for Stage=competitive, Exam=JEE, Year=2027
"NEET 2027 — Aspirants"       → for Stage=competitive, Exam=NEET
"Class 10 Boards"              → for Stage=secondary, Exam=Class10
"Class 6 — School Prep"        → for Stage=primary
"SAT 2026"                     → for Stage=competitive, Exam=SAT
"IELTS Prep"                   → for Stage=professional, Exam=IELTS
... (auto-generated from exam catalog)
```

**Room is shown to user only if:**
- Their `stage` + `exam` matches the room
- Room has at least 1 participant currently studying (avoids empty ghost rooms)

---

### Room Interaction Features

| Feature | Free | Premium | How It Works |
|---|---|---|---|
| Join public exam room | ✅ | ✅ | Auto-matched to their exam room |
| See others' timers | ✅ | ✅ | Firestore real-time listener |
| Emoji reactions | ✅ | ✅ | Firestore write: `{emoji, userId, timestamp}` |
| Daily study challenge | ✅ | ✅ | "Who studies the most today?" — auto-ranked |
| Create private room | ❌ | ✅ | Firestore doc created by user |
| Invite friends to private room | ❌ | ✅ | Share invite link / deep link |

**No text chat** — keeps it focused, reduces moderation burden, keeps data costs near zero.

---

### Room UI Layout

```
┌──────────────────────────────────────────────────────────┐
│  🏠 JEE 2027 — Aspirants            247 studying now 🟢  │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  [Avatar] Arjun          ⏱ 32:14  Physics     🔥        │
│  [Avatar] Priya          ⏱ 18:45  Chemistry   💪        │
│  [Avatar] Rahul          ⏱ 49:02  Maths       ✨        │
│  [Avatar] Sara           ⏱ 05:31  Physics     🎯        │
│  ... +243 more studying                                  │
│                                                          │
├──────────────────────────────────────────────────────────┤
│  🏆 Today's Challenge: Most hours wins                   │
│  1. Rahul — 4h 12m                                       │
│  2. Priya — 3h 45m                                       │
│  3. You — 2h 10m  (#3)                                   │
├──────────────────────────────────────────────────────────┤
│  React:  👍  🔥  💪  🎯  ✨   (tap to send to room)     │
│                                                          │
│  [ Join Session → Start Timer ]                          │
└──────────────────────────────────────────────────────────┘
```

---

### Firestore Data Structure (Cost-Optimized)

**Key insight:** Don't store all participants in one big document — use subcollections + presence pattern.

```
study_rooms/
└── {roomId}/                           ← one doc per room
    ├── name: "JEE 2027 Aspirants"
    ├── examType: "JEE"
    ├── stage: "competitive"
    ├── isPublic: true
    ├── createdBy: "system" | "userId"
    ├── activeCount: 247               ← updated every 60s (not real-time per user)
    │
    └── participants/                  ← subcollection
        └── {userId}/
            ├── name: "Arjun"
            ├── avatarId: 3
            ├── subject: "Physics"
            ├── timerSeconds: 1934     ← updated every 30s only (NOT every second!)
            ├── isActive: true
            ├── lastSeen: timestamp
            └── todayHours: 4.2
```

**Cost optimization tricks:**
- Timer updates to Firestore every **30 seconds** only (not every second — that's 1000x cheaper)
- On-device: timer counts down locally (no lag, smooth UI)
- Emoji reactions: written to a `reactions` subcollection, TTL 5 min auto-delete via Cloud Function
- `activeCount` updated by Cloud Function every 60s (not per join/leave event)
- Users removed from room after `lastSeen` > 5 minutes (Cloud Function cleanup)

---

## 4. 🎁 Referral System

### Rewards Structure

| Action | Referrer Gets | Referred Friend Gets |
|---|---|---|
| Friend installs + signs up | 7 days Premium free | 7 days Premium free |
| 5 referrals total | 1 month Premium free | — |
| 10 referrals total | 3 months Premium free | — |
| 25 referrals total | Lifetime Premium | — |

**Rules:**
- Friend must complete onboarding (not just install) to trigger reward
- Referral tracked via unique referral code (5-char alphanumeric, e.g. `ARJUN3`)
- No self-referral (device fingerprinting check)
- Rewards stack — 5 referrals = 1 month on top of any existing premium

---

### Referral Code Generation
```
Format: First 5 chars of name (uppercase) + 2 random digits
Example: ARJUN38, PRIYA91, RAHUL52
Unique check: Firestore query to ensure no duplicate
```

---

### Shareable Referral Card (Viral Feature)

When user taps "Share Quovex", they get a **dynamically generated image card** showing:

```
┌────────────────────────────────────────────┐
│  ⚡ QUOVEX                                  │
│                                            │
│  "Arjun has studied 142 hours on Quovex"  │
│                                            │
│  🔥 Streak: 23 days                        │
│  📚 Exam: JEE 2027 • Level: Intermediate   │
│  🏆 Rank: #47 this week                    │
│                                            │
│  Join me and get 7 days Premium FREE!      │
│  Use code: ARJUN38                         │
│                                            │
│  [Download Quovex]  play.google.com/...    │
└────────────────────────────────────────────┘
```

**Generated using:** Android Canvas API → Bitmap → share as image
**Shareable via:** WhatsApp, Instagram Stories, Twitter/X, copy link

---

### Firestore Referral Schema
```
referrals/{referralCode}:
{
  code: "ARJUN38",
  ownerId: "uid_arjun",
  totalReferrals: 3,
  rewardedMonths: 0,
  referredUsers: ["uid_1", "uid_2", "uid_3"],
  createdAt: timestamp
}

users/{uid}:
{
  ...
  referralCode: "ARJUN38",          ← user's own code
  referredBy: "PRIYA91" | null,     ← code used when they signed up
  referralRewardDays: 21            ← total earned days
}
```

---

## 5. 🖼️ UI Component Library (Key Components)

### QuovexTopAppBar (Global Standard)
```
Every child screen (non-root) MUST use this standard Top App Bar.
Back Arrow: Phosphor Icon `ic_caret_left` (Size: 24dp, Stroke: 2dp).
Consistency Rule: The back arrow is identical across the entire project. NEVER use different arrow styles (e.g., no mixing standard Material arrows with custom ones).
```

### QuovexButton
```
Variants: Primary (filled) | Secondary (outlined) | Ghost (text only)
States: Default | Hovered | Pressed | Disabled | Loading (spinner)
Sizes: Small (32dp) | Medium (44dp) | Large (56dp)
```

### StatCard
```
Used on: Home, Analytics, Profile
Contains: Icon | Value (large bold) | Label | Trend (↑↓)
```

### TimerRing
```
Circular progress ring
Color: primaryGreen fill → empties as time runs out
Center: Large countdown digits (monospace)
Pulse animation when active
```

### FocusScoreBadge
```
Score 0-100 shown as colored badge
0-40: Red | 40-70: Amber | 70-90: Green | 90-100: Gold
```

### AvatarWithLevel
```
Avatar circle + level badge in bottom-right corner
Green ring when user is currently studying
```

### HeatmapCalendar
```
GitHub-style contribution calendar
Color: Transparent (no study) → Light green → Dark green (heavy study)
Tap a day: shows that day's session summary
```
