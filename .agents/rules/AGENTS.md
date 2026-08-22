# Quovex — Agent Rules

These rules govern how AI agents (Antigravity, Gemini, Copilot, etc.) should behave when working on the **Quovex** Android project.

---

## 🏗️ Project Identity

- **App Name:** Quovex
- **Platform:** Android (Native)
- **Language:** Kotlin only — no Java
- **UI:** Jetpack Compose only — no XML layouts
- **Architecture:** MVVM + Clean Architecture (strict layer separation)
- **Package:** `com.quovex`

---

## ✅ Coding Standards

### Kotlin
- Always use Kotlin idioms: `data class`, `sealed class`, `object`, `companion object`, extension functions
- Prefer `val` over `var` wherever possible
- Use `StateFlow` and `SharedFlow` — never `LiveData`
- Use `suspend` functions and Kotlin Coroutines — never RxJava or callbacks
- Use `Result<T>` or sealed `UiState` for error handling — no bare exceptions
- All ViewModels must use `viewModelScope` for coroutines

### Jetpack Compose
- Every screen is a `@Composable` function — no Fragments
- Use `LazyColumn` / `LazyRow` for lists — never `RecyclerView`
- All colors, typography, and shapes must come from the `QuovexTheme`
- Never hardcode colors, font sizes, or dp values inline
- Use `rememberSaveable` for UI state that survives recomposition

### Architecture Rules
- **Data layer** must never depend on **Presentation layer**
- **Domain layer** must have zero Android dependencies (pure Kotlin)
- **Repository interfaces** live in `domain/repository/`
- **Repository implementations** live in `data/repository/`
- All screen navigation must go through `QuovexNavGraph.kt`
- Business logic belongs in **UseCases**, not ViewModels

---

## 🤖 AI Integration Rules

### Providers
| Provider | Use For | Client |
|---|---|---|
| Groq | Real-time chat, fast completions, motivational quotes | `GroqApiService.kt` |
| Cerebras | Study plan generation, long-context reasoning | `CerebrasApiService.kt` |

### Rules
- **CRITICAL:** Always read `docs/AI_MODELS.md` to get the currently approved AI model IDs (e.g., `openai/gpt-oss-20b`) before writing any API calls. **DO NOT GUESS MODEL NAMES.**
- Always try Groq first for chat; fall back to Cerebras if Groq fails
- All API keys must be stored in `secrets.properties` — **never hardcoded**
- API keys are read via `BuildConfig` — configured in `build.gradle.kts`
- Implement retry with exponential backoff (max 3 retries)
- Free tier users: max 10 AI queries/day, enforced locally via DataStore counter
- AI responses must be streamed where the API supports it
- Never send personally identifiable information (PII) to AI APIs

---

## 🔐 Security Rules

- API keys go in `secrets.properties` (gitignored)
- `google-services.json` must never be committed
- Camera data (focus detection) stays on-device — never uploaded
- No user data sent to AI APIs without explicit consent
- Premium features must validate entitlement server-side (Firebase Functions)

---

## 📁 File & Naming Conventions

| Item | Convention | Example |
|---|---|---|
| Screens | `[Name]Screen.kt` | `TimerScreen.kt` |
| ViewModels | `[Name]ViewModel.kt` | `TimerViewModel.kt` |
| UseCases | `[Verb][Noun]UseCase.kt` | `StartSessionUseCase.kt` |
| Entities | `[Name]Entity.kt` | `SessionEntity.kt` |
| DAOs | `[Name]Dao.kt` | `SessionDao.kt` |
| DTOs | `[Name]Request/Response.kt` | `GroqRequest.kt` |
| DI Modules | `[Name]Module.kt` | `NetworkModule.kt` |
| Workers | `[Name]Worker.kt` | `ReminderWorker.kt` |
| Services | `[Name]Service.kt` | `TimerForegroundService.kt` |

---

## 🚫 What Agents Must NOT Do

- Do **not** use XML layouts or Views — Compose only
- Do **not** use `LiveData` — use `StateFlow`
- Do **not** add business logic inside `@Composable` functions
- Do **not** hardcode API keys, strings, or colors
- Do **not** use Java — Kotlin only
- Do **not** use threads directly — use Coroutines
- Do **not** add new dependencies without checking if one already exists
- Do **not** create files outside the defined project structure
- Do **not** commit sensitive files (`secrets.properties`, `google-services.json`)

---

## 📦 Dependency Rules

Before adding any new library:
1. Check if the functionality already exists in the project
2. Prefer official Jetpack/Google libraries first
3. Check the library's last update — avoid unmaintained libraries
4. Add to the appropriate module in `build.gradle.kts`

### Approved Core Dependencies
```toml
# build.gradle.kts (approved)
- Jetpack Compose BOM (latest stable)
- Hilt (DI)
- Room (local DB)
- Retrofit + OkHttp (networking)
- Kotlinx Coroutines
- WorkManager
- ML Kit Face Detection
- Firebase BOM (Auth, Firestore, FCM, Analytics, Crashlytics)
- Google Play Billing v6
- Coil (image loading)
- DataStore Preferences
```

---

## 🎨 Design System Rules

- Primary color: Emerald Green (`#00C896`) and Dark Charcoal (`#0A0F0D`)
- App uses **dark mode by default**
- Typography: use `Inter` font from Google Fonts
- All UI must follow Material Design 3 (M3)
- Animations must be smooth — use `AnimatedVisibility`, `animateContentSize`
- Every interactive element needs a visual feedback (ripple, scale, color change)

---

## 🧪 Testing Rules

- Every UseCase must have unit tests
- ViewModels must have unit tests with fake repositories
- Use `kotlinx-coroutines-test` for coroutine testing
- UI tests use Compose testing APIs
- All tests live in `src/test/` (unit) and `src/androidTest/` (instrumented)

---

## 📝 Documentation Rules

- All `public` classes and functions must have KDoc comments
- Complex business logic must have inline comments explaining the "why"
- Keep `PRD.md` updated when features are added or changed
- Update `CHANGELOG.md` for every meaningful change

---

## 🔄 Git Workflow

- Branch naming: `feature/feature-name`, `fix/bug-description`, `refactor/what`
- Commit messages: follow Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`)
- Never commit directly to `main`
- PRs require at least one review (when team grows)
