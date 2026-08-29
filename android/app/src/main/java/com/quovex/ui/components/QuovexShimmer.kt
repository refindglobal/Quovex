package com.quovex.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme

/**
 * Shimmer gradient brush modifier for skeleton placeholders.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        Color(0xFF141F1A),
        Color(0xFF22352C),
        Color(0xFF141F1A)
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Card skeleton shimmer placeholder for feed and catalog items.
 */
@Composable
fun QuovexCardSkeleton(
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = modifier.fillMaxWidth(),
        elevation = QuovexTheme.elevation.card
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.padding(top = spacing.xs)
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(width = 80.dp, height = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brush)
                    )
                }
            }
        }
    }
}
