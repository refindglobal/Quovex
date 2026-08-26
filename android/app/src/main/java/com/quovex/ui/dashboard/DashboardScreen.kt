package com.quovex.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.quovex.R
import com.quovex.domain.model.RecentActivityItem
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.ExamCountdownCard
import com.quovex.ui.components.HeatmapCalendar
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexEmptyState
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexSectionHeader
import com.quovex.ui.components.SubjectBreakdownChart
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onStartTimerClick: () -> Unit = {},
    onDeckClick: (Int) -> Unit = {},
    onAiChatClick: () -> Unit = {},
    onAiNoteParserClick: () -> Unit = {},
    onLibraryClick: () -> Unit = {},
    onStudyPlannerClick: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {},
    onAnalyticsClick: () -> Unit = {},
    onDailyDiagnosticQuizClick: () -> Unit = {},
    onStreakClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val colors = QuovexTheme.colors

    // Android 13+ Notification Permission Request
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        containerColor = colors.background
    ) { padding ->
        when {
            state.isLoading -> {
                QuovexLoading(
                    message = "Loading student command center...",
                    modifier = Modifier.padding(padding)
                )
            }
            state.isError -> {
                QuovexErrorState(
                    title = "Dashboard Error",
                    message = state.errorMessage ?: "Failed to load dashboard metrics.",
                    onRetry = { viewModel.loadDashboardData() },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                val todayHours = (state.todayFocusMinutes / 60.0)
                val targetHours = (state.targetMinutes / 60.0)
                val progressPercentInt = (state.progressPercent * 100).toInt()
                val visualProgress = state.progressPercent.coerceIn(0f, 1f)

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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = QuovexTheme.spacing.xl, vertical = QuovexTheme.spacing.base)
                ) {
                    // ── 1. HEADER (Greeting, Name, Streak, Avatar) ─────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = avatarResId),
                                    contentDescription = "User Avatar ${state.userProfile.avatarId}",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, colors.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(colors.primary, CircleShape)
                                        .border(2.dp, colors.background, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }

                            Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))

                            Column {
                                Text(
                                    text = "${state.greeting},",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = state.userProfile.name.ifBlank { "Aspirant" },
                                    style = QuovexTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1
                                )
                            }
                        }

                        // Streak Chip & Rescue Token (Clickable to Streak & Cemetery Screen)
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier
                                .clip(QuovexTheme.shapes.medium)
                                .clickable(onClick = onStreakClick)
                                .padding(4.dp)
                        ) {
                            QuovexChip(
                                label = state.streakInfo.milestoneTitle ?: "${state.streakDays} Day Streak",
                                isSelected = true,
                                onClick = onStreakClick,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Streak flame",
                                        tint = colors.warning,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            if (state.streakInfo.rescueTokens > 0) {
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                                Text(
                                    text = "🪙 ${state.streakInfo.rescueTokens} Rescue Token",
                                    style = QuovexTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    // Rescue Token Alert Banner (if missed yesterday and can restore)
                    if (state.streakInfo.canUseRescueToken) {
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.warning.copy(alpha = 0.15f),
                            borderColor = colors.warning.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(QuovexTheme.spacing.base),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Streak at Risk!",
                                        style = QuovexTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.warning
                                    )
                                    Text(
                                        text = "You missed yesterday. Spend 1 Rescue Token to keep your streak alive.",
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textPrimary
                                    )
                                }

                                QuovexButton(
                                    text = "Restore",
                                    onClick = { viewModel.useRescueToken() },
                                    variant = QuovexButtonVariant.Primary,
                                    height = 36.dp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                    // ── 2. TODAY'S GOAL CARD ───────────────────────────────────────
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface,
                        elevation = QuovexTheme.elevation.card
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(QuovexTheme.spacing.xl),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TODAY'S GOAL",
                                    style = QuovexTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))
                                Text(
                                    text = if (state.hasGoal) {
                                        String.format("%.1f / %.1f hrs", todayHours, targetHours)
                                    } else {
                                        String.format("%.1f hrs", todayHours)
                                    },
                                    style = QuovexTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                                Text(
                                    text = when {
                                        !state.hasGoal -> "No daily goal set"
                                        state.isGoalExceeded -> "Goal exceeded! 🔥 (${progressPercentInt}%)"
                                        state.isGoalCompleted -> "Daily target achieved! 🎯"
                                        else -> "$progressPercentInt% complete • ${state.userProfile.targetExam}"
                                    },
                                    style = QuovexTheme.typography.bodySmall,
                                    color = if (state.isGoalCompleted) colors.primary else colors.textSecondary
                                )
                            }

                            // Progress Ring
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(80.dp)
                                    .semantics {
                                        progressBarRangeInfo = ProgressBarRangeInfo(visualProgress, 0f..1f)
                                    }
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = colors.surfaceVariant,
                                    strokeWidth = 8.dp
                                )
                                CircularProgressIndicator(
                                    progress = { visualProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = if (state.isGoalCompleted) colors.success else colors.primary,
                                    strokeWidth = 8.dp
                                )
                                if (state.isGoalCompleted) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Goal Complete",
                                        tint = colors.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$progressPercentInt%",
                                        style = QuovexTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                    // ── 3. PRIMARY FOCUS CTA ────────────────────────────────────────
                    val isActiveSession = state.activeSession.isActive
                    QuovexButton(
                        text = if (isActiveSession) {
                            "Continue Focus (${state.activeSession.subject.ifBlank { "Active Session" }}) →"
                        } else {
                            "Start Focus →"
                        },
                        onClick = onStartTimerClick,
                        variant = if (isActiveSession) QuovexButtonVariant.Secondary else QuovexButtonVariant.Primary,
                        leadingIcon = {
                            Icon(
                                imageVector = if (isActiveSession) Icons.Filled.Timer else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = if (isActiveSession) colors.primary else colors.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                    // ── 3B. EXAM COUNTDOWN ──────────────────────────────────────────
                    ExamCountdownCard(countdown = state.examCountdown)

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                    // ── 3C. AI STUDY PLAN ROADMAP WIDGET ────────────────────────────
                    val activePlan = state.activeStudyPlan
                    val todayTasks = state.todayStudyTasks

                    QuovexCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onStudyPlannerClick),
                        backgroundColor = colors.surface,
                        borderColor = colors.border
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(QuovexTheme.spacing.base)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (activePlan != null) "TODAY'S STUDY PLAN" else "AI STUDY PLANNER",
                                        style = QuovexTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary,
                                        letterSpacing = 1.1.sp
                                    )
                                }

                                Text(
                                    text = if (activePlan != null) "Roadmap →" else "Create Plan →",
                                    style = QuovexTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (activePlan != null) {
                                val completedCount = todayTasks.count { it.isCompleted }
                                Text(
                                    text = "${activePlan.targetExam} • $completedCount/${todayTasks.size} tasks done today",
                                    style = QuovexTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                val nextTask = todayTasks.firstOrNull { !it.isCompleted }
                                if (nextTask != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Next: ${nextTask.taskType.icon} ${nextTask.topic} (${nextTask.estimatedMinutes}m)",
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text(
                                    text = "Synthesize a personalized day-by-day exam roadmap",
                                    style = QuovexTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "AI balances chapter theory, active recall, and problem solving",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                    // ── 4. JUMP BACK IN ─────────────────────────────────────────────
                    QuovexSectionHeader(
                        title = "Jump Back In"
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                    val jumpItem = state.jumpBackInItem
                    if (jumpItem == null) {
                        QuovexCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            backgroundColor = colors.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(QuovexTheme.spacing.lg),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                                Text(
                                    text = "Start your first study session",
                                    style = QuovexTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                                Text(
                                    text = "Launch focus or create flashcards to begin.",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    } else {
                        val bgRes = when (jumpItem.subject.lowercase()) {
                            "physics" -> R.drawable.deck_physics_bg
                            "chemistry" -> R.drawable.deck_chemistry_bg
                            "maths", "mathematics" -> R.drawable.deck_maths_bg
                            "biology" -> R.drawable.deck_biology_bg
                            "history" -> R.drawable.deck_history_bg
                            else -> R.drawable.deck_history_bg
                        }

                        QuovexCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp),
                            elevation = QuovexTheme.elevation.card,
                            onClick = { onDeckClick(jumpItem.deckId) }
                        ) {
                            Image(
                                painter = painterResource(id = bgRes),
                                contentDescription = jumpItem.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.4f),
                                                Color.Black.copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(QuovexTheme.spacing.base),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    QuovexChip(
                                        label = jumpItem.subject.uppercase(),
                                        isSelected = false
                                    )
                                    Text(
                                        text = "+150 XP",
                                        style = QuovexTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = jumpItem.title,
                                        style = QuovexTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxs))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${jumpItem.dueCount} Due Today • ${jumpItem.masteryPercent}% Mastered",
                                            style = QuovexTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = "Study Now →",
                                            style = QuovexTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                    // ── 5. WEEKLY PROGRESS (THIS WEEK) ─────────────────────────────
                    QuovexSectionHeader(
                        title = "This Week"
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface,
                        elevation = QuovexTheme.elevation.low
                    ) {
                        Column(modifier = Modifier.padding(QuovexTheme.spacing.base)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                state.weeklyProgress.forEach { day ->
                                    val isGoalDone = day.isGoalCompleted
                                    val hasActivity = day.minutesStudied > 0

                                    val boxBackground = when {
                                        isGoalDone -> colors.primary
                                        hasActivity -> colors.primary.copy(alpha = 0.35f)
                                        else -> colors.surfaceVariant
                                    }

                                    val boxBorder = when {
                                        day.isToday -> colors.primary
                                        isGoalDone -> colors.primary
                                        else -> colors.border
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(QuovexTheme.shapes.small)
                                                .background(boxBackground)
                                                .border(
                                                    width = if (day.isToday) 2.dp else 1.dp,
                                                    color = boxBorder,
                                                    shape = QuovexTheme.shapes.small
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isGoalDone) {
                                                Text(
                                                    text = "✓",
                                                    color = colors.onPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    style = QuovexTheme.typography.labelMedium
                                                )
                                            } else if (hasActivity) {
                                                Text(
                                                    text = "${day.minutesStudied}m",
                                                    color = colors.textPrimary,
                                                    style = QuovexTheme.typography.labelSmall,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))
                                        Text(
                                            text = day.dayShort,
                                            style = QuovexTheme.typography.labelSmall,
                                            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
                                            color = if (day.isToday) colors.primary else colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                    // ── 5B. CONSISTENCY HEATMAP (28 DAYS) ───────────────────────────
                    if (state.heatmapGrid.isNotEmpty()) {
                        HeatmapCalendar(days = state.heatmapGrid)
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                    }

                    // ── 5C. SUBJECT BREAKDOWN (30 DAYS) ─────────────────────────────
                    if (state.subjectBreakdown.isNotEmpty()) {
                        SubjectBreakdownChart(breakdown = state.subjectBreakdown)
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                    // ── 6. FLASHCARD DUE REMINDER ──────────────────────────────────
                    QuovexSectionHeader(
                        title = "Flashcards Due"
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                    val dueCount = state.dueFlashcards.totalDueCount
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = colors.surface,
                        elevation = QuovexTheme.elevation.low,
                        onClick = {
                            if (dueCount > 0 && state.dueFlashcards.primaryDeckId != null) {
                                onDeckClick(state.dueFlashcards.primaryDeckId!!)
                            } else {
                                onLibraryClick()
                            }
                        }
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
                                Icon(
                                    imageVector = if (dueCount > 0) Icons.Filled.Style else Icons.Filled.CheckCircle,
                                    contentDescription = "Flashcard due status",
                                    tint = if (dueCount > 0) colors.warning else colors.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                                Column {
                                    Text(
                                        text = if (dueCount > 0) {
                                            "$dueCount cards ready for review"
                                        } else {
                                            "You're all caught up."
                                        },
                                        style = QuovexTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = if (dueCount > 0) {
                                            "Review scheduled spaced repetition cards"
                                        } else {
                                            "No cards currently due for review"
                                        },
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            if (dueCount > 0) {
                                Text(
                                    text = "Review Now →",
                                    style = QuovexTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    // ── 7. RECENT ACTIVITY SECTION ──────────────────────────────────
                    if (state.recentActivities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                        QuovexSectionHeader(
                            title = "Recent Activity"
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                        Column(verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                            state.recentActivities.forEach { activity ->
                                RecentActivityCard(activity = activity)
                            }
                        }
                    }

                    // ── 7A. DAILY DIAGNOSTIC QUIZ (MODULE C: Q-001) ───────────
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onDailyDiagnosticQuizClick,
                        backgroundColor = colors.surface,
                        borderColor = colors.primary.copy(alpha = 0.5f)
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
                                        .background(colors.primary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.School,
                                        contentDescription = "Daily Quiz",
                                        tint = colors.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                                Column {
                                    Text(
                                        text = "Today's Daily Diagnostic Quiz",
                                        style = QuovexTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "5 questions from today's topics • Auto-remedial queueing",
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = "Start →",
                                style = QuovexTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                    // ── 7B. PERFORMANCE ANALYTICS & INSIGHTS CTA ──────────────────
                    QuovexCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onAnalyticsClick,
                        backgroundColor = colors.surface,
                        borderColor = colors.primary.copy(alpha = 0.4f)
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
                                        .background(colors.primary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = "Analytics",
                                        tint = colors.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))
                                Column {
                                    Text(
                                        text = "Study Analytics & Performance",
                                        style = QuovexTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "24h Productivity curve, AI insights & Weekly PDF",
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Text(
                                text = "View →",
                                style = QuovexTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                    // ── 8. QUICK ACTIONS ───────────────────────────────────────────
                    QuovexSectionHeader(
                        title = "Quick Actions"
                    )

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.md)
                    ) {
                        // AI Tutor
                        QuovexCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = colors.surface,
                            elevation = QuovexTheme.elevation.low,
                            onClick = onAiChatClick
                        ) {
                            Column(
                                modifier = Modifier.padding(QuovexTheme.spacing.base),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Psychology,
                                    contentDescription = "AI Tutor",
                                    tint = colors.warning,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                                Text(
                                    text = "AI Doubt Tutor",
                                    style = QuovexTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Step solver",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        // AI Notes
                        QuovexCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = colors.surface,
                            elevation = QuovexTheme.elevation.low,
                            onClick = onAiNoteParserClick
                        ) {
                            Column(
                                modifier = Modifier.padding(QuovexTheme.spacing.base),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "AI Notes",
                                    tint = colors.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                                Text(
                                    text = "AI Note Parser",
                                    style = QuovexTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "OCR summary",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        // Library
                        QuovexCard(
                            modifier = Modifier.weight(1f),
                            backgroundColor = colors.surface,
                            elevation = QuovexTheme.elevation.low,
                            onClick = onLibraryClick
                        ) {
                            Column(
                                modifier = Modifier.padding(QuovexTheme.spacing.base),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = "Library",
                                    tint = colors.info,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))
                                Text(
                                    text = "Knowledge Base",
                                    style = QuovexTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Decks & cards",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }

                    // ── 9. GOOGLE ADMOB BANNER (FREE TIER ONLY) ───────────────────
                    if (!state.isAdFree) {
                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                        com.quovex.ui.components.QuovexBannerAd(
                            isAdFree = state.isAdFree
                        )
                    }

                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.xxxl))
                }
            }
        }
    }
}

@Composable
private fun RecentActivityCard(
    activity: RecentActivityItem,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors
    val timeAgo = formatTimeAgo(activity.timestamp)

    QuovexCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = colors.surface,
        elevation = QuovexTheme.elevation.none
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuovexTheme.spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(QuovexTheme.spacing.md))

                Column {
                    Text(
                        text = "${activity.title} • ${activity.durationMinutes}m",
                        style = QuovexTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$timeAgo • Focus Score ${activity.focusScore}%",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = "+${activity.durationMinutes * 5} XP",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary
            )
        }
    }
}

private fun formatTimeAgo(timestampMillis: Long): String {
    val now = Instant.now()
    val sessionTime = Instant.ofEpochMilli(timestampMillis)
    val minutes = ChronoUnit.MINUTES.between(sessionTime, now)
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> "${minutes / 1440}d ago"
    }
}
