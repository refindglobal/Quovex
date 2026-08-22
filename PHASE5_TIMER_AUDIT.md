# PHASE 5 — FOCUS ENGINE & STRICT FOCUS AUDIT

**Date:** 2026-08-22  
**Status:** Pre-implementation Audit

---

## 1. CURRENT TIMER STATE MODEL

### Components Involved
- **`TimerScreen.kt`**: Single composable screen attempting to handle both timer setup and active session simultaneously.
- **`TimerViewModel.kt`**: Owns the timer loop in memory via `viewModelScope.launch { while(...) { delay(1000L); remainingSeconds-- } }`.
- **`SessionStateManager.kt`**: `@Singleton` in-memory state holder exposing `activeSession: StateFlow<ActiveSessionState>`. Holds `isActive`, `remainingSeconds`, `totalSeconds`, `subject`.
- **`ActiveSessionState`**: In `DashboardModels.kt`. Does not hold `startedAtMillis`, `endTimeMillis`, `mode`, or `strictFocusEnabled`.

### Critical Architecture Flaws
1. **Timer lives in ViewModel scope:** When the user navigates away or the app process is paused/recreated, the coroutine can stall, drift, or die.
2. **Relative decrement vs Absolute timestamp:** The countdown uses `remainingSeconds - 1` in a `delay(1000L)` loop. Frame skips and OS coroutine throttling cause severe timer drift when backgrounded.
3. **Dual responsibility on TimerScreen:** Setup UI and active countdown are mingled in one screen without a clear state machine or distinct active/summary screens.

---

## 2. CURRENT TIMER CALCULATIONS

- **Setup Selection:** Static list of minute options: `[15, 25, 45, 60, 90]`.
- **Calculation:** `totalSeconds = minutes * 60`; `remainingSeconds = remainingSeconds - 1`.
- **Formatting:** `formattedTime` formats `remainingSeconds` as `"%02d:%02d"`. If duration exceeds 60 minutes, minutes are displayed as `90:00` instead of standard `01:30:00` (HH:MM:SS).
- **Time Drift:** Coroutine `delay(1000)` does not account for execution overhead or Android doze mode.

---

## 3. SERVICE LIFECYCLE & NOTIFICATION BEHAVIOR

- **`TimerForegroundService` Status:** **MISSING**. There is currently no `Service` or `ForegroundService` implementation in the project.
- **Permissions:** `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE` are declared in `AndroidManifest.xml`, but no `<service>` tag is registered for the timer.
- **Notification Channel:** No notification channel created for the timer; no ongoing notification posted when the session starts.
- **Background Execution:** If the user leaves the app, Android will kill the backgrounded Activity/ViewModel, terminating the focus session.

---

## 4. PERSISTENCE & SESSION COMPLETION

- **Session Recording:**
  - Calls `RecordSessionUseCase` on finish or end early: `startTime`, `endTime`, `durationMinutes`, `focusScore = 95` (hardcoded!), `appBlockViolations = 0`.
  - Persists to Room `sessions` table (`SessionEntity`).
- **XP Calculation:**
  - Fabricated calculation: `val xp = minutes * 5` in ViewModel, displaying `"Great focus! +$xp XP added"`. This violates the zero fabricated XP rule.
- **Completion UI:**
  - Displays a text message inline on the setup screen instead of transitioning to a dedicated `SessionSummaryScreen`.

---

## 5. PAUSE / RESUME BEHAVIOR

- **Current State:** `toggleTimer()` calls `pauseTimer()` which sets `isRunning = false` and cancels `timerJob`.
- **Product Requirement:** Product spec does not define user-facing pause for deep work (strict focus / flow state). Pausing can be an internal state, but deep work sessions are either Running, Cancelled, or Completed.

---

## 6. APP-KILL & PROCESS RECREATION BEHAVIOR

- **Process Death:** If the Android OS terminates the process while a session is active, all timer state is lost because:
  1. No foreground service is running to keep the process prioritized.
  2. `SessionStateManager` is purely in-memory with no persistent timestamp restoration (`endTimeMillis`).
  3. No `SharedPreferences` / `DataStore` backup of the active session's `endTimeMillis` exists.

---

## 7. CURRENT STRICT FOCUS & BLOCKER FOUNDATION

- **Current Switch:** `TimerScreen.kt` has a visual switch for "Strict App Blocker" with label "Blocks YouTube & social apps".
- **Implementation:** Simply toggles a boolean in `TimerUiState`. Does not integrate with any blocker engine or tracking.
- **Violation Logging:** Hardcoded to `appBlockViolations = 0`.

---

## 8. MISSING REQUIREMENTS FOR PRODUCTION FOCUS ENGINE

| Area | Current State | Required Phase 5 State |
|---|---|---|
| Timer Modes | Hardcoded minutes (15, 25, 45, 60, 90) | Presets: Pomodoro (25/5), Deep Work (50/10), Long Deep Work (90/20), Custom (custom focus + break) |
| Time Computation | Decrement loop with drift | Derived from absolute `endTimeMillis - System.currentTimeMillis()` |
| Timer Service | None | `TimerForegroundService` with persistent ongoing notification & action intent |
| Notification | None | Ongoing notification with title, subject, formatted remaining time, and return action |
| State Machine | `isRunning: Boolean` | `Idle`, `Setup`, `Running`, `Completed`, `Cancelled` |
| Active Session UI | Setup & Active merged in 1 screen | Dedicated Active Session screen with breathing animation, dominant timer, Strict Focus badge, "End Early" confirmation dialog |
| Session Summary | Text string on setup screen | Dedicated `SessionSummaryScreen` with duration, subject, strict focus outcome (zero fake XP, zero fake focus score) |
| Subject Selection | Hardcoded `"Physics: Thermodynamics"` | Real configured subject selector from decks/profile |
| Audio & Haptics | None | System audio/haptic triggers on session complete and countdown |
| Time Formatting | MM:SS only | HH:MM:SS for >= 60 mins, MM:SS for < 60 mins, clamp at 00:00 |
| Home Sync | Partial | Observes `SessionStateManager` with instant navigation to active session |
| Unit Tests | 3 tests in DashboardViewModelTest | Comprehensive deterministic tests with fake clock covering all presets, modes, timestamps, lifecycle |
