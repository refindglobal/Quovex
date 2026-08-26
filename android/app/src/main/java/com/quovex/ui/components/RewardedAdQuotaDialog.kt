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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.theme.QuovexTheme

/**
 * Opt-in modal prompt displayed when a student has reached their free daily AI quota (10/10).
 * Offers watching a short video ad (+5 bonus queries) or upgrading to Quovex Pro.
 */
@Composable
fun RewardedAdQuotaDialog(
    onDismiss: () -> Unit,
    onWatchAd: () -> Unit,
    onUpgradePro: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexDialog(
        title = "",
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(colors.primaryGlow, CircleShape)
                    .border(1.5.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(spacing.md))

            Text(
                text = "Daily AI Limit Reached (10/10)",
                style = QuovexTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xs))

            Text(
                text = "You have used all 10 free AI credits for today. Choose an option to continue studying:",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.lg))

            // Option 1: Rewarded Ad (+5 Queries)
            QuovexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onWatchAd),
                backgroundColor = colors.surfaceElevated,
                borderColor = colors.primary,
                borderWidth = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OndemandVideo,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Watch Quick Video Ad",
                            style = QuovexTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "+5 Free AI Questions Instantly",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.sm))

            // Option 2: Quovex Pro Unlimited
            QuovexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onUpgradePro),
                backgroundColor = colors.surface,
                borderColor = colors.border
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(colors.warning.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 20.sp
                        )
                    }

                    Spacer(Modifier.width(spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Upgrade to Quovex Pro",
                            style = QuovexTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Unlimited 24/7 AI tutoring • 100% Ad-Free",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.lg))

            Text(
                text = "Maybe Later",
                style = QuovexTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary,
                modifier = Modifier
                    .clickable(onClick = onDismiss)
                    .padding(spacing.sm)
            )
        }
    }
}
