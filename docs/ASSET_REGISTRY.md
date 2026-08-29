# Quovex — Canonical Asset Architecture & Registry

**Version:** 3.0 | **Status:** 100% Verified & Localized | **Canonical Registry:** `quovex-web/lib/assets.ts` & `quovex-admin/lib/assets.ts`

---

## 1. Executive Summary

This document specifies the complete, canonical asset catalog across Android native (`android/app/src/main/res/drawable*`) and Web / Admin interfaces (`public/assets/*`). Every single asset listed in this registry is physically verified on disk, normalized to standard formats, and mapped into typed TypeScript constants (`ASSETS.*`).

---

## 2. Brand Identity Assets

| Asset Token | Source File | Web Public Path | Purpose | Android Usage | Web / Admin Usage | Screen / Location |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `ASSETS.brand.emblem` | `logo .png` | `/assets/brand/logo_emblem.png` | Transparent emerald Q emblem | `ic_launcher_foreground.xml` | `AppHeader`, `AppSidebar`, `AdminSidebar`, `Auth` | App navigation headers & login cards |
| `ASSETS.brand.logo` | `asserts/logo.png` | `/assets/brand/logo.png` | High-res brand logo | `quovex_logo.png` | Marketing landing page | Landing hero, splash |
| `ASSETS.brand.logoText` | `logo text.png` | `/assets/brand/logo_text.png` | Horizontal typography wordmark | Splash / Onboarding header | Landing navbar | Marketing header & footer |
| `ASSETS.brand.logoBg` | `logo bg.png` | `/assets/brand/logo_bg.png` | Solid background emblem | Notification icon | App store banners | Download page / Marketing |
| `ASSETS.brand.heroMockup` | `asserts/hero_mockup.webp*` | `/assets/brand/hero_mockup.png` | 3D Floating Mobile Preview | Google Play feature graphic | Marketing Hero | Landing page hero banner |

---

## 3. Scholar Avatars (1–12)

All avatars are transparent PNGs (256×256) rendered via `ASSETS.avatars(id: number)`.

| Avatar ID | File Path | Theme / Character | Android Location | Web Location |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `/assets/avatars/avatar_1.png` | Cyber Scholar (Default) | `avatar_1.webp` (Profile, Dashboard) | `AppHeader`, `ProfilePage`, `Leaderboard` |
| **2** | `/assets/avatars/avatar_2.png` | Neon Physicist | `avatar_2.webp` (Profile selector) | `ProfilePage`, `CommunityStudyRoom` |
| **3** | `/assets/avatars/avatar_3.png` | Bio Chemist | `avatar_3.webp` (Profile selector) | `ProfilePage`, `CommunityStudyRoom` |
| **4** | `/assets/avatars/avatar_4.png` | Cyber Strategist | `avatar_4.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **5** | `/assets/avatars/avatar_5.png` | Quantum Explorer | `avatar_5.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **6** | `/assets/avatars/avatar_6.png` | Deep Focus Monk | `avatar_6.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **7** | `/assets/avatars/avatar_7.png` | Tech Aspirant | `avatar_7.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **8** | `/assets/avatars/avatar_8.png` | Emerald Master | `avatar_8.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **9** | `/assets/avatars/avatar_9.png` | Neural Scholar | `avatar_9.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **10** | `/assets/avatars/avatar_10.png` | Cybernetic Tactician | `avatar_10.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **11** | `/assets/avatars/avatar_11.png` | Grandmaster Scholar | `avatar_11.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |
| **12** | `/assets/avatars/avatar_12.png` | God Mode Sage | `avatar_12.webp` (Profile selector) | `ProfilePage`, `Leaderboard` |

---

## 4. Flashcard Deck Artworks

| Asset Token | Public File Path | Subject | Android Usage | Web Usage |
| :--- | :--- | :--- | :--- | :--- |
| `ASSETS.decks.physics` | `/assets/decks/deck_physics.jpg` | Physics | `deck_physics_bg.webp` (DeckCard) | `FlashcardsPage`, `DashboardRecentDeck` |
| `ASSETS.decks.chemistry` | `/assets/decks/deck_chemistry.jpg` | Chemistry | `deck_chemistry_bg.webp` (DeckCard) | `FlashcardsPage`, `DashboardRecentDeck` |
| `ASSETS.decks.biology` | `/assets/decks/deck_biology.jpg` | Biology | `deck_biology_bg.webp` (DeckCard) | `FlashcardsPage`, `DashboardRecentDeck` |
| `ASSETS.decks.maths` | `/assets/decks/deck_maths.jpg` | Mathematics | `deck_maths_bg.webp` (DeckCard) | `FlashcardsPage`, `DashboardRecentDeck` |
| `ASSETS.decks.history` | `/assets/decks/deck_history.jpg` | History / Humanities | `deck_history_bg.webp` (DeckCard) | `FlashcardsPage`, `DashboardRecentDeck` |

---

## 5. System Empty States & Illustrations

