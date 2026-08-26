package com.quovex.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.ExamCountdown
import com.quovex.theme.QuovexTheme

/**
 * High-impact target exam countdown card.
 */
@Composable
fun ExamCountdownCard(
    countdown: ExamCountdown,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors

    QuovexCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface,
        elevation = QuovexTheme.elevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuovexTheme.spacing.base),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = "Countdown",
                        tint = colors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))

                Column {
                    Text(
                        text = "${countdown.targetExam.uppercase()} TARGET",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                    Text(
                        text = "${countdown.daysRemaining} Days Remaining",
                        style = QuovexTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Target: ${countdown.targetDateFormatted}",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
