# Quovex — Phase 3 Home Dashboard Audit

**Phase:** Phase 3 — Production Home Dashboard  
**Date:** August 22, 2026  
**Auditor:** Quovex Lead Android Engineer  
**Status:** Audit Complete  

---

## 1. Audit of Existing Home Dashboard

### A. Current Dashboard UI (`DashboardScreen.kt`)
- **Header:** Displays user greeting and avatar, but greeting was hardcoded ("Good Evening," without time-of-day calculation). User name displayed from preferences.
- **Daily Target Ring:** Displayed hardcoded/calculated progress percent, but did not handle edge cases like "goal exceeded", "no goal configured", or "0 hours".
- **Primary CTA:** Only had a Floating Action Button for timer. Did not have a prominent in-flow "Start Focus" CTA or "Continue Focus" active session awareness.
- **Jump Back In:** Checked first deck from Room, but if empty had a generic card instead of a dedicated "Start your first study session" CTA.
- **Weekly Heatmap:** Displayed a hardcoded list of booleans (`listOf(true, true, true, true, true, false, false)`) rather than querying real session logs from Room for Monday through Sunday.
- **Flashcard Reminder:** Lacked a dedicated global Flashcard Due Reminder card across decks.
- **Study Tools / Quick Actions:** Present (AI Note Parser and AI Doubt Tutor).
- **Recent Activity:** Missing recent session history list.

### B. Current ViewModel (`DashboardViewModel.kt`)
- `DashboardViewModel` exposed a basic `DashboardUiState` containing:
  - `userProfile` (from `UserPreferencesManager`)
  - `stats: DashboardStats` (today focus minutes, target minutes, progress percent, streak, xp)
  - `activeDeckId`, `activeDeckTitle`, `activeDeckSubject`
  - `isLoading: Boolean`
- Lacked:
  - Time-of-day dynamic greeting calculation
  - Active session state awareness
  - Weekly Monday–Sunday real study minutes aggregation
  - Real global due flashcard count across Room flashcards
  - Recent activity session feed
  - Error and offline state management

### C. Current Use Cases & Repository
- **`GetDashboardStatsUseCase.kt`:**
  - Computed `todayFocusMinutes`, `targetMinutes`, `progressPercent`, `streakDays`, `totalXp`.
  - Did not compute day-by-day weekly breakdown (Mon–Sun).
  - Did not compute global due flashcards count or active focus status.
- **`QuovexRepository.kt` & `QuovexRepositoryImpl.kt`:**
  - Has `getTodayFocusSeconds()`, `getTotalXp()`, `getDecks()`, `getDueFlashcards()`, `getRecentSessions()`.
  - Missing:
    - `getWeeklyStudyMinutes(startOfWeekMillis: Long): Map<DayOfWeek, Int>`
    - `getTotalDueFlashcardsCount(currentTimeMillis: Long): Int`
    - `getMostRecentDeck(): DeckEntity?`
    - `getActiveSession(): SessionEntity?` / Session tracking flow

### D. Current Room Entities & DAOs (`QuovexDao.kt`)
- `QuovexDao` has:
  - `getTotalStudyMinutesSince(startTime: Long): Int?`
  - `getAllSessions(): Flow<List<SessionEntity>>`
  - `getDueFlashcardsFlow(deckId: Int, currentTimeMillis: Long): Flow<List<FlashcardEntity>>`
- Needs additions in `QuovexDao`:
  - Query for `getSessionsBetween(startTime: Long, endTime: Long): List<SessionEntity>`
  - Query for `getTotalDueFlashcardsCount(currentTimeMillis: Long): Int`
  - Query for `getMostRecentDueDeck(currentTimeMillis: Long): DeckEntity?`

---

## 2. Gap Analysis & Requirements for Target UI

| Feature Requirement | Current Status | Remediation Plan |
|---|---|---|
| **Dynamic Greeting** | Hardcoded "Good Evening" | Implement `TimeOfDayHelper` / greeting logic (Morning, Afternoon, Evening) based on device clock. |
| **Real User Avatar & Streak** | Functional with fallback | Connect directly to `userProfile.avatarId` (1..12) and `userProfile.streakDays`. |
| **Today's Goal Progress** | Basic calculation | Support 0h, partial, 100% complete, exceeded (>100%), and unconfigured goal. |
| **Primary "Start Focus" CTA** | FAB only | Create high-priority "Start Focus" button; switch to "Continue Focus" if session running. |
| **Active Session Tracking** | Local to `TimerViewModel` | Introduce singleton `SessionStateManager` or repository-backed active session flow. |
| **Jump Back In** | First deck or empty | Show most recently accessed deck with due cards/mastery; if none, show "Start your first study session" CTA. |
| **Weekly Progress (Mon–Sun)** | Hardcoded mock booleans | Query Room for all sessions this week (Monday 00:00 to Sunday 23:59) and compute daily minutes & completion intensity. |
| **Flashcard Due Reminder** | Missing on Home | Add Room query for total due cards today and display card with "Review Now" or "You're all caught up." |
| **Recent Activity Section** | Missing | Query Room for latest 3 completed sessions and display duration, date, and focus score. |
| **Zero Mock Data** | Partial mock booleans in weekly view | Eradicate all mock booleans/numbers; rely 100% on Room/Preferences with intentional empty states. |
| **States** | Loading only | Support Loading, Normal, No Activity Empty State, Error State, and Offline indicators. |

---

## 3. Architecture Execution Plan

1. **Data Layer Updates:**
   - Update `QuovexDao.kt` with queries for weekly sessions, global due flashcards count, and latest active deck.
   - Update `QuovexRepository.kt` & `QuovexRepositoryImpl.kt` with corresponding methods.
   - Create `SessionStateManager.kt` (or active session tracking in `QuovexRepository`) so active timer state is globally observable.
2. **Domain Layer Updates:**
   - Create domain models for `WeeklyDayProgress`, `RecentActivityItem`, `DueFlashcardsSummary`, `JumpBackInItem`.
   - Update `GetDashboardStatsUseCase.kt` to aggregate all real dashboard metrics.
3. **Presentation Layer Updates:**
   - Update `DashboardViewModel.kt` with unified `DashboardUiState`.
   - Redesign `DashboardScreen.kt` using `QuovexTheme`, `QuovexCard`, `QuovexButton`, `QuovexChip`, `QuovexSectionHeader`, `QuovexStates` following the information architecture in STEP 2.
4. **Unit Testing & Verification:**
   - Add unit tests for `GetDashboardStatsUseCase` and `DashboardViewModel` covering all 10 required test scenarios.
   - Compile with `./gradlew clean testDebugUnitTest assembleDebug` and verify 100% pass.