| Asset Token | Public File Path | Purpose | Screen / State |
| :--- | :--- | :--- | :--- |
| `ASSETS.illustrations.emptyDeck` | `/assets/illustrations/ill_empty_deck.svg` | Zero cards in deck | `FlashcardStudyScreen` (Zero review queue) |
| `ASSETS.illustrations.emptyNotes` | `/assets/illustrations/ill_empty_notes.svg` | Zero notes uploaded | `NotesLibraryScreen` (Zero state) |
| `ASSETS.illustrations.focusBlocked` | `/assets/illustrations/ill_focus_blocked.svg` | Distraction app blocked | `BlockerOverlayActivity`, `FocusShieldPage` |
| `ASSETS.illustrations.permissions` | `/assets/illustrations/ill_permissions.svg` | System permission request | `UsageStatsPermissionSheet`, `DndPromptSheet` |
| `ASSETS.illustrations.welcome` | `/assets/illustrations/ill_welcome.svg` | First-time onboarding | `OnboardingWizardScreen`, `AuthPage` |

---

## 6. 3D Visual Icons & Gamification

| Asset Token | Public File Path | Category | UI Usage |
| :--- | :--- | :--- | :--- |
| `ASSETS.icons3d.rankNovice` | `/assets/icons/3d/Flame_badge_with_green_fire_...png` | Ranks | Novice Scholar badge (0–500 XP) |
| `ASSETS.icons3d.rankApprentice` | `/assets/icons/3d/Silver_and_emerald_cyber_helmet_...png` | Ranks | Apprentice Scholar badge (500–1500 XP) |
| `ASSETS.icons3d.rankStrategist` | `/assets/icons/3d/Floating_badge_with_neon_atom_...png` | Ranks | Strategist Scholar badge (1500–4000 XP) |
| `ASSETS.icons3d.rankGrandmaster` | `/assets/icons/3d/Cybernetic_crown_with_emerald_fl…_...png` | Ranks | Grandmaster Scholar badge (4000+ XP) |
| `ASSETS.icons3d.trophy` | `/assets/icons/3d/Futuristic_championship_trophy_f…_...png` | Gamification | Weekly Leaderboard #1 Podium Card |
| `ASSETS.icons3d.tournamentPodium` | `/assets/icons/3d/Tournament_podium_with_cyber_crowns_...png` | Gamification | Weekly League Standings Banner |
| `ASSETS.icons3d.stopwatch` | `/assets/icons/3d/Futuristic_circular_stopwatch_gl…_...png` | Timer | Focus Timer Screen & HUD |
| `ASSETS.icons3d.emblemQ` | `/assets/icons/3d/Metallic_letter_Q_stopwatch_emblem_...png` | Timer | Deep Focus active state animation |
| `ASSETS.icons3d.soundscapeRain` | `/assets/icons/3d/Storm_cloud_with_glowing_raindrops_...png` | Soundscapes | Binaural Ambient Sound: Rain & Thunder |
| `ASSETS.icons3d.soundscapeCoffee` | `/assets/icons/3d/Cassette_tape_and_coffee_cup_...png` | Soundscapes | Binaural Ambient Sound: Coffee Shop Lo-Fi |
| `ASSETS.icons3d.soundscapeClock` | `/assets/icons/3d/Cloud_raining_over_melting_clock_...png` | Soundscapes | Binaural Ambient Sound: Ticking Clock |
| `ASSETS.icons3d.robotMascot` | `/assets/icons/3d/Futuristic_robot_sphere_floating_...png` | AI | Quovex AI Tutor Avatar & Chat Mascot |
| `ASSETS.icons3d.scannerHologram` | `/assets/icons/3d/Holographic_camera_scanner_brack…_...png` | AI Doubt | Photo Doubt Solver scanning state |
| `ASSETS.icons3d.flashcards` | `/assets/icons/3d/Holographic_flashcards_floating_…_...png` | Flashcards | Flashcard Hub hero card |
| `ASSETS.icons3d.quizBuzzer` | `/assets/icons/3d/Quiz_game_show_buzzer_podium_...png` | Quiz | Diagnostic MCQ challenge card |
| `ASSETS.icons3d.flameBurning` | `/assets/icons/3d/Burning_emerald_flame_badge_stop…_...png` | Streaks | Active streak indicator & streak shield |
| `ASSETS.icons3d.vaultChest` | `/assets/icons/3d/Futuristic_titanium_storage_ches…_...png` | Vault | Streak freeze & rescue token storage |
| `ASSETS.icons3d.iceShield` | `/assets/icons/3d/Crystalline_ice_shield_with_flame_...png` | Streaks | Streak Freeze shield active status |
| `ASSETS.icons3d.shieldChains` | `/assets/icons/3d/Smartphone_wrapped_in_glowing_ch…_...png` | Blocker | Strict Focus Lock mode active banner |
| `ASSETS.icons3d.calendar` | `/assets/icons/3d/Floating_holographic_calendar_gr…_...png` | Planner | AI Study Planner Roadmap card |

---

## 7. Verification Invariants

1. **Zero 404s**: Every path in `ASSETS` references a verified local file on disk.
2. **Type Safety**: All UI components import `ASSETS` from `@/lib/assets` rather than string literals.
3. **Parity**: `quovex-web` and `quovex-admin` share identical asset availability under `public/assets/`.
