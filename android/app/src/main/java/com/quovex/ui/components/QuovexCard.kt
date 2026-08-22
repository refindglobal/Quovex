package com.quovex.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme

@Composable
fun QuovexCard(
    modifier: Modifier = Modifier,
    shape: Shape = QuovexTheme.shapes.card,
    backgroundColor: Color = QuovexTheme.colors.surface,
    borderColor: Color = QuovexTheme.colors.border,
    borderWidth: Dp = 1.dp,
    elevation: Dp = QuovexTheme.elevation.low,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = backgroundColor,
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        shadowElevation = elevation,
        tonalElevation = elevation
    ) {
        Box(content = content)
    }
}

@Composable
fun QuovexElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = QuovexTheme.shapes.card,
    backgroundColor: Color = QuovexTheme.colors.surfaceElevated,
    borderColor: Color = QuovexTheme.colors.border,
    borderWidth: Dp = 1.dp,
    elevation: Dp = QuovexTheme.elevation.high,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    QuovexCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        elevation = elevation,
        onClick = onClick,
        content = content
    )
}

@Composable
fun QuovexOutlinedCard(
    modifier: Modifier = Modifier,
    shape: Shape = QuovexTheme.shapes.card,
    backgroundColor: Color = Color.Transparent,
    borderColor: Color = QuovexTheme.colors.border,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    QuovexCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        borderWidth = borderWidth,
        elevation = 0.dp,
        onClick = onClick,
        content = content
    )
}
