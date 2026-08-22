package com.quovex.ui.timer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.FocusMode
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.util.TimerFormatter
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexDialog
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun TimerScreen(
    viewModel: TimerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = QuovexTheme.colors

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = when (state.screenState) {
                    TimerScreenState.SETUP -> "Focus Zone"
                    TimerScreenState.ACTIVE -> "Focus Session"
                    TimerScreenState.SUMMARY -> "Session Summary"
                }
            )
        },
        containerColor = colors.background
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.screenState) {
                TimerScreenState.SETUP -> TimerSetupContent(
                    state = state,
                    onSelectSubject = { viewModel.selectSubject(it) },
                    onSelectMode = { viewModel.selectMode(it) },
                    onOpenCustomDialog = { viewModel.openCustomDurationDialog() },
                    onToggleStrictFocus = { viewModel.toggleStrictFocus() },
                    onStartSession = { viewModel.startSession(context) }
                )

                TimerScreenState.ACTIVE -> ActiveSessionContent(
                    state = state,
                    onEndEarlyClick = { viewModel.requestEndEarly() }
                )

                TimerScreenState.SUMMARY -> state.latestSummary?.let { summary ->
                    SessionSummaryContent(
                        summary = summary,
                        onDismiss = { viewModel.dismissSummary() }
                    )
                }
            }

            // Error banner if any
            if (state.errorMessage != null) {
                QuovexErrorState(
                    message = state.errorMessage ?: "",
                    onRetry = { viewModel.clearError() },
                    fullScreen = false,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Custom Duration Dialog
            if (state.isCustomDurationDialogOpen) {
                CustomDurationDialog(
                    initialFocusMinutes = (state.selectedMode as? FocusMode.Custom)?.customFocusMinutes ?: 30,
                    initialBreakMinutes = (state.selectedMode as? FocusMode.Custom)?.customBreakMinutes ?: 5,
                    onDismiss = { viewModel.closeCustomDurationDialog() },
                    onConfirm = { focus, breakMins -> viewModel.setCustomDuration(focus, breakMins) }
                )
            }

            // End Early Confirmation Dialog
            if (state.isEndEarlyDialogOpen) {
                EndEarlyConfirmationDialog(
                    onDismiss = { viewModel.dismissEndEarlyDialog() },
                    onConfirm = { viewModel.confirmEndEarly(context) }
                )
            }
        }
    }
}

// ── 1. Timer Setup Screen ─────────────────────────────────────────────────────

