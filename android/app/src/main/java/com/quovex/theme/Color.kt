package com.quovex.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─── Primitive Brand Palette ────────────────────────────────────────────────
val PrimaryEmerald = Color(0xFF00C896)
val PrimaryEmeraldDark = Color(0xFF009B74)
val PrimaryEmeraldLight = Color(0xFF5DFDCB)
val PrimaryEmeraldGlow = Color(0x3300C896)

val DangerRed = Color(0xFFFF5252)
val DangerRedDark = Color(0xFFD32F2F)
val DangerRedContainer = Color(0x22FF5252)

val WarningOrange = Color(0xFFFF9F1C)
val WarningOrangeDark = Color(0xFFE67E22)
val WarningOrangeContainer = Color(0x22FF9F1C)

val InfoBlue = Color(0xFF2196F3)
val InfoBlueContainer = Color(0x222196F3)

val SuccessGreen = Color(0xFF00C896)
val SuccessGreenContainer = Color(0x2200C896)

// ─── Dark Theme Raw Primitives ──────────────────────────────────────────────
val AppBackground = Color(0xFF0A0F0D)
val SurfaceCard = Color(0xFF111917)
val CardElevated = Color(0xFF16231F)
val SurfaceCardBorder = Color(0xFF2D4438)
val SurfaceCardBorderLight = Color(0xFF1F3329)
val InputBackground = Color(0xFF1C2B24)
val TextPrimary = Color(0xFFE8F5F0)
val TextSecondary = Color(0xFF8AAFA3)
val TextTertiary = Color(0xFF567269)
val TextDisabled = Color(0xFF384D45)
val TextDark = Color(0xFF0A0F0D)

// ─── Light Theme Raw Primitives ─────────────────────────────────────────────
val AppBackgroundLight = Color(0xFFF7F9F8)
val SurfaceCardLight = Color(0xFFFFFFFF)
val CardElevatedLight = Color(0xFFF1F5F3)
val SurfaceCardBorderLightMode = Color(0xFFD9E2DC)
val InputBackgroundLight = Color(0xFFEDF2EF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextTertiaryLight = Color(0xFF94A3B8)
val TextDisabledLight = Color(0xFFCBD5E1)

// ─── Semantic Quovex Color System ───────────────────────────────────────────
data class QuovexColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val primaryGlow: Color,

    val secondary: Color,
    val onSecondary: Color,

    val background: Color,
    val onBackground: Color,

    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceElevated: Color,

    val border: Color,
    val borderLight: Color,
    val borderFocus: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,

    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,

    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,

    val error: Color,
    val onError: Color,
    val errorContainer: Color,

    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,

    val divider: Color,
    val isDark: Boolean
)

val DarkQuovexColors = QuovexColors(
    primary = PrimaryEmerald,
    onPrimary = TextDark,
    primaryContainer = Color(0xFF003828),
    onPrimaryContainer = PrimaryEmeraldLight,
    primaryGlow = PrimaryEmeraldGlow,

    secondary = Color(0xFF2AC49D),
    onSecondary = TextDark,

    background = AppBackground,
    onBackground = TextPrimary,

    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = InputBackground,
    onSurfaceVariant = TextSecondary,
    surfaceElevated = CardElevated,

    border = SurfaceCardBorder,
    borderLight = SurfaceCardBorderLight,
    borderFocus = PrimaryEmerald,

    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    textDisabled = TextDisabled,

    success = SuccessGreen,
    onSuccess = TextDark,
    successContainer = SuccessGreenContainer,

    warning = WarningOrange,
    onWarning = TextDark,
    warningContainer = WarningOrangeContainer,

    error = DangerRed,
    onError = Color.White,
    errorContainer = DangerRedContainer,

    info = InfoBlue,
    onInfo = Color.White,
    infoContainer = InfoBlueContainer,

    divider = SurfaceCardBorderLight,
    isDark = true
)

val LightQuovexColors = QuovexColors(
    primary = PrimaryEmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC3FBE6),
    onPrimaryContainer = Color(0xFF002116),
    primaryGlow = Color(0x2200C896),

    secondary = Color(0xFF008966),
    onSecondary = Color.White,

    background = AppBackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceCardLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = InputBackgroundLight,
    onSurfaceVariant = TextSecondaryLight,
    surfaceElevated = CardElevatedLight,

    border = SurfaceCardBorderLightMode,
    borderLight = Color(0xFFE2E8F0),
    borderFocus = PrimaryEmeraldDark,

    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    textDisabled = TextDisabledLight,

    success = Color(0xFF059669),
    onSuccess = Color.White,
    successContainer = Color(0xFFD1FAE5),

    warning = Color(0xFFD97706),
    onWarning = Color.White,
    warningContainer = Color(0xFFFEF3C7),

    error = DangerRedDark,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),

    info = Color(0xFF0284C7),
    onInfo = Color.White,
    infoContainer = Color(0xFFE0F2FE),

    divider = Color(0xFFE2E8F0),
    isDark = false
)

val LocalQuovexColors = staticCompositionLocalOf { DarkQuovexColors }

// ─── Material 3 Compatibility Mappings ──────────────────────────────────────
val md_theme_dark_primary = PrimaryEmerald
val md_theme_dark_onPrimary = TextDark
val md_theme_dark_background = AppBackground
val md_theme_dark_surface = SurfaceCard
val md_theme_dark_surfaceVariant = InputBackground
val md_theme_dark_onBackground = TextPrimary
val md_theme_dark_onSurface = TextPrimary
val md_theme_dark_onSurfaceVariant = TextSecondary
val md_theme_dark_outline = SurfaceCardBorder
val md_theme_dark_error = DangerRed

val md_theme_light_primary = PrimaryEmeraldDark
val md_theme_light_onPrimary = Color.White
val md_theme_light_background = AppBackgroundLight
val md_theme_light_surface = SurfaceCardLight
val md_theme_light_surfaceVariant = InputBackgroundLight
val md_theme_light_onBackground = TextPrimaryLight
val md_theme_light_onSurface = TextPrimaryLight
val md_theme_light_onSurfaceVariant = TextSecondaryLight
val md_theme_light_outline = SurfaceCardBorderLightMode
val md_theme_light_error = DangerRedDark
