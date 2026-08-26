package com.quovex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.AchievementBadge
import com.quovex.theme.QuovexTheme

/**
 * Achievement badge item card for the student profile screen.
 */
@Composable
fun AchievementBadgeCard(
    badge: AchievementBadge,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors

    QuovexCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = if (badge.isUnlocked) colors.surface else colors.surfaceVariant.copy(alpha = 0.5f),
        borderColor = if (badge.isUnlocked) colors.primary.copy(alpha = 0.4f) else colors.border.copy(alpha = 0.3f),
        elevation = if (badge.isUnlocked) QuovexTheme.elevation.low else QuovexTheme.elevation.none
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuovexTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) colors.primary.copy(alpha = 0.2f) else colors.surfaceVariant
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (badge.isUnlocked) colors.primary else colors.border.copy(alpha = 0.5f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.iconEmoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) colors.textPrimary else colors.textSecondary
                    )

                    Text(
                        text = badge.progressText,
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (badge.isUnlocked) colors.primary else colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))

                Text(
                    text = badge.description,
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textSecondary
                )

                if (!badge.isUnlocked && badge.progress > 0f) {
                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))
                    QuovexLinearProgressIndicator(
                        progress = badge.progress,
                        height = 4.dp,
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                }
            }
        }
    }
}
