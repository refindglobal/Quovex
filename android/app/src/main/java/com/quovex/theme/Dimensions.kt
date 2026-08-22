package com.quovex.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Quovex Centralized Spacing System ───────────────────────────────────────
data class QuovexSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val base: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp
)

object QuovexTouchTarget {
    val minimum: Dp = 48.dp
    val iconButton: Dp = 44.dp
}

val LocalQuovexSpacing = staticCompositionLocalOf { QuovexSpacing() }
