package com.quovex.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim

/**
 * Animated Live Soundscape Waveform Visualizer
 * Renders pulsating audio bars with subtle phase shifts to visualize active soundscapes.
 */
@Composable
fun SoundscapeWaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18,
    maxHeight: Dp = 32.dp,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 3.dp,
    color: Color = BrandEmerald
) {
    val transition = rememberInfiniteTransition(label = "waveformTransition")

    val phases = (0 until barCount).map { i ->
        val duration = 400 + ((i * 137) % 500)
        val initialHeight = if (isPlaying) 0.15f + ((i * 31) % 40) / 100f else 0.12f
        val targetHeight = if (isPlaying) 0.5f + ((i * 73) % 50) / 100f else 0.12f

        val animatedFraction by transition.animateFloat(
            initialValue = initialHeight,
            targetValue = targetHeight,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$i"
        )
        if (isPlaying) animatedFraction else 0.12f
    }

    Row(
        modifier = modifier
            .height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(barSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        phases.forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(barWidth / 2))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                color,
                                BrandEmeraldDim
                            )
                        )
                    )
            )
        }
    }
}