@Composable
private fun TimerSetupContent(
    state: TimerUiState,
    onSelectSubject: (String) -> Unit,
    onSelectMode: (FocusMode) -> Unit,
    onOpenCustomDialog: () -> Unit,
    onToggleStrictFocus: () -> Unit,
    onStartSession: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Ready to Focus?",
                style = QuovexTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Select your subject and study mode to begin.",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(spacing.xl))

            // Subject Selector Section
            Text(
                text = "SUBJECT",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(spacing.xs))

            if (state.availableSubjects.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(vertical = spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    items(state.availableSubjects) { subject ->
                        QuovexChip(
                            label = subject,
                            isSelected = state.selectedSubject.equals(subject, ignoreCase = true),
                            onClick = { onSelectSubject(subject) }
                        )
                    }
                }
            } else {
                QuovexChip(
                    label = state.selectedSubject,
                    isSelected = true,
                    onClick = {}
                )
            }

            Spacer(Modifier.height(spacing.xl))

            // Mode Selector Section
            Text(
                text = "FOCUS PRESET",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(spacing.xs))

            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                FocusModeCard(
                    mode = FocusMode.Pomodoro,
                    isSelected = state.selectedMode is FocusMode.Pomodoro,
                    onClick = { onSelectMode(FocusMode.Pomodoro) }
                )
                FocusModeCard(
                    mode = FocusMode.DeepWork,
                    isSelected = state.selectedMode is FocusMode.DeepWork,
                    onClick = { onSelectMode(FocusMode.DeepWork) }
                )
                FocusModeCard(
                    mode = FocusMode.LongDeepWork,
                    isSelected = state.selectedMode is FocusMode.LongDeepWork,
                    onClick = { onSelectMode(FocusMode.LongDeepWork) }
                )
                FocusModeCard(
                    mode = state.selectedMode as? FocusMode.Custom ?: FocusMode.Custom(),
                    isSelected = state.selectedMode is FocusMode.Custom,
                    onClick = onOpenCustomDialog
                )
            }

            Spacer(Modifier.height(spacing.xl))

            // Strict Focus Card
            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface,
                elevation = QuovexTheme.elevation.card
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.base),
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
                                .background(colors.surfaceVariant, CircleShape)
                                .border(1.dp, colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shield,
                                contentDescription = null,
                                tint = if (state.strictFocusEnabled) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(spacing.md))

                        Column {
                            Text(
                                text = "Strict Focus Mode",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Monitors distractions during deep work",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Switch(
                        checked = state.strictFocusEnabled,
                        onCheckedChange = { onToggleStrictFocus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surfaceVariant
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = if (state.strictFocusEnabled) "Strict focus enabled" else "Strict focus disabled"
                        }
                    )
                }
            }
        }

        // Start Session CTA
        Column(modifier = Modifier.padding(top = spacing.xxl, bottom = spacing.md)) {
            QuovexButton(
                text = "Start Focus Session — ${state.selectedMode.focusDurationMinutes}m",
                onClick = onStartSession,
                variant = QuovexButtonVariant.Primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Start ${state.selectedMode.focusDurationMinutes} minute focus session for ${state.selectedSubject}"
                    }
            )
        }
    }
}

@Composable
private fun FocusModeCard(
    mode: FocusMode,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (isSelected) colors.surfaceElevated else colors.surface,
        borderColor = if (isSelected) colors.primary else colors.border,
        borderWidth = if (isSelected) 2.dp else 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.base, vertical = spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = mode.title,
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) colors.primary else colors.textPrimary
                )
                Text(
                    text = mode.description,
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ── 2. Active Session Screen ──────────────────────────────────────────────────

@Composable
private fun ActiveSessionContent(
    state: TimerUiState,
    onEndEarlyClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    // Breathing pulse animation for focus glow
    val infiniteTransition = rememberInfiniteTransition(label = "PulseGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Subject Tag & Mode Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            QuovexChip(
                label = state.activeSession.subject.ifBlank { "Deep Work" },
                isSelected = false
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "${state.activeSession.modeName} Session",
                style = QuovexTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }

        // Circular Timer Visualizer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            // Pulsing glow aura
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(pulseScale)
                    .background(colors.primaryGlow, CircleShape)
            )

            // Background track
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(250.dp),
                color = colors.surfaceVariant,
                strokeWidth = 10.dp
            )

            // Foreground progress indicator
            CircularProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.size(250.dp),
                color = colors.primary,
                strokeWidth = 10.dp
            )

            // Digital Countdown Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics {
                    contentDescription = "Focus timer: ${state.formattedRemainingTime} remaining for ${state.activeSession.subject}"
                }
            ) {
                Text(
                    text = state.formattedRemainingTime,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(spacing.xs))
                Text(
                    text = "STAY FOCUSED",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = colors.primary
                )
            }
        }

        // Strict Focus Status Card & End Early Action
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.activeSession.strictFocusEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = spacing.lg)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Strict focus active",
                        tint = colors.warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        text = "Strict Focus Active",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.warning
                    )
                }
            }

            QuovexButton(
                text = "End Session Early",
                onClick = onEndEarlyClick,
                variant = QuovexButtonVariant.Danger,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "End this active focus session early" }
            )
        }
    }
}

// ── 3. Session Summary Screen ─────────────────────────────────────────────────

