package com.quovex.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkMaterialColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    background = md_theme_dark_background,
    surface = md_theme_dark_surface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onBackground = md_theme_dark_onBackground,
    onSurface = md_theme_dark_onSurface,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    error = md_theme_dark_error
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    background = md_theme_light_background,
    surface = md_theme_light_surface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onBackground = md_theme_light_onBackground,
    onSurface = md_theme_light_onSurface,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    error = md_theme_light_error
)

// ─── Unified Theme Accessor ──────────────────────────────────────────────────
object QuovexTheme {
    val colors: QuovexColors
        @Composable
        get() = LocalQuovexColors.current

    val typography: Typography
        @Composable
        get() = LocalQuovexTypography.current

    val shapes: QuovexShapes
        @Composable
        get() = LocalQuovexShapes.current

    val spacing: QuovexSpacing
        @Composable
        get() = LocalQuovexSpacing.current

    val elevation: QuovexElevation
        @Composable
        get() = LocalQuovexElevation.current
}

@Composable
fun QuovexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val quovexColors = if (darkTheme) DarkQuovexColors else LightQuovexColors
    val materialColorScheme = if (darkTheme) DarkMaterialColorScheme else LightMaterialColorScheme
    val quovexTypography = QuovexTypographyInstance
    val quovexShapes = QuovexShapes()
    val quovexSpacing = QuovexSpacing()
    val quovexElevation = QuovexElevation()

    CompositionLocalProvider(
        LocalQuovexColors provides quovexColors,
        LocalQuovexTypography provides quovexTypography,
        LocalQuovexShapes provides quovexShapes,
        LocalQuovexSpacing provides quovexSpacing,
        LocalQuovexElevation provides quovexElevation
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = quovexTypography,
            content = content
        )
    }
}
