package com.quovex.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─── Quovex Centralized Elevation System ─────────────────────────────────────
data class QuovexElevation(
    val none: Dp = 0.dp,
    val low: Dp = 2.dp,
    val medium: Dp = 4.dp,
    val card: Dp = 4.dp,
    val high: Dp = 8.dp,
    val dialog: Dp = 16.dp
)

val LocalQuovexElevation = staticCompositionLocalOf { QuovexElevation() }
