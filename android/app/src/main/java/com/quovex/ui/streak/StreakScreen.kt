package com.quovex.ui.streak

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.quovex.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.CemeteryTombstone
import com.quovex.domain.model.StreakInfo
import com.quovex.domain.model.StreakMilestone
import com.quovex.domain.model.StreakStatus
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakScreen(
    viewModel: StreakViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    var reflectionInput by remember { mutableStateOf("") }

    LaunchedEffect(state.actionMessage) {
        state.actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissActionMessage()
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Streak & Resilience",
                        style = QuovexTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                actions = {
                    // Rescue Token Badge Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = QuovexTheme.spacing.md)
                            .clip(QuovexTheme.shapes.pill)
                            .background(colors.primary.copy(alpha = 0.15f))
                            .border(1.dp, colors.primary.copy(alpha = 0.4f), QuovexTheme.shapes.pill)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Rescue Tokens",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.streakInfo.rescueTokens} Token${if (state.streakInfo.rescueTokens == 1) "" else "s"}",
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = QuovexTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.lg)
        ) {
            // ── 1. ACTIVE STREAK HERO CARD ────────────────────────────────
            item {
                ActiveStreakHeroCard(
                    streakInfo = state.streakInfo
                )
            }

            // ── 2. RESCUE TOKEN PROTECTION MODULE ─────────────────────────
            item {
                RescueTokenModuleCard(
                    streakInfo = state.streakInfo,
                    isRedeeming = state.isRedeemingToken,
                    onRedeemToken = { viewModel.spendRescueToken() }
                )
            }

            // ── 3. MILESTONES ROADMAP ─────────────────────────────────────
            item {
                Text(
                    text = "Milestone Journey",
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            item {
                MilestonesGrid(milestones = state.milestones)
            }

            // ── 4. STREAK CEMETERY (ANTI-DUOLINGO ANTI-FRAGILITY) ──────────
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🪦 Streak Cemetery",
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${state.tombstones.size} monument${if (state.tombstones.size == 1) "" else "s"}",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = "Honoring past momentum. Reflect on broken streaks to forge unbreakable discipline.",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (state.tombstones.isEmpty()) {
                item {
                    QuovexCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(QuovexTheme.spacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔥", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                            Text(
                                text = "Zero Broken Streaks",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Your momentum is pristine! Keep studying consistently to protect your active flame.",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(state.tombstones, key = { it.id }) { tombstone ->
                    CemeteryTombstoneCard(
                        tombstone = tombstone,
                        onWriteReflection = {
                            reflectionInput = tombstone.reflectionNote ?: ""
                            viewModel.openReflectionDialog(tombstone)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))
            }
        }
    }

    // ── POST-MORTEM REFLECTION DIALOG ──────────────────────────────────────
    state.selectedTombstoneForReflection?.let { tombstone ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissReflectionDialog() },
            title = {
                Text(
                    text = "Post-Mortem Reflection",
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Streak: ${tombstone.streakLength} Days (${tombstone.dateRangeFormatted})",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                    Text(
                        text = "Why did this streak end and what will you do differently this time?",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                    OutlinedTextField(
                        value = reflectionInput,
                        onValueChange = { reflectionInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = {
                            Text(
                                text = "e.g. Lost focus after exam week. Next time I will set a 15-min minimum baseline...",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.surfaceVariant,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveReflection(reflectionInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
                ) {
                    Text("Save Reflection", color = colors.background, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReflectionDialog() }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.surface,
            shape = QuovexTheme.shapes.dialog
        )
    }
}

@Composable
fun ActiveStreakHeroCard(
    streakInfo: StreakInfo
) {
    val colors = QuovexTheme.colors
    val progressAnim by animateFloatAsState(
        targetValue = streakInfo.milestoneProgress,
        label = "milestoneProgress"
    )

    QuovexCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuovexTheme.spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "ACTIVE STREAK",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${streakInfo.currentStreak} Days",
                        style = QuovexTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    colors.primary.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_streak_flame_blazing),
                        contentDescription = "Active Flame",
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

            // Milestone Target progress
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Target: ${streakInfo.nextMilestoneDays}-Day Milestone",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )
                Text(
                    text = "${(streakInfo.milestoneProgress * 100).toInt()}%",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(QuovexTheme.shapes.pill),
                color = colors.primary,
                trackColor = colors.surfaceVariant
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Personal Best: ${streakInfo.longestStreak} Days",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Text(
                    text = if (streakInfo.isProtectedToday) "🛡️ Streak Shielded" else "⚡ Active Today",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (streakInfo.isProtectedToday) colors.primary else colors.secondary
                )
            }
        }
    }
}

@Composable
fun RescueTokenModuleCard(
    streakInfo: StreakInfo,
    isRedeeming: Boolean,
    onRedeemToken: () -> Unit
) {
    val colors = QuovexTheme.colors
    val hasTokens = streakInfo.rescueTokens > 0

    QuovexCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuovexTheme.spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_streak_freeze_shield),
                        contentDescription = "Streak Rescue",
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                Column {
                    Text(
                        text = "Streak Rescue Token",
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${streakInfo.rescueTokens} Token${if (streakInfo.rescueTokens == 1) "" else "s"} in Reserve",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasTokens) colors.primary else colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

            Text(
                text = "Life happens. If you miss a study session, redeem a Rescue Token to protect your streak. Earn 1 token for every 7 consecutive days of focus.",
                style = QuovexTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

            Button(
                onClick = onRedeemToken,
                enabled = hasTokens && !isRedeeming,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    disabledContainerColor = colors.surfaceVariant
                ),
                shape = QuovexTheme.shapes.medium
            ) {
                if (isRedeeming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = colors.background
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (hasTokens) colors.background else colors.textSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasTokens) "Protect / Heal Streak (1 Token)" else "No Tokens Available",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (hasTokens) colors.background else colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun MilestonesGrid(
    milestones: List<StreakMilestone>
) {
    val colors = QuovexTheme.colors

    Column(
        verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)
    ) {
        milestones.forEach { milestone ->
            QuovexCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QuovexTheme.spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = milestone.iconEmoji,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = milestone.title,
                                    style = QuovexTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (milestone.isUnlocked) colors.primary else colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${milestone.milestoneDays} Days",
                                    style = QuovexTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = milestone.description,
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Status Indicator
                    if (milestone.isUnlocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(QuovexTheme.shapes.pill)
                                .background(colors.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Unlocked",
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${milestone.xpBonus} XP",
                                style = QuovexTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(QuovexTheme.shapes.pill)
                                .background(colors.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${milestone.xpBonus} XP",
                                style = QuovexTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CemeteryTombstoneCard(
    tombstone: CemeteryTombstone,
    onWriteReflection: () -> Unit
) {
    val colors = QuovexTheme.colors

    QuovexCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.surfaceVariant, QuovexTheme.shapes.large)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuovexTheme.spacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_streak_cemetery_rune),
                        contentDescription = "Rune Tombstone",
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(QuovexTheme.spacing.sm))
                    Column {
                        Text(
                            text = "RIP ${tombstone.streakLength}-Day Streak",
                            style = QuovexTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = tombstone.dateRangeFormatted,
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // Cause of Death Pill
                Box(
                    modifier = Modifier
                        .clip(QuovexTheme.shapes.pill)
                        .background(colors.error.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tombstone.causeOfDeath,
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = colors.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

            if (!tombstone.reflectionNote.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(QuovexTheme.shapes.medium)
                        .background(colors.surfaceVariant.copy(alpha = 0.6f))
                        .padding(QuovexTheme.spacing.sm)
                ) {
                    Column {
                        Text(
                            text = "STUDENT REFLECTION",
                            style = QuovexTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.secondary,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${tombstone.reflectionNote}\"",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.textPrimary
                        )
                    }
                }
            } else {
                TextButton(
                    onClick = onWriteReflection,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "✍️ Write Post-Mortem Reflection",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primary
                    )
                }
            }
        }
    }
}
