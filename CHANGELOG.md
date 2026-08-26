# Quovex — Changelog

All notable changes to the Quovex Android platform and ecosystem are documented in this file.

## [Unreleased] - 2026-08-26

### Added — Phase 23: Visual Problem Solver 2.0 & Interactive Mini-Chat (Module B3: L-030 to L-037)

#### Data & Domain Architecture
- **Domain Models (`com.quovex.domain.model`):**
  - Enhanced [`ImageDoubtModels.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/ImageDoubtModels.kt) with `StructuredDoubtSolution` (Problem Summary, Core Concept, Step-by-Step Derivation, Formula Items with LaTeX, Final Answer, Common Mistakes/Traps) and `DoubtFollowUpMessage` for mini-chat conversation turns.
  - Implemented resilient `toStructured(subject)` markdown section parser handling bulleted formulas, numbered steps, and exam pitfall blocks.
- **Use Cases (`com.quovex.domain.usecase`):**
  - Created [`AskDoubtFollowUpUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/AskDoubtFollowUpUseCase.kt) allowing students to query the AI visual tutor with conversational context and problem image memory preserved across multiple turns.
  - Upgraded [`SolveImageDoubtUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/SolveImageDoubtUseCase.kt) with comprehensive validation and structured parsing unit tests.

#### Presentation & UI
- **`ImageDoubtViewModel.kt` (`com.quovex.ui.ai`):**
  - Manages structured 6-tier visual solutions, conversational mini-chat thread state, 1-tap Knowledge Hub `LearningMaterial` ingestion via `QuovexRepository.insertMaterial`, and atomic SM-2 spaced repetition flashcard synthesis.
- **`ImageDoubtScreen.kt` (`com.quovex.ui.ai`):**
  - Rich Compose screen featuring:
    - 6-tier structured pedagogical breakdown: Problem Summary, Core Concept & Governing Law, Numbered Reasoning Steps with LaTeX math, Key Formulas, Final Answer Card, and Collapsible Common Mistakes & Exam Pitfalls.
    - Interactive Mini-Chat thread with quick prompt chips (*"Can we solve this using an alternate method?"*, *"Explain Step 2 in more detail"*), user/tutor message bubbles, and real-time query bar.
    - One-tap actions: `[Save to Materials]` and `[Create Flashcards]` with instant feedback.

#### Testing & Quality Assurance
- Added unit tests passing 100%:
  - `SolveImageDoubtUseCaseTest.kt` (tests empty input rejection, vision AI solution retrieval, and 6-tier structured markdown parsing)
  - `AskDoubtFollowUpUseCaseTest.kt` (tests blank input rejection and multi-turn contextual prompt construction)
  - `ImageDoubtViewModelTest.kt` (tests subject loading, query mutation, follow-up messages, Material insertion, and flashcard synthesis)
- Verified all 360 unit tests passing via `./gradlew testDebugUnitTest`.
- Assembled and validated debug APK via `./gradlew assembleDebug`.

---

### Added — Phase 22: Learning Material Ingestion Engine, ML Kit Note Scanner & AI Topic Inference (Module B: L-001, L-010 to L-016, L-020 to L-026)

#### Domain & Presentation Architecture
- **Use Cases (`com.quovex.domain.usecase`):**
  - [`SynthesizeLearningMaterialUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/SynthesizeLearningMaterialUseCase.kt): Transforms raw note/OCR text into structured `LearningMaterial`, auto-generates SM-2 flashcards in `FlashcardDao`, and saves diagnostic quiz questions into `QuizDao`.
  - [`InferNoteMetadataUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/InferNoteMetadataUseCase.kt): AI-first classification pipeline predicting subject, chapter/topic, subtopic, and exam relevance.
- **UI & Presentation (`com.quovex.ui.material`):**
  - `MaterialViewModel.kt`: Real-time AI classification state, multi-modal ingestion pipeline, and synthesis coordination.
  - `InferredMetadataConfirmationCard.kt`: Interactive confirmation card with confidence matching pill, exam relevance tags, and `[Confirm]` / `[Change]` actions.
  - `AddMaterialScreen.kt`: Multi-modal import hub (Camera Scan, PDF Document, YouTube Lecture, Web Article, Quick Text).
- Verified full test suite passing and debug APK build.

---

### Added — Phase 21: Streak Protection System, Streak Cemetery & Milestone Celebration Badges (Module F1: F-111, F-112, F-113, F-114)

#### Data & Architecture
- **Room Database & Schema Extension v5 (`com.quovex.data.local`):**
  - Created `StreakEntity` (`streaks_cemetery` table) recording past broken streak history, duration, date ranges, causes of death, and student post-mortem reflection notes.
  - Created `StreakDao` with methods `insertStreak()`, `getAllBrokenStreaks()`, `getStreakById()`, `updateReflection()`, `getLongestCemeteryStreak()`, and `getTotalCemeteryCount()`.
  - Added Room database migration `MIGRATION_4_5` and updated `AppDatabase` version to 5.
  - Extended `UserPreferencesManager` with `rescueTokens` preference management.
- **Domain Models & UseCases (`com.quovex.domain`):**
  - Added [`StreakModels.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/StreakModels.kt) (`StreakStatus`, `CemeteryTombstone`, `StreakMilestone`, `StreakProtectionResult`).
  - Unified [`StreakInfo.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/StreakInfo.kt) with milestone progress calculation and protection status telemetry.
  - Added [`StreakRepository.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/repository/StreakRepository.kt) domain repository contract.
  - Added [`ManageStreakUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/ManageStreakUseCase.kt) evaluating current streak health, milestone progression, and rescue token deduction.
  - Added [`LogStreakReflectionUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/LogStreakReflectionUseCase.kt) allowing students to add introspective reflection notes to cemetery tombstones.
