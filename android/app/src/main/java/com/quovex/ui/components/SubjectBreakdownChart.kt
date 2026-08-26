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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.SubjectStudyTime
import com.quovex.theme.QuovexTheme

/**
 * Subject-wise time breakdown visualization.
 */
@Composable
fun SubjectBreakdownChart(
    breakdown: List<SubjectStudyTime>,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors

    val subjectColors = listOf(
        colors.primary,
        Color(0xFF38BDF8), // Sky Blue
        Color(0xFFA78BFA), // Purple
        Color(0xFFFBBF24), // Amber
        Color(0xFFF472B6), // Pink
        Color(0xFF34D399)  // Emerald
    )

    QuovexCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface,
        elevation = QuovexTheme.elevation.low
    ) {
        Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
            Text(
                text = "SUBJECT DISTRIBUTION (30 DAYS)",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = colors.primary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

            if (breakdown.isEmpty()) {
                Text(
                    text = "No subject session data recorded yet.",
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            } else {
                // Multi-segment horizontal bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(colors.surfaceVariant)
                ) {
                    breakdown.forEachIndexed { index, item ->
                        val segmentColor = subjectColors[index % subjectColors.size]
                        Box(
                            modifier = Modifier
                                .weight(item.percentage.coerceAtLeast(0.01f))
                                .height(10.dp)
                                .background(segmentColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                // Subject items list
                Column(verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                    breakdown.forEachIndexed { index, item ->
                        val itemColor = subjectColors[index % subjectColors.size]
                        val hours = item.totalMinutes / 60.0
                        val percentInt = (item.percentage * 100).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(itemColor)
                                )
                                Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
                                Text(
                                    text = item.subject,
                                    style = QuovexTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }

                            Text(
                                text = String.format("%.1fh (%d%%)", hours, percentInt),
                                style = QuovexTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
