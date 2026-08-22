# Quovex — Project File Structure

## Complete Android Project Layout

```
Quovex APP/
│
├── docs/
│   ├── PRD.md                          ← Product Requirements Document
│   ├── PROJECT_FILES.md                ← This file
│   └── CHANGELOG.md                   ← Version history
│
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json            ← Firebase config (DO NOT commit)
│   │
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           ├── java/com/quovex/
│           │   │
│           │   ├── QuovexApp.kt                ← Application class, Hilt setup
│           │   │
│           │   ├── di/                         ← Dependency Injection (Hilt)
│           │   │   ├── AppModule.kt
│           │   │   ├── NetworkModule.kt
│           │   │   ├── DatabaseModule.kt
│           │   │   └── AIModule.kt
│           │   │
│           │   ├── data/
│           │   │   ├── local/
│           │   │   │   ├── db/
│           │   │   │   │   ├── QuovexDatabase.kt
│           │   │   │   │   ├── dao/
│           │   │   │   │   │   ├── SessionDao.kt
│           │   │   │   │   │   ├── SubjectDao.kt
│           │   │   │   │   │   ├── ChatDao.kt
│           │   │   │   │   │   └── StudyPlanDao.kt
│           │   │   │   │   └── entity/
│           │   │   │   │       ├── SessionEntity.kt
│           │   │   │   │       ├── SubjectEntity.kt
│           │   │   │   │       ├── ChatMessageEntity.kt
│           │   │   │   │       └── StudyPlanEntity.kt
│           │   │   │   └── prefs/
│           │   │   │       └── UserPreferences.kt      ← DataStore
│           │   │   │
│           │   │   ├── remote/
│           │   │   │   ├── api/
│           │   │   │   │   ├── GroqApiService.kt
│           │   │   │   │   ├── CerebrasApiService.kt
│           │   │   │   │   └── FirebaseService.kt
│           │   │   │   └── dto/
│           │   │   │       ├── GroqRequest.kt
│           │   │   │       ├── GroqResponse.kt
│           │   │   │       ├── CerebrasRequest.kt
│           │   │   │       └── CerebrasResponse.kt
│           │   │   │
│           │   │   └── repository/
│           │   │       ├── SessionRepositoryImpl.kt
│           │   │       ├── AIRepositoryImpl.kt
│           │   │       ├── StudyPlanRepositoryImpl.kt
│           │   │       └── UserRepositoryImpl.kt
│           │   │
│           │   ├── domain/
│           │   │   ├── model/
│           │   │   │   ├── Session.kt
│           │   │   │   ├── Subject.kt
│           │   │   │   ├── ChatMessage.kt
│           │   │   │   ├── StudyPlan.kt
│           │   │   │   └── User.kt
│           │   │   │
│           │   │   ├── repository/
│           │   │   │   ├── SessionRepository.kt        ← interfaces
│           │   │   │   ├── AIRepository.kt
│           │   │   │   ├── StudyPlanRepository.kt
│           │   │   │   └── UserRepository.kt
│           │   │   │
│           │   │   └── usecase/
│           │   │       ├── timer/
│           │   │       │   ├── StartSessionUseCase.kt
│           │   │       │   ├── EndSessionUseCase.kt
│           │   │       │   └── GetSessionHistoryUseCase.kt
│           │   │       ├── ai/
│           │   │       │   ├── SendMessageUseCase.kt
│           │   │       │   ├── GenerateStudyPlanUseCase.kt
│           │   │       │   └── GetMotivationalQuoteUseCase.kt
│           │   │       ├── analytics/
│           │   │       │   ├── GetDashboardStatsUseCase.kt
│           │   │       │   └── GetProductivityCurveUseCase.kt
│           │   │       └── blocker/
│           │   │           ├── GetInstalledAppsUseCase.kt
│           │   │           └── ManageBlocklistUseCase.kt
│           │   │
│           │   ├── presentation/
│           │   │   │
│           │   │   ├── MainActivity.kt
│           │   │   ├── navigation/
│           │   │   │   ├── QuovexNavGraph.kt
│           │   │   │   └── Screen.kt               ← sealed class of routes
│           │   │   │
│           │   │   ├── theme/
│           │   │   │   ├── Color.kt
│           │   │   │   ├── Theme.kt
│           │   │   │   ├── Type.kt
│           │   │   │   └── Shape.kt
│           │   │   │
│           │   │   ├── components/                 ← Reusable Compose UI
│           │   │   │   ├── QuovexButton.kt
│           │   │   │   ├── TimerRing.kt
│           │   │   │   ├── StatCard.kt
│           │   │   │   ├── HeatmapCalendar.kt
│           │   │   │   └── ChatBubble.kt
│           │   │   │
│           │   │   ├── onboarding/
│           │   │   │   ├── OnboardingScreen.kt
│           │   │   │   └── OnboardingViewModel.kt
│           │   │   │
│           │   │   ├── home/
│           │   │   │   ├── HomeScreen.kt
│           │   │   │   └── HomeViewModel.kt
│           │   │   │
│           │   │   ├── timer/
│           │   │   │   ├── TimerScreen.kt
│           │   │   │   └── TimerViewModel.kt
│           │   │   │
│           │   │   ├── blocker/
│           │   │   │   ├── BlockerScreen.kt
│           │   │   │   ├── BlockerViewModel.kt
│           │   │   │   └── BlockedOverlayActivity.kt
│           │   │   │
│           │   │   ├── ai/
│           │   │   │   ├── chat/
│           │   │   │   │   ├── ChatScreen.kt
│           │   │   │   │   └── ChatViewModel.kt
│           │   │   │   └── planner/
│           │   │   │       ├── PlannerScreen.kt
│           │   │   │       └── PlannerViewModel.kt
│           │   │   │
│           │   │   ├── analytics/
│           │   │   │   ├── AnalyticsScreen.kt
│           │   │   │   └── AnalyticsViewModel.kt
│           │   │   │
│           │   │   ├── social/
│           │   │   │   ├── leaderboard/
│           │   │   │   │   ├── LeaderboardScreen.kt
│           │   │   │   │   └── LeaderboardViewModel.kt
│           │   │   │   └── profile/
│           │   │   │       ├── ProfileScreen.kt
│           │   │   │       └── ProfileViewModel.kt
│           │   │   │
│           │   │   └── settings/
│           │   │       ├── SettingsScreen.kt
│           │   │       └── SettingsViewModel.kt
│           │   │
│           │   ├── service/
│           │   │   ├── TimerForegroundService.kt   ← Keeps timer alive
│           │   │   ├── BlockerAccessibilityService.kt
│           │   │   ├── FocusDetectionService.kt    ← ML Kit camera service
│           │   │   └── VpnBlockerService.kt        ← Website blocker
│           │   │
│           │   └── worker/
│           │       ├── ReminderWorker.kt           ← Daily study reminder
│           │       ├── StreakCheckWorker.kt         ← Streak protection
│           │       └── QuoteWorker.kt              ← AI motivational quote
│           │
│           └── res/
│               ├── drawable/
│               ├── font/
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml/
│                   └── accessibility_service_config.xml
│
├── build.gradle.kts                    ← Root build file
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

---

## Key Files Description

| File | Purpose |
|---|---|
| `QuovexApp.kt` | Hilt application class, initializes Firebase & logging |
| `QuovexNavGraph.kt` | Central navigation graph for all screens |
| `QuovexDatabase.kt` | Room DB with all DAOs and entities |
| `AIModule.kt` | Provides Groq & Cerebras Retrofit clients |
| `TimerForegroundService.kt` | Runs timer in background, prevents kill |
| `BlockerAccessibilityService.kt` | Core distraction blocking engine |
| `FocusDetectionService.kt` | Camera-based drowsiness detection |
| `UserPreferences.kt` | DataStore for all user settings |
| `Screen.kt` | Sealed class defining all navigation routes |

---

## Files to NEVER Commit

Add to `.gitignore`:
```
google-services.json
local.properties
*.keystore
secrets.properties          ← API keys for Groq & Cerebras
```

---

## Secrets File (`secrets.properties`)

```properties
GROQ_API_KEY=your_groq_api_key_here
CEREBRAS_API_KEY=your_cerebras_api_key_here
FIREBASE_PROJECT_ID=your_firebase_project_id
```