- **Data Implementation (`com.quovex.data.repository`):**
  - Created [`StreakRepositoryImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/repository/StreakRepositoryImpl.kt) aggregating dynamic Room telemetry from `StreakDao`, `UserStatsDao`, `SessionDao`, and `UserPreferencesManager`.
  - Bound in Hilt [`RepositoryModule.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/di/RepositoryModule.kt).

#### Presentation & UI
- **`StreakViewModel.kt` (`com.quovex.ui.streak`):**
  - Manages live streak metrics, rescue token usage, broken streak cemetery log, post-mortem reflection dialogues, and milestone achievement status.
- **`StreakScreen.kt` (`com.quovex.ui.streak`):**
  - Comprehensive screen with:
    - Glowing flame header with current streak count and next milestone target badge.
    - Streak Protection Card displaying available Rescue Tokens with one-tap shield activation.
    - Milestone Progression Roadmap showcasing 7-Day Flame (🔥), 30-Day Master (⚡), 100-Day Legend (🏆), and 365-Day Grandmaster (👑) milestones.
    - Streak Cemetery section with tombstone cards, cause of death chips, and interactive reflection modal dialogues.
    - Dynamic anti-Duolingo motivational quotes emphasizing consistency over guilt.
- **Dashboard & Navigation:**
  - Connected Streak Chip on `DashboardScreen.kt` directly to `QuovexRoute.Streak`.
  - Wired `QuovexRoute.Streak` in `QuovexRoutes.kt` and `QuovexNavGraph.kt`.

#### Testing & Quality Assurance
- Added unit test suites passing 100%:
  - `ManageStreakUseCaseTest.kt` (computes live streak, verifies milestone progress, deducts rescue tokens)
  - `LogStreakReflectionUseCaseTest.kt` (saves and updates reflection notes on tombstone records in Room DB)
  - `StreakViewModelTest.kt` (tests StateFlow emission, rescue token trigger, and reflection logging)
- Verified all unit test suites passing 100% via `./gradlew testDebugUnitTest`.
- Assembled and validated debug APK via `./gradlew assembleDebug`.

---

### Added — Phase 20: Spaced Repetition Remedial Mistake Auto-Queueing & Daily Diagnostic Quiz Engine (Module C: Q-001, Q-009, F-067)

#### Data & Architecture
- **Room Database & Schema Extension (`com.quovex.data.local`):**
  - Extended `QuizMistakeEntity` with `remedialCardId: Long?` to permanently map student quiz misconceptions to generated remedial flashcards.
  - Extended `QuizDao` with `getUnremediedMistakes()`, `getMistakesForConcept()`, and `updateRemedialCardId()`.
  - Added `FlashcardEntity` and `FlashcardDao` with SM-2 Spaced Repetition parameters (`intervalDays`, `easeFactor`, `repetitionNumber`, `isRemedial`, `conceptTag`, `frontContent`, `backContent`).
  - Implemented Room Database Migration (`MIGRATION_3_4`) and upgraded database version to 4.
- **Domain Models & Repository Contracts (`com.quovex.domain`):**
  - Added [`DailyDiagnosticModels.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/DailyDiagnosticModels.kt) (`DiagnosticQuestion`, `DiagnosticQuizRequest`, `DailyTopicContext`, `RemedialCardSynthesis`).
  - Added [`DiagnosticQuizRepository.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/repository/DiagnosticQuizRepository.kt) domain interface.
  - Added [`GenerateDailyDiagnosticQuizUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/GenerateDailyDiagnosticQuizUseCase.kt) aggregating real-time Room study sessions from today and invoking AI model `gpt-oss-20b` to generate a personalized 5-question diagnostic quiz.
  - Added [`SynthesizeRemedialFlashcardsUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/SynthesizeRemedialFlashcardsUseCase.kt) transforming quiz mistakes into targeted remedial flashcards and scheduling them into the user's SM-2 review queue with interval 1 day and ease factor 2.0.
- **Data Implementation & AI Integration (`com.quovex.data.repository`):**
  - [`DiagnosticQuizRepositoryImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/repository/DiagnosticQuizRepositoryImpl.kt): Direct integration with Groq AI API (`gpt-oss-20b`) for structured JSON quiz generation and misconception synthesis.
  - Bound in Hilt [`RepositoryModule.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/di/RepositoryModule.kt).

#### Presentation & UI
- **`DailyDiagnosticQuizViewModel.kt` (`com.quovex.ui.quiz`):**
  - Manages quiz loading, question transitions, instant option feedback (correct/incorrect styling with detailed rationale), quiz submission, XP reward awards (+100 XP base, +20 XP per correct question), Room DB recording, and automatic remedial flashcard synthesis.
- **`DailyDiagnosticQuizScreen.kt` (`com.quovex.ui.quiz`):**
  - Rich Compose screen with animated progress bar, question timer, concept pills, selectable options, immediate rationale breakdown cards, final score celebration dialogs, and remedial card queue confirmation.
- **Dashboard & Navigation:**
  - Added "Today's Daily Diagnostic Quiz" CTA card on `DashboardScreen.kt`.
  - Added `QuovexRoute.DailyDiagnosticQuiz` in `QuovexRoutes.kt` and `QuovexNavGraph.kt`.

