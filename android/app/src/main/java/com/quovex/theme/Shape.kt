package com.quovex.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

// ─── Quovex Centralized Shape System ─────────────────────────────────────────
data class QuovexShapes(
    val extraSmall: CornerBasedShape = RoundedCornerShape(4.dp),
    val small: CornerBasedShape = RoundedCornerShape(8.dp),
    val medium: CornerBasedShape = RoundedCornerShape(12.dp),
    val large: CornerBasedShape = RoundedCornerShape(16.dp),
    val extraLarge: CornerBasedShape = RoundedCornerShape(20.dp),
    val card: CornerBasedShape = RoundedCornerShape(20.dp),
    val dialog: CornerBasedShape = RoundedCornerShape(24.dp),
    val superLarge: CornerBasedShape = RoundedCornerShape(32.dp),
    val pill: CornerBasedShape = RoundedCornerShape(100.dp),
    val circle: Shape = CircleShape
)

val LocalQuovexShapes = staticCompositionLocalOf { QuovexShapes() }
