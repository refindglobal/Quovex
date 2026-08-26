package com.quovex.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexLinearProgressIndicator
import com.quovex.ui.components.QuovexSectionHeader
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToBlocker: () -> Unit = {},
    onSignedOut: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = QuovexTheme.colors

    val avatarResId = when (state.userProfile.avatarId) {
        1 -> R.drawable.avatar_1
        2 -> R.drawable.avatar_2
        3 -> R.drawable.avatar_3
        4 -> R.drawable.avatar_4
        5 -> R.drawable.avatar_5
        6 -> R.drawable.avatar_6
        7 -> R.drawable.avatar_7
        8 -> R.drawable.avatar_8
        9 -> R.drawable.avatar_9
        10 -> R.drawable.avatar_10
        11 -> R.drawable.avatar_11
        12 -> R.drawable.avatar_12
        else -> R.drawable.avatar_1
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(title = "Profile & Settings")
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = QuovexTheme.spacing.lg, vertical = QuovexTheme.spacing.md)
        ) {
            // --- 1. USER PROFILE HEADER ---
            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                elevation = QuovexTheme.elevation.card
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QuovexTheme.spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Image(
                            painter = painterResource(id = avatarResId),
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, colors.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(colors.primary, CircleShape)
                                .border(2.5.dp, colors.background, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                    Text(
                        text = state.userProfile.name.ifBlank { "Quovex Aspirant" },
                        style = QuovexTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))

                    Text(
                        text = "${state.userProfile.targetExam} Aspirant • ${state.userProfile.email}",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                    // XP and Scholar Level Progress
                    val scholarInfo = state.scholarLevelInfo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuovexChip(
                            label = "Level ${scholarInfo.rank.level} • ${scholarInfo.rank.title}",
                            isSelected = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Level",
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        Text(
                            text = if (scholarInfo.nextRank != null) {
                                "${scholarInfo.currentXp} XP (${scholarInfo.xpRequiredForNextLevel} to ${scholarInfo.nextRank.title})"
                            } else {
                                "${scholarInfo.currentXp} XP (Grandmaster)"
                            },
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                    QuovexLinearProgressIndicator(
                        progress = scholarInfo.progressPercent.coerceIn(0.05f, 1f),
                        height = 8.dp,
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

            // --- PRO MEMBERSHIP CARD ---
            QuovexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToPaywall),
                backgroundColor = colors.surfaceElevated,
                borderColor = colors.primary,
                borderWidth = 1.5.dp
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
                                .size(40.dp)
                                .background(colors.primaryGlow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(QuovexTheme.spacing.md))
                        Column {
                            Text(
                                text = "Quovex Pro Membership",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Unlimited AI tutoring, PDF OCR & soundscapes",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Upgrade to Pro",
                        tint = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

            // --- 2. STATS 3-GRID ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
            ) {
                // Focus Hours
                QuovexCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface,
                    elevation = QuovexTheme.elevation.low
                ) {
                    Column(
                        modifier = Modifier.padding(QuovexTheme.spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format("%.1fh", state.totalFocusHours),
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                        Text(
                            text = "Focus Time",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // Focus Score
                QuovexCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface,
                    elevation = QuovexTheme.elevation.low
                ) {
                    Column(
                        modifier = Modifier.padding(QuovexTheme.spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${state.focusScoreAverage}%",
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                        Text(
                            text = "Focus Score",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // Streak
                QuovexCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface,
                    elevation = QuovexTheme.elevation.low
                ) {
                    Column(
                        modifier = Modifier.padding(QuovexTheme.spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${state.userProfile.streakDays}d",
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                        Text(
                            text = "Streak",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // --- 3. ACHIEVEMENTS SECTION ---
            if (state.achievements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                QuovexSectionHeader(
                    title = "Scholar Achievements"
                )

                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                Column(verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                    state.achievements.forEach { badge ->
                        com.quovex.ui.components.AchievementBadgeCard(badge = badge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // --- 4. REFERRAL BANNER ---
            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.primaryGlow,
                borderColor = colors.primary.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QuovexTheme.spacing.base),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = "Pro",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(QuovexTheme.spacing.xs))
                            Text(
                                text = "Quovex Pro • Refer & Earn",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))
                        Text(
                            text = "Share code: ${state.userProfile.name.take(4).uppercase()}${state.userProfile.level * 7}",
                            style = QuovexTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Get 7 days of Pro for each friend who joins.",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    QuovexButton(
                        text = "Share",
                        onClick = { },
                        variant = QuovexButtonVariant.Primary,
                        height = 36.dp,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share Icon",
                                tint = colors.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // --- 4. PREFERENCES LIST ---
            QuovexSectionHeader(
                title = "Preferences"
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                elevation = QuovexTheme.elevation.card
            ) {
                Column {
                    ProfileOptionRow(
                        icon = Icons.Filled.Person,
                        title = "Account Details",
                        subtitle = state.userProfile.email.ifEmpty { "Connected via Google / Guest" }
                    )
                    ProfileOptionRow(
                        icon = Icons.Filled.Notifications,
                        title = "Daily Study Reminders",
                        subtitle = "Target: ${state.userProfile.dailyGoalHours}h daily"
                    )
                    ProfileOptionRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Focus Audio & Binaural Beats",
                        subtitle = "Lo-fi beats enabled"
                    )
                    ProfileOptionRow(
                        icon = Icons.Filled.Security,
                        title = "Strict App Blocker",
                        subtitle = "Accessibility & app restriction shield",
                        onClick = onNavigateToBlocker
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

            // --- 5. SIGN OUT BUTTON ---
            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                elevation = QuovexTheme.elevation.low,
                onClick = onSignedOut
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QuovexTheme.spacing.base),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign out",
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
                    Text(
                        text = "Sign Out",
                        style = QuovexTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxxl))
        }
    }
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    val colors = QuovexTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = QuovexTheme.spacing.base, vertical = QuovexTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(QuovexTheme.shapes.small)
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))

            Column {
                Text(
                    text = title,
                    style = QuovexTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open setting",
            tint = colors.textSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
