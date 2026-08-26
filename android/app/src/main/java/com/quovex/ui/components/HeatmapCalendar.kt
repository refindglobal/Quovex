package com.quovex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.HeatmapDay
import com.quovex.theme.QuovexTheme

/**
 * GitHub-style study consistency contribution heatmap calendar.
 * Renders a 4-week (28-day) grid with 5 level green gradient intensities.
 */
@Composable
fun HeatmapCalendar(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    onDayClick: (HeatmapDay) -> Unit = {}
) {
    val colors = QuovexTheme.colors
    var selectedDay by remember { mutableStateOf<HeatmapDay?>(null) }

    val daysByWeek = remember(days) {
        days.chunked(7)
    }

    QuovexCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface,
        elevation = QuovexTheme.elevation.low
    ) {
        Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONSISTENCY HEATMAP",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = colors.primary
                )

                // Legend (Less -> More)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Less",
                        style = QuovexTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = colors.textSecondary
                    )
                    (0..4).forEach { intensity ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(getIntensityColor(intensity, colors.primary, colors.surfaceVariant))
                        )
                    }
                    Text(
                        text = "More",
                        style = QuovexTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

            // Day-of-week header row (M T W T F S S)
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = QuovexTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))

            // Grid of Weeks
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                daysByWeek.forEach { weekDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekDays.forEach { day ->
                            val cellColor = getIntensityColor(day.intensityLevel, colors.primary, colors.surfaceVariant)
                            val isSelected = selectedDay?.dateMillis == day.dateMillis

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(cellColor)
                                    .border(
                                        width = if (day.isToday || isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) colors.primary else if (day.isToday) colors.warning else colors.border.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        selectedDay = day
                                        onDayClick(day)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.minutesStudied > 0) {
                                    Text(
                                        text = if (day.minutesStudied >= 60) "${day.minutesStudied / 60}h" else "${day.minutesStudied}m",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (day.intensityLevel >= 3) Color.Black else colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Inspection Callout
            selectedDay?.let { day ->
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = QuovexTheme.spacing.sm, vertical = QuovexTheme.spacing.xs),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${day.formattedDate}: ${if (day.minutesStudied > 0) "${day.minutesStudied} mins studied" else "No study logged"}",
                        style = QuovexTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (day.minutesStudied > 0) colors.primary else colors.textSecondary
                    )
                    if (day.isToday) {
                        Text(
                            text = "TODAY",
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = colors.warning
                        )
                    }
                }
            }
        }
    }
}

private fun getIntensityColor(intensity: Int, primary: Color, baseSurface: Color): Color {
    return when (intensity) {
        0 -> baseSurface
        1 -> primary.copy(alpha = 0.25f)
        2 -> primary.copy(alpha = 0.50f)
        3 -> primary.copy(alpha = 0.75f)
        4 -> primary
        else -> primary
    }
}