#### Testing & Quality Assurance
- Added unit test suites passing 100%:
  - `GenerateDailyDiagnosticQuizUseCaseTest.kt` (gathers real-time sessions and validates 5-question daily diagnostic generation)
  - `SynthesizeRemedialFlashcardsUseCaseTest.kt` (synthesizes remedial flashcards with SM-2 interval 1, inserts to Room DB, and updates mistake entity records)
  - `DailyDiagnosticQuizViewModelTest.kt` (tests option selection, answer validation, score calculation, and remedial trigger execution)
- Verified all unit test suites passing 100% via `./gradlew testDebugUnitTest`.
- Verified live APK deployment and end-to-end integration on `emulator-5554`.

---

### Added — Phase 19: Advanced Performance Analytics Center & Weekly PDF Report Exporter (Module E1 & E2: F-093, F-094, F-101, F-102)

#### Data & Architecture
- **Domain Models & Generator Interface (`com.quovex.domain`):**
  - Added [`AnalyticsModels.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/AnalyticsModels.kt) (`HourlyProductivity`, `PerformanceInsights`, `StudyReportData`).
  - Added [`PdfReportGenerator.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/manager/PdfReportGenerator.kt) domain interface for structured PDF rendering.
  - Added [`GenerateWeeklyPdfReportUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/GenerateWeeklyPdfReportUseCase.kt) aggregating user profile, streak, real subject time, 24h hourly curve, and AI coach recommendations.
  - Extended [`StudyAnalyticsUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/StudyAnalyticsUseCase.kt) with `getHourlyProductivity()` (24-hour peak curve) and `getPerformanceInsights()` (best study day, peak focus window, distraction resistance rate, dynamic telemetry insights).