@Composable
private fun SessionSummaryContent(
    summary: SessionSummary,
    onDismiss: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (summary.isCompleted) Icons.Filled.EmojiEvents else Icons.Filled.HourglassBottom,
            contentDescription = null,
            tint = if (summary.isCompleted) colors.warning else colors.textSecondary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(spacing.lg))

        Text(
            text = if (summary.isCompleted) "Session Complete!" else "Session Ended Early",
            style = QuovexTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(spacing.xs))

        Text(
            text = if (summary.isCompleted)
                "Outstanding discipline. Your focus time has been recorded."
            else
                "Good effort. Every minute of focus counts towards mastery.",
            style = QuovexTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(spacing.xl))

        // Factual Summary Card (Zero Fake XP / Score)
        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = QuovexTheme.elevation.card
        ) {
            Column(
                modifier = Modifier.padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Text(
                    text = "Session Details",
                    style = QuovexTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary
                )

                SummaryRow(label = "Subject", value = summary.subject)
                SummaryRow(label = "Preset", value = summary.modeName)
                SummaryRow(
                    label = "Time Focused",
                    value = TimerFormatter.formatDurationMinutes(summary.actualDurationMinutes)
                )
                SummaryRow(
                    label = "Planned",
                    value = TimerFormatter.formatDurationMinutes(summary.plannedDurationMinutes)
                )
                SummaryRow(
                    label = "Strict Focus",
                    value = if (summary.strictFocusEnabled) "Enabled" else "Disabled"
                )
            }
        }

        Spacer(Modifier.height(spacing.xxl))

        QuovexButton(
            text = "Done",
            onClick = onDismiss,
            variant = QuovexButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val colors = QuovexTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = QuovexTheme.typography.bodyMedium, color = colors.textSecondary)
        Text(text = value, style = QuovexTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textPrimary)
    }
}

// ── Dialogs ───────────────────────────────────────────────────────────────────

@Composable
private fun CustomDurationDialog(
    initialFocusMinutes: Int,
    initialBreakMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (focus: Int, breakMins: Int) -> Unit
) {
    var focusText by remember { mutableIntStateOf(initialFocusMinutes) }
    var breakText by remember { mutableIntStateOf(initialBreakMinutes) }
    val spacing = QuovexTheme.spacing

    QuovexDialog(
        onDismissRequest = onDismiss,
        title = "Custom Focus Preset"
    ) {
        Column {
            QuovexTextField(
                value = focusText.toString(),
                onValueChange = { focusText = it.toIntOrNull()?.coerceIn(1, 240) ?: 1 },
                label = "Focus Duration (Minutes)",
                placeholder = "e.g. 45"
            )

            Spacer(Modifier.height(spacing.md))

            QuovexTextField(
                value = breakText.toString(),
                onValueChange = { breakText = it.toIntOrNull()?.coerceIn(1, 60) ?: 1 },
                label = "Break Duration (Minutes)",
                placeholder = "e.g. 10"
            )

            Spacer(Modifier.height(spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                QuovexButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = QuovexButtonVariant.Ghost,
                    modifier = Modifier.padding(end = spacing.sm),
                    height = 44.dp
                )
                QuovexButton(
                    text = "Apply",
                    onClick = { onConfirm(focusText, breakText) },
                    variant = QuovexButtonVariant.Primary,
                    height = 44.dp
                )
            }
        }
    }
}

@Composable
private fun EndEarlyConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val spacing = QuovexTheme.spacing
    val colors = QuovexTheme.colors

    QuovexDialog(
        onDismissRequest = onDismiss,
        title = "End Focus Session?"
    ) {
        Column {
            Text(
                text = "Ending your session early will record the elapsed study time and stop the countdown timer.",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary
            )

            Spacer(Modifier.height(spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                QuovexButton(
                    text = "Continue Session",
                    onClick = onDismiss,
                    variant = QuovexButtonVariant.Ghost,
                    modifier = Modifier.padding(end = spacing.sm),
                    height = 44.dp
                )
                QuovexButton(
                    text = "End Session",
                    onClick = onConfirm,
                    variant = QuovexButtonVariant.Danger,
                    height = 44.dp
                )
            }
        }
    }
}
