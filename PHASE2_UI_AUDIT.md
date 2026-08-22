# Quovex Phase 2 — UI/UX Design System Audit

## 1. Existing Design Tokens
- **Colors (`theme/Color.kt`)**: Defines `PrimaryEmerald` (#00C896), `PrimaryEmeraldGlow`, `DangerRed`, `WarningOrange`, `AppBackground` (#0A0F0D), `SurfaceCard` (#111917), `SurfaceCardBorder` (#2D4438), `InputBackground` (#1C2B24), `TextPrimary` (#E8F5F0), `TextSecondary` (#8AAFA3), `TextDark` (#000000), `CardElevated` (#16231F), and light mode equivalents.
- **Typography (`theme/Type.kt`)**: Only basic `Typography` with `bodyLarge` is configured; complete M3 scale (`displayLarge`, `headlineMedium`, `titleLarge`, `bodyMedium`, `labelLarge`, etc.) is missing.
- **Shapes (`theme/Shape.kt`)**: Currently missing; corner radii are hardcoded inline (12.dp, 16.dp, 20.dp, 24.dp, 32.dp, 100.dp).
- **Spacing (`theme/Dimensions.kt` / `QuovexSpacing`)**: Currently missing; padding values like 4.dp, 8.dp, 12.dp, 14.dp, 16.dp, 20.dp, 24.dp, 28.dp, 36.dp are hardcoded inline.
- **Elevation (`theme/Elevation.kt` / `QuovexElevation`)**: Currently missing; inconsistent shadow and tonal elevations used across screens.
- **Theme Composition (`theme/Theme.kt`)**: Uses standard MaterialTheme with `DarkColorScheme` and `LightColorScheme`, but custom Quovex semantic tokens are not exposed via a `LocalQuovexColors` CompositionLocal or unified accessor.

## 2. Existing Reusable Components (`ui/components/`)
- `QuovexButton.kt`: Primary, Secondary, Ghost, Danger variants with basic icon and loading support.
- `QuovexCard.kt`: Surface card with outline and border stroke.
- `QuovexChip.kt`: Selectable filter pill with optional leading icon.
- `QuovexTopAppBar.kt`: Center-aligned top bar with back navigation and actions.

## 3. Duplicated Components & Repetitive Patterns
- **Ad-hoc TextFields**: `TextField` and `OutlinedTextField` are styled with inline `TextFieldDefaults.colors(...)` and duplicate shape styling in `AiChatScreen`, `AiNoteSummarizerScreen`, `DeckListScreen`, `OnboardingWizardScreen`.
- **Ad-hoc Dialogs**: Raw `AlertDialog` in `DeckListScreen` without standardized buttons, rounded corners, or surface hierarchy.
- **Ad-hoc Progress Indicators**: `CircularProgressIndicator` and `LinearProgressIndicator` styled with custom colors across multiple screens without standardized sizing or state feedback.
- **Ad-hoc Stats & Summary Cards**: Custom card containers duplicated in `DashboardScreen`, `ProfileScreen`, and `CommunityScreen`.

## 4. Hardcoded Styling Across Screens
- Raw font sizes (`fontSize = 11.sp`, `13.sp`, `15.sp`, `18.sp`, `22.sp`, `26.sp`) instead of typography tokens.
- Scattered dp paddings (`10.dp`, `14.dp`, `18.dp`, `22.dp`, `28.dp`, `36.dp`).
- Direct references to top-level color constants (`AppBackground`, `SurfaceCard`, `PrimaryEmerald`) rather than theme-aware dynamic color scheme accessors, reducing clean light/dark adaptation.

## 5. Screens Requiring Migration
1. `ui/auth/AuthScreen.kt`
2. `ui/onboarding/OnboardingWizardScreen.kt`
3. `ui/dashboard/DashboardScreen.kt`
4. `ui/decks/DeckListScreen.kt`
5. `ui/flashcards/FlashcardPlayerScreen.kt`
6. `ui/timer/TimerScreen.kt`
7. `ui/ai/AiChatScreen.kt`
8. `ui/ai/AiNoteSummarizerScreen.kt`
9. `ui/community/CommunityScreen.kt`
10. `ui/profile/ProfileScreen.kt`
11. `ui/navigation/QuovexNavGraph.kt` (Bottom navigation bar)

## 6. Missing Design Primitives
- `QuovexTextField` (standard, focused, error state with text explanation, disabled state, accessible touch target)
- `QuovexSearchField` (search bar with search icon and clear button)
- `QuovexLoading` (contextual loading spinner and skeleton placeholder)
- `QuovexErrorState` (error illustration/icon, descriptive text, retry CTA button)
- `QuovexEmptyState` (empty illustration, title, subtitle, action button)
- `QuovexDialog` (standardized M3 dialog with brand styling)
- `QuovexBottomSheet` (modal bottom sheet container)
- `QuovexSectionHeader` (title, optional subtitle, and trailing action)
- `QuovexProgressIndicator` (determinate and indeterminate brand progress bars)
- `QuovexSpacing`, `QuovexShapes`, `QuovexElevation`, `QuovexAnimations`

## 7. Accessibility Problems
- Minimum touch target sizing (< 48dp on some icon buttons and compact chips).
- Missing or non-descriptive content descriptions on decorative vector graphics.
- Button disabled states relying solely on opacity rather than accessible semantic disabled properties.
- Keyboard actions and IME options need proper focus management so the software keyboard does not obscure input fields.

## 8. Visual Inconsistencies
- Corner radii mismatch (16dp on buttons vs 20dp on cards vs 24dp on dialogs vs 32dp on hero boxes).
- Border stroke widths varying between 1dp, 1.5dp, and 2dp arbitrarily.
- Tonal elevation vs shadow elevation used inconsistently across lists.