- **Data Implementation & Native PDF Engine (`com.quovex.data.analytics`):**
  - [`PdfReportGeneratorImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/analytics/PdfReportGeneratorImpl.kt): Native Android `PdfDocument` / `Canvas` renderer producing standard A4 printable PDF documents styled with Quovex dark charcoal (`#0A0F0D`) and emerald green (`#00C896`) branding, KPI metric grid, subject progress bars, AI study coach analysis, and footer metadata.
  - Added [`filepaths.xml`](file:///d:/Quovex%20APP/android/app/src/main/res/xml/filepaths.xml) and declared `androidx.core.content.FileProvider` (`com.quovex.fileprovider`) in [`AndroidManifest.xml`](file:///d:/Quovex%20APP/android/app/src/main/AndroidManifest.xml) for secure file sharing.
  - Bound in Hilt [`RepositoryModule.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/di/RepositoryModule.kt).

#### Presentation & UI
- **`AnalyticsViewModel.kt` (`com.quovex.ui.analytics`):**
  - Exposes `hourlyProductivity`, `subjectBreakdown`, `performanceInsights`, `pdfExportState`, and `userEntitlement` state flows.
- **`AnalyticsScreen.kt` & `DashboardScreen.kt` (`com.quovex.ui`):**
  - Created dedicated `AnalyticsScreen` featuring:
    - AI Cognitive Coach Insights Card with dynamic advice and best study day/window telemetry.
    - 24-Hour Productivity Curve interactive bar chart identifying peak study hours.
    - Subject Time & Mastery distribution bars.
    - Executive Study Analytics PDF Export card with native Android Share Sheet integration.
  - Added "Study Analytics & Performance" CTA card on Dashboard.
  - Added `QuovexRoute.Analytics` in `QuovexRoutes.kt` and `QuovexNavGraph.kt`.

#### Testing & Quality Assurance
- Added unit test suites passing 100%:
  - `StudyAnalyticsUseCaseTest.kt` (5 tests — heatmap grid, subject breakdown, exam countdown, 24h hourly productivity aggregation, dynamic performance insights)
  - `GenerateWeeklyPdfReportUseCaseTest.kt` (assembles telemetry, verifies calculations and PDF generator invocation)
  - `AnalyticsViewModelTest.kt` (loads telemetry, handles PDF export success and failure states)
- Verified all unit test suites passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK deployment and Android Share Sheet PDF export on `emulator-5554`.

---

### Added — Phase 18: YouTube & Web URL Intelligent Study Material Importer (Module B0: L-003 & L-004)

#### Data & Architecture
- **Domain Models & Repository (`com.quovex.domain`):**
  - Added [`ExtractedContent.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/ExtractedContent.kt) (`title`, `content`, `inputType`, `sourceUrl`, `authorOrChannel`, `durationSeconds`).
  - Added [`ContentExtractionRepository.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/repository/ContentExtractionRepository.kt) domain contract for web and YouTube extraction.
  - Added [`ExtractUrlContentUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/ExtractUrlContentUseCase.kt) validating input URL schemas and routing to appropriate content extractors.
- **Extraction Engine & Repository Implementation (`com.quovex.data.repository`):**
  - [`ContentExtractionRepositoryImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/repository/ContentExtractionRepositoryImpl.kt):
    - **Web Article Parser (L-004)**: Real-time HTML downloader using OkHttp, strips non-content tags (`<script>`, `<style>`, `<nav>`, `<footer>`, `<header>`, `<aside>`), decodes full HTML character entities, and builds structured markdown documents with title, source headers, and paragraph blocks.
    - **YouTube Lecture Parser (L-003)**: Extracts video IDs from multiple YouTube URL formats (`watch?v=`, `youtu.be/`, `shorts/`, `embed/`), queries oEmbed metadata for lecture title/creator, fetches timed text XML captions/subtitles, and groups continuous speech into formatted 45-second lecture blocks with `[mm:ss]` timestamp cues.
    - **Captions Fallback**: Automatically creates a syllabus study briefing if a video has disabled captions.
  - Bound in Hilt [`RepositoryModule.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/di/RepositoryModule.kt).

#### Presentation & UI
- **`MaterialViewModel.kt` (`com.quovex.ui.material`):**
  - Added `importUrlContent(url: String, inputType: NoteInputType)` method that triggers live progress state, downloads & formats the study material, and routes to `SubjectInferenceScreen` and AI summary/quiz generation.
- **`ImportUrlScreen.kt` & `QuovexNavGraph.kt`:**
  - Integrated state transitions connecting URL extraction to AI Subject Inference, Key Concepts, Flashcard Generation, and Practice Quizzes.

#### Testing & Quality Assurance
- Added unit test suites passing 100%:
  - `ContentExtractionRepositoryTest.kt` (4 unit tests — YouTube URL parser across 5 formats, HTML title extraction & entity decoding, HTML body tag stripping, timed text XML timestamp grouping)
  - `ExtractUrlContentUseCaseTest.kt` (3 unit tests — invalid URL validation, web article delegation, YouTube delegation)
  - `MaterialViewModelTest.kt` (3 unit tests — initial idle state, URL extraction success to Inferred, URL extraction network error handling)
- Total: All 327 unit test suites passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK deployment on Android emulator (`emulator-5554`) via `./gradlew installDebug`.

---

### Added — Phase 17: Google AdMob Monetization Engine & Rewarded AI Credits (Module 10A)

#### Data & Architecture
- **Dependencies & Manifest:**
  - Added Google Mobile Ads SDK (`playServicesAds = "23.3.0"`, `com.google.android.gms:play-services-ads:23.3.0`).
  - Added Google AdMob test application ID meta-data (`ca-app-pub-3940256099942544~3347511713`) in [`AndroidManifest.xml`](file:///d:/Quovex%20APP/android/app/src/main/AndroidManifest.xml).
  - Resolved Android 14 AdServices manifest merger property collision with `tools:replace="android:resource"`.
  - Added ProGuard keep and optimization rules for Google Mobile Ads in [`proguard-rules.pro`](file:///d:/Quovex%20APP/android/app/proguard-rules.pro).
- **Domain Models & Manager (`com.quovex.domain`):**
  - Added [`AdModels.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/model/AdModels.kt) with `AdRewardResult` (`Success`, `DismissedEarly`, `Error`, `AdFreeSubscriber`), `AdState`, and `AdUnitIds` constants.
  - Added [`AdManager.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/manager/AdManager.kt) domain manager interface.
  - Added [`ShowInterstitialAdUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/ShowInterstitialAdUseCase.kt), [`ShowRewardedAdUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/ShowRewardedAdUseCase.kt), and [`GrantBonusAiQueriesUseCase.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/usecase/GrantBonusAiQueriesUseCase.kt).
- **Data & Billing Integration (`com.quovex.data`):**
  - [`BillingRepository.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/domain/repository/BillingRepository.kt) & [`BillingRepositoryImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/repository/BillingRepositoryImpl.kt): Added `grantBonusAiQueries(bonusCount: Int)` to credit additional queries to the user's daily balance upon watching rewarded video ads.
  - [`AdMobManagerImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/admob/AdMobManagerImpl.kt): Manages `MobileAds.initialize()`, preloads Interstitial and Rewarded ads, handles user rewards callbacks, and provides 100% Pro-tier ad-free exemption.
  - Bound `AdManager` in Hilt [`RepositoryModule.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/di/RepositoryModule.kt).

#### Presentation & UI
- **`QuovexBannerAd.kt` (`com.quovex.ui.components`):**
  - Composable `AndroidView` wrapping Google AdMob `AdView` with automatic Pro entitlement checking that completely disappears with zero height for Pro / Lifetime subscribers.
- **`RewardedAdQuotaDialog.kt` (`com.quovex.ui.components`):**
  - High-polish modal prompt when a student reaches their daily 10 free AI queries:
    - *"Watch Quick Video Ad (+5 Free AI Questions Instantly)"*
    - *"Upgrade to Quovex Pro (Unlimited 24/7 AI tutoring • 100% Ad-Free)"*
- **Dashboard Screen Integration:**
  - Updated [`DashboardViewModel.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/ui/dashboard/DashboardViewModel.kt) to observe `userEntitlement.isAdFree` and embedded [`QuovexBannerAd`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/ui/components/QuovexBannerAd.kt) at the bottom of [`DashboardScreen.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/ui/dashboard/DashboardScreen.kt).

#### Testing & Quality Assurance
- Added unit test suites passing 100%:
  - `AdMobManagerTest.kt` (3 tests — Pro bypass rewarded, Pro bypass interstitial, Free banner enabled)
  - `GrantBonusAiQueriesUseCaseTest.kt` (2 tests)
  - `DashboardViewModelTest.kt` (3 tests updated with `observeUserEntitlementUseCase`)
- Total: All 317 unit test suites passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK deployment on Android emulator (`emulator-5554`) via `./gradlew installDebug`.

---

### Added — Phase 16: Multi-Layer App Distraction Blocker & Accessibility Shield (Module A2)

#### Data & Architecture
- **Android Manifest & Service Configuration:**
  - Added [`accessibility_service_config.xml`](file:///d:/Quovex%20APP/android/app/src/main/res/xml/accessibility_service_config.xml) with event-driven `typeWindowStateChanged` and `canRetrieveWindowContent="false"`.
  - Added launcher intent `<queries>` in [`AndroidManifest.xml`](file:///d:/Quovex%20APP/android/app/src/main/AndroidManifest.xml) for discovering launchable installed apps on device.
  - Declared `QuovexAccessibilityService` with `BIND_ACCESSIBILITY_SERVICE` and `BlockerOverlayActivity`.
- **Domain Models (`com.quovex.domain.model.AppBlockerModels.kt`):**
  - Added `AppCategory` (`SOCIAL`, `STREAMING`, `GAMING`, `BROWSING`, `ENTERTAINMENT`, `CUSTOM`), `BlockedAppInfo`, `DistractionEvent`, and `DistractionShieldState`.
  - Added `KnownDistractorPackages` catalog with automatic category matching for popular distraction apps (Instagram, YouTube, TikTok, Netflix, Reddit, Discord, Twitter/X, BGMI/PUBG, Free Fire, Chrome, etc.).
- **Accessibility Service & Data Repository (`com.quovex.data`):**
  - [`QuovexAccessibilityService.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/service/QuovexAccessibilityService.kt): Event-driven `TYPE_WINDOW_STATE_CHANGED` interception with 1500ms debounce that detects when a student attempts to open a blocked app during an active focus session and launches `BlockerOverlayActivity`.
  - [`DistractionBlockerRepositoryImpl.kt`](file:///d:/Quovex%20APP/android/app/src/main/java/com/quovex/data/repository/DistractionBlockerRepositoryImpl.kt): Discovers installed launchable applications via `PackageManager`, maintains blocked package sets in SharedPreferences, and records resisted distraction attempts.
  - Bound in Hilt `RepositoryModule.kt`.
- **Domain Use Cases (`com.quovex.domain.usecase`):**
  - `ObserveBlockedAppsUseCase.kt`: Reactive `StateFlow<DistractionShieldState>` stream.
  - `ToggleBlockedAppUseCase.kt`: Per-app and category-wide batch blocking actions.
  - `LogDistractionAttemptUseCase.kt`: Distraction resistance tallying.
  - `GetInstalledAppsUseCase.kt`: On-demand installed application discovery.

#### Presentation & UI
- **`BlockerOverlayActivity.kt` (`com.quovex.ui.blocker`):**
  - Fullscreen Jetpack Compose distraction blocker overlay with emerald glowing shield badge.
  - Active study lock card with digital session countdown timer (`22:45 Remaining for Physics`).
  - Distraction resistance counter (`🔥 Resisted 3 distraction attempts today`) and motivational quote.
  - Primary CTA (`← Return to Focus Zone`) that brings Quovex to the foreground.
- **`DistractionShieldSheet.kt` (`com.quovex.ui.timer.components`):**
  - Modal bottom sheet with global shield toggle switch.
  - Android Accessibility Service status banner with direct shortcut to Android Accessibility settings.
  - Category quick-filter chips (`All Apps`, `Social`, `Gaming`, `Streaming`, `Web Browsers`).
  - Search bar and per-app toggle switch list.
- **Timer Integration (`com.quovex.ui.timer`):**
  - Added **App Distraction Shield** setup card in `TimerScreen.kt` and wired actions to `TimerViewModel.kt`.

#### Testing & Quality Assurance
- Added unit test suites passing 100%:
  - `DistractionBlockerRepositoryTest.kt` (4 tests)
  - `ToggleBlockedAppUseCaseTest.kt` (3 tests)
  - `TimerViewModelTest.kt` (9 tests)
- Total: All 313 unit test suites passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK on Android emulator (`emulator-5554`): App Distraction Shield setup card, Distraction Shield bottom sheet, installed apps and category toggles, and fullscreen `BlockerOverlayActivity`.

---

### Added — Phase 15C: Google Play Billing & Premium Subscription Paywall Engine (Module 10)

#### Data & Architecture
- **Dependencies (`libs.versions.toml` & `build.gradle.kts`):**
  - Added Google Play Billing v6 (`com.android.billingclient:billing-ktx:6.2.1`).
- **Domain Models (`com.quovex.domain.model.BillingModels.kt`):**
  - Added `SubscriptionTier` (`FREE`, `PRO_MONTHLY`, `PRO_ANNUAL`, `LIFETIME`), `SubscriptionPlan`, `UserEntitlement`, and `PurchaseResult`.
  - Added `DefaultSubscriptionPlans` catalog featuring **Pro Annual** (`₹999/yr`, 7-Day Free Trial, Save 60%), **Pro Monthly** (`₹199/mo`, Launch Offer ₹99 1st Month), and **Founder Lifetime** (`₹2,499 one-time`, Permanent VIP Access).
- **Billing Repository & Quota Manager (`com.quovex.data.repository.BillingRepositoryImpl.kt`):**
  - Google Play `BillingClient` integration with auto-reconnection and purchase state listener.
  - Offline-resilient fallback plan catalog and local SharedPreferences persistence.
  - Persistent daily AI query quota tracker enforcing the **10 queries/day limit for Free tier users** with automatic midnight calendar date reset, and unlocking unlimited 24/7 AI tutoring for Pro subscribers.
  - Bound in Hilt `RepositoryModule.kt`.
- **Domain Use Cases (`com.quovex.domain.usecase`):**
  - `ObserveUserEntitlementUseCase.kt`: Reactive `StateFlow<UserEntitlement>` observer.
  - `GetSubscriptionPlansUseCase.kt`: Catalog streamer for subscription plans.
  - `PurchaseSubscriptionUseCase.kt`: Triggers Google Play Billing checkout flow.
  - `RestorePurchasesUseCase.kt`: Re-queries active subscriptions and validates entitlements.
  - `CheckAiQuotaUseCase.kt`: Validates and decrements daily free AI query credits (10/day max) or grants unlimited Pro access.

#### Presentation & UI
- **`PremiumPaywallScreen.kt` & `PremiumPaywallViewModel.kt` (`com.quovex.ui.premium`):**
  - Material 3 high-aesthetic Paywall screen with glowing emerald crown hero badge.
  - Feature comparison matrix: Unlimited AI Tutoring (Groq & Cerebras), Unlimited NCERT PDF OCR Scanning, All 9 Ambient Binaural Soundscapes, Real-time Camera AI Focus & Drowsiness Tracker, 100% Ad-Free Deep Work Zone.
  - Interactive Plan Selector cards (Annual Pro, Monthly, Lifetime) with discount badges (`⭐ 7-DAY FREE TRIAL • SAVE 60%`, `LAUNCH OFFER • ₹99 1st MONTH`, `🚀 FOUNDER PASS`).
  - Dynamic checkout CTA button (`Start 7-Day Free Trial • ₹999/yr`, `Upgrade to Pro — ₹2,499`) and Restore Purchases action.
  - Active Pro banner (`👑 Founder Lifetime Active • Permanent VIP Access`).
- **Navigation & Integration:**
  - Added `QuovexRoute.PremiumPaywall` to `QuovexRoutes.kt` and `QuovexNavGraph.kt`.
  - Added **Quovex Pro Membership** card to `ProfileScreen.kt` and connected navigation entry points in `DashboardScreen.kt`.

#### Testing & Quality Assurance
- Added comprehensive unit test suites passing 100%:
  - `BillingRepositoryTest.kt` (5 tests: free initial quota, query consumption, 10-query limit enforcement, plan activation to Pro unlimited, purchase restoration)
  - `CheckAiQuotaUseCaseTest.kt` (2 tests)
  - `PremiumPaywallViewModelTest.kt` (5 tests)
- Total: All unit test suites passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK on Android emulator (`emulator-5554`): Profile Pro Membership card, Premium Paywall screen layout, plan selection between Annual/Monthly/Lifetime, Restore Purchases flow, and active VIP banner.

---

### Added — Phase 15B: Camera AI Focus & Drowsiness Detection (Module A3)

#### Data & Architecture
- **Dependencies & Manifest:**
  - Added Google ML Kit Face Detection (`com.google.android.gms:play-services-mlkit-face-detection:17.1.0`) and AndroidX CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view:1.3.4`) in `libs.versions.toml` and `build.gradle.kts`.
  - Added `android.permission.CAMERA` and camera features to `AndroidManifest.xml`.
- **Domain Models (`com.quovex.domain.model.FocusDetectionModels.kt`):**
  - Added `AttentivenessState` (`ATTENTIVE`, `LOOKING_AWAY`, `DROWSY_EYES_CLOSED`, `NO_FACE_DETECTED`, `CAMERA_OFF`), `FocusTrackingState`, and `FocusFrameResult`.
  - Updated `SessionSummary.kt` to include `focusScore: Int?`, `distractionsCount: Int`, `drowsinessCount: Int`, and `cameraTrackingEnabled: Boolean`.
- **On-Device ML Kit Analyzer (`com.quovex.data.camera.CameraFocusAnalyzer.kt`):**
  - CameraX `ImageAnalysis.Analyzer` leveraging ML Kit `FaceDetector` with fast classification mode enabled.
  - 1 FPS analysis rate limiting to ensure near-zero battery and CPU overhead.
  - Evaluates eye open probabilities (`< 0.25f` triggers drowsiness) and head Euler angles ($X$ and $Y$ $> 24^\circ$ triggers looking away).
  - Strict privacy rule: 100% on-device RAM processing — zero frames are ever saved, recorded, or transmitted to any cloud API.
- **Repository & State Manager (`com.quovex.data.repository.FocusDetectionRepositoryImpl.kt`):**
  - Real-time rolling focus score calculation ($0..100\%$).
  - Consecutive frame streak detection for drowsiness ($\ge 3$ consecutive frames/seconds) and distractions ($\ge 2$ consecutive frames/seconds).
  - SharedPreferences persistence for opt-in camera tracking state.
  - Bound in Hilt `RepositoryModule.kt`.
- **Timer Service & Database Integration:**
  - `TimerForegroundService.kt` activates camera tracking on start and stops on finish.
  - `EndFocusSessionUseCase.kt` records verified focus scores into Room `SessionEntity` and awards bonus 50 XP for laser focus sessions ($\ge 85$).

#### Presentation & UI
- **`CameraFocusPreview.kt` (`com.quovex.ui.timer.components`):**
  - Jetpack Compose preview component with runtime camera permission launcher.
  - Live attentiveness status badge (`🎯 Laser Focused • 98% Score` / `👀 Looking Away` / `😴 Drowsy Alert!`).
  - Expandable/minimizable front-camera preview with `🔒` privacy lock badge.
- **`TimerScreen.kt` & `TimerViewModel.kt`:**
  - **Setup Screen**: Added **AI CAMERA FOCUS DETECTION** toggle card with privacy badge ("100% On-Device ML • Zero Video Saved").
  - **Active Session Screen**: Embedded live attentiveness badge and stealth camera preview below the circular countdown timer.
  - **Summary Screen**: Added verified **AI Camera Focus Score** metric card with distraction & drowsiness event counters.

#### Testing & Quality Assurance
- Added comprehensive unit test suites passing 100%:
  - `FocusDetectionRepositoryTest.kt` (7 tests)
  - `ControlFocusDetectionUseCaseTest.kt` (4 tests)
  - `TimerViewModelTest.kt` (11 tests)
- Total: All unit test suites passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK on Android emulator (`emulator-5554`): Camera runtime permission request, AI Camera Focus setup toggle, active session attentiveness chip & mini preview, and session summary focus score card.

---

### Added — Phase 15A: Ambient Focus Soundscape & Binaural Beats Engine (Module F5)

#### Data & Architecture
- **Domain Models (`com.quovex.domain.model.SoundscapeModels.kt`):**
  - Added `SoundscapePreset`, `SoundscapeCategory` (`ALL`, `BINAURAL`, `NATURE`, `NOISE`), `NoiseType` (`WHITE`, `PINK`, `BROWN`, `RAIN`), `SoundscapePresets` catalog, and `SoundscapeState`.
  - Built-in soundscapes: **Silent Focus**, **Binaural Alpha (10 Hz)** for deep flow, **Binaural Gamma (40 Hz)** for intense problem solving, **Binaural Beta (20 Hz)** for timed exam practice, **Binaural Theta (6 Hz)** for memory consolidation, **Deep Monsoon Rain**, **Soft Pink Noise (1/f)**, **Deep Brown Noise (ADHD focus)**, and **Clean White Noise**.
- **Real-Time DSP Synthesizer (`com.quovex.data.audio.FocusAudioEngine.kt`):**
  - Android `AudioTrack` 44.1kHz stereo 16-bit PCM streaming with pure offline DSP synthesis.
  - Stereo sine phase differentiation for binaural beat entrainment.
  - Voss-McCartney 6-pole IIR pink noise filtering, Brownian random walk integration, and slow-frequency LFO envelope rain modulations.
  - Per-sample smooth volume fading to eliminate audio pops and clicks.
- **Repository & Persistence (`com.quovex.data.repository.SoundscapeRepositoryImpl.kt`):**
  - Reactive `StateFlow<SoundscapeState>` with SharedPreferences persistence for active preset, volume level, and auto-play preference.
  - Bound in Hilt `RepositoryModule.kt`.
- **Timer Service Integration (`com.quovex.data.service.TimerForegroundService.kt`):**
  - Seamless auto-play on focus session start when enabled; auto-pause on timer completion, break, or manual stop.

#### Domain Use Cases
- **`ObserveSoundscapeUseCase`**: Exposes reactive state flow of soundscape playback and user preferences.
- **`ControlSoundscapeUseCase`**: Handles preset switching, volume adjustments (clamped 0.0f..1.0f), auto-play toggles, and play/pause controls.

#### Presentation & UI
- **`TimerScreen.kt` & `TimerViewModel.kt`**:
  - Added **AMBIENT SOUNDSCAPE** card to the Timer Setup screen displaying current preset, emoji, volume, and tuning action.
  - Added sleek mini ambient soundscape controller chip beneath the active circular countdown timer with live play/pause toggles and quick sheet opening.
- **`SoundscapeSelectorSheet.kt` (`com.quovex.ui.timer.components`):**
  - M3 Modal Bottom Sheet with category filter tabs (`All`, `Binaural Beats`, `Nature & Rain`, `Noise Masks`).
  - Master volume slider with dynamic percentage indicator.
  - Auto-play with timer switch.
  - Preset card list with animated 3-bar equalizer visualization during active playback and play preview actions.

#### Testing & Quality Assurance
- Added comprehensive unit test suites passing 100%:
  - `SoundscapeRepositoryTest.kt` (6 tests)
  - `ControlSoundscapeUseCaseTest.kt` (5 tests)
  - `TimerViewModelTest.kt` (10 tests)
- Total: All unit tests passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK on Android emulator (`emulator-5554`): Soundscape bottom sheet, category tabs, volume slider, active timer mini controller, and auto-pause on session end.

---

### Added — Phase 14: AI Dynamic Study Planner & Adaptive Schedule Engine (Module B5)

#### Data & Architecture
- **Domain Models (`com.quovex.domain.model.StudyPlanModels.kt`):**
  - Added `StudyPlan`, `DailyStudyTask`, `PlanStatus` (`ACTIVE`, `COMPLETED`, `ARCHIVED`), `StudyTaskType` (`STUDY_CHAPTER`, `REVISE_FLASHCARDS`, `TAKE_QUIZ`, `DEEP_WORK_PRACTICE`), and `StudyRecommendation`.
- **Local Persistence & Room Migration (`com.quovex.data.local`):**
  - Added `StudyPlanEntity` and `StudyTaskEntity` (indexed on `planId` and `dateMillis`).
  - Added `StudyPlanDao` with reactive `Flow` queries (`observeActivePlan`, `observeTasksForPlan`, `observeTasksForDay`, `observeTasksForDateRange`, `updateTaskCompletion`).
  - Upgraded Room DB to **version 6** via `MIGRATION_5_6` in `QuovexDatabase.kt` and wired into Hilt `DatabaseModule.kt`.

#### Domain Use Cases
- **`GenerateStudyPlanUseCase`**: Synthesizes day-by-day exam roadmaps via AI Gateway (`AIRepository.generateStudyPlan()`), includes balanced offline curriculum fallback generation, archives previous plans, and batch inserts tasks into Room.
- **`ObserveDailyScheduleUseCase`**: Reactive `Flow` streams for active plan, today's schedule, day-specific tasks, and entire roadmap timeline.
- **`UpdateTaskProgressUseCase`**: Handles task completion checkbox toggling, study minutes accumulation, and automatic plan completion status recalculation.
- **`GetDailyStudyRecommendationUseCase`**: Generates real-time "What should I study today?" recommendations prioritizing uncompleted daily tasks and low-mastery quiz mistake concepts.

#### Presentation & UI
- **`StudyPlannerScreen.kt` & `StudyPlannerViewModel.kt`**:
  - **4-Step AI Wizard**:
    1. *Target Exam & Date*: Select preset competitive exam (JEE Advanced, NEET UG, CBSE 12, UPSC, SAT, Other) and target date.
    2. *Daily Commitment & Subjects*: Interactive hours slider (1–12 hrs/day) and multi-subject selector chips.
    3. *Weak Topics & Pain Points*: Free-form input for prioritized focus allocation.
    4. *Roadmap Synthesis Review*: Summary card with single-tap "Generate Study Plan ✨" action.
  - **Interactive Visual Roadmap**:
    - Day carousel (`Day 1` to `Day N`) with real-time completion indicators.
    - Daily objective cards with action type emoji badges (`📚 Study Chapter`, `🎯 Deep Practice`, `⚡ Revise Flashcards`, `📝 Quiz`), estimated time, play button, and reactive strikethrough checkboxes.
    - Overall roadmap progress card with live completed hours vs total planned hours.
- **Dashboard Integration (`DashboardScreen.kt` & `DashboardViewModel.kt`)**:
  - Added **TODAY'S STUDY PLAN** widget card displaying active exam roadmap, today's task completion counter, and direct roadmap navigation.
- **Navigation (`QuovexNavGraph.kt` & `QuovexRoutes.kt`)**:
  - Registered `StudyPlanner` route (`"study_planner"`).

#### Testing & Quality Assurance
- Added 5 comprehensive unit test suites passing 100%:
  - `GenerateStudyPlanUseCaseTest.kt` (3 tests)
  - `ObserveDailyScheduleUseCaseTest.kt` (3 tests)
  - `UpdateTaskProgressUseCaseTest.kt` (3 tests)
  - `GetDailyStudyRecommendationUseCaseTest.kt` (3 tests)
  - `StudyPlannerViewModelTest.kt` (6 tests)
- Total: All unit tests passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK on Android emulator (`emulator-5554`): Wizard steps 1-4, roadmap generation, checkbox progress update, and Dashboard widget live sync.

---

### Added — Phase 13: Social, Community, Live Study Rooms & Leaderboards (Module G)

#### Data & Architecture
- **Domain Models (`com.quovex.domain.model.CommunityModels.kt`):**
  - Added `LeaderboardEntry`, `LeaderboardType` (`GLOBAL`, `FRIENDS`, `SUBJECT`), `RankTrend` (`UP`, `DOWN`, `SAME`).
  - Added `StudyRoomSession`, `RoomMember`, and `RoomChatMessage` for real-time multiplayer focus rooms.
  - Added `StudyBattle`, `BattleStatus` (`PENDING`, `ACTIVE`, `COMPLETED`), and `FriendProfile`.
- **Local Persistence & Room Migration (`com.quovex.data.local`):**
  - Added `FriendEntity`, `StudyBattleEntity` (indexed on `challengerId`, `opponentId`), and `LeaderboardCacheEntity` (composite key on `leaderboardType`, `userId`).
  - Added `CommunityDao` with reactive `Flow` queries and automatic stale cache eviction (`cachedAtMillis < threshold`).
  - Upgraded Room DB to **version 5** via `MIGRATION_4_5` in `QuovexDatabase.kt` and wired into Hilt `DatabaseModule.kt`.
- **Remote Networking (`com.quovex.data.remote.FirebaseFirestoreService.kt`):**
  - Real-time snapshot listeners for live room members (`study_rooms/{roomId}/members`) and break-time chat (`study_rooms/{roomId}/messages`).
  - Leaderboard aggregation queries (`weekly_leaderboards/{weekKey}/entries`) sorted by `studyMinutes` descending.
  - 1v1 battle creation and status mutation endpoints.

#### Domain Use Cases
- **`GetLeaderboardUseCase`**: Online Firestore fetch with ISO-week key caching and automatic offline fallback.
- **`StudyRoomSessionUseCase`**: Presence join/leave lifecycle, live member stream, and chat broadcast.
- **`ManageFriendsAndBattlesUseCase`**: Friend and battle observation, lead margin calculation, and goal progress clamping.

#### Presentation & UI
- **`CommunityScreen.kt` & `CommunityViewModel.kt`**:
  - Unified 3-tab experience:
    - **Study Rooms**: Active room feed with subject chips, active member count, live indicator, and create room FAB.
    - **Leaderboards**: Filter by Global / Friends / Subject; Champion Podium (🥇 Gold, 🥈 Silver, 🥉 Bronze) with crown badges, avatar chips, scholar ranks, and dynamic rank trend indicators.
    - **1v1 Battles**: Live progress bars with study minute margins and friend management.
- **`StudyRoomLiveScreen.kt` & `StudyRoomLiveViewModel.kt`**:
  - Full-screen deep work focus screen with live peer presence avatar strip and focus pulse dot.
  - Slide-up break-time chat panel with real-time messages and automatic scroll.
- **Navigation**:
  - Registered `StudyRoomLive` route (`study_room_live/{roomId}`) in `QuovexRoutes.kt` and wired into `QuovexNavGraph.kt`.

#### Testing & Quality Assurance
- Added MockK test dependency (`1.13.11`) to version catalog and Gradle build.
- 5 comprehensive unit test suites passing 100%:
  - `GetLeaderboardUseCaseTest.kt` (5 tests)
  - `StudyRoomSessionUseCaseTest.kt` (6 tests)
  - `ManageFriendsAndBattlesUseCaseTest.kt` (7 tests)
  - `CommunityViewModelTest.kt` (6 tests)
  - `StudyRoomLiveViewModelTest.kt` (9 tests)
- Total: 33/33 tests passing with 0 errors via `./gradlew testDebugUnitTest`.
- Verified live APK on Android emulator (`medium_phone(AVD) - 16`).
