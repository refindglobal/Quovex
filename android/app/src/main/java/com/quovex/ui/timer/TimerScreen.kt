package com.quovex.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.model.FocusMode
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.model.SoundscapePreset
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
import com.quovex.ui.components.SoundscapeWaveformVisualizer
import com.quovex.ui.timer.components.CameraFocusPreview
import com.quovex.ui.timer.components.DistractionShieldSheet
import com.quovex.ui.timer.components.SoundscapeSelectorSheet

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
                    onToggleCameraFocus = { viewModel.toggleCameraFocusDetection() },
                    onOpenSoundscapeSheet = { viewModel.openSoundscapeSheet() },
                    onOpenDistractionShield = { viewModel.openDistractionShieldSheet() },
                    onStartSession = { viewModel.startSession(context) }
                )

                TimerScreenState.ACTIVE -> ActiveSessionContent(
                    state = state,
                    onOpenSoundscapeSheet = { viewModel.openSoundscapeSheet() },
                    onToggleSoundscapePlay = { viewModel.toggleSoundscapePlay() },
                    onCameraPermissionResult = { viewModel.updateCameraPermission(it) },
                    onFrameAnalyzed = { viewModel.onFrameAnalyzed(it) },
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

            // Soundscape Selector Modal Bottom Sheet
            if (state.isSoundscapeSheetOpen) {
                SoundscapeSelectorSheet(
                    soundscapeState = state.soundscapeState,
                    onDismiss = { viewModel.closeSoundscapeSheet() },
                    onSelectPreset = { viewModel.selectSoundscapePreset(it) },
                    onSetVolume = { viewModel.setSoundscapeVolume(it) },
                    onToggleAutoPlay = { viewModel.toggleSoundscapeAutoPlay(it) },
                    onTogglePlay = { viewModel.toggleSoundscapePlay() }
                )
            }

            // Distraction Shield App Blocker Sheet
            if (state.isDistractionShieldSheetOpen) {
                DistractionShieldSheet(
                    shieldState = state.distractionShieldState,
                    onDismiss = { viewModel.closeDistractionShieldSheet() },
                    onToggleApp = { pkg, blocked -> viewModel.toggleAppBlocked(pkg, blocked) },
                    onToggleCategory = { cat, blocked -> viewModel.toggleCategoryBlocked(cat, blocked) },
                    onToggleShield = { enabled -> viewModel.toggleShield(enabled) }
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
    onToggleCameraFocus: () -> Unit,
    onOpenSoundscapeSheet: () -> Unit,
    onOpenDistractionShield: () -> Unit,
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

            // Ambient Soundscape Card
            Text(
                text = "AMBIENT SOUNDSCAPE",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(spacing.xs))

            QuovexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSoundscapeSheet),
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
                                .background(colors.primaryGlow, CircleShape)
                                .border(1.dp, colors.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.soundscapeState.selectedPreset.iconEmoji,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(Modifier.width(spacing.md))

                        Column {
                            Text(
                                text = state.soundscapeState.selectedPreset.title,
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${(state.soundscapeState.volume * 100).toInt()}% vol • ${if (state.soundscapeState.isAutoPlayWithTimerEnabled) "Auto-plays on start" else "Manual"}",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Tune",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tune soundscape",
                            tint = colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.xl))

            // Multi-Layer Distraction Shield Card
            Text(
                text = "APP DISTRACTION SHIELD",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(spacing.xs))

            QuovexCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenDistractionShield),
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
                                .background(colors.primaryGlow, CircleShape)
                                .border(1.dp, colors.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (state.distractionShieldState.isShieldEnabled) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(spacing.md))

                        Column {
                            Text(
                                text = "App Distraction Shield",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${state.distractionShieldState.blockedCount} apps blocked • ${if (state.distractionShieldState.isAccessibilityServiceEnabled) "Shield Active" else "Setup Required"}",
                                style = QuovexTheme.typography.bodySmall,
                                color = if (state.distractionShieldState.isAccessibilityServiceEnabled) colors.primary else colors.textSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Configure",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Configure shield",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.xl))

            // AI Camera Focus Detection Card
            Text(
                text = "AI CAMERA FOCUS DETECTION",
                style = QuovexTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(spacing.xs))

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
                                .background(colors.primaryGlow, CircleShape)
                                .border(1.dp, colors.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = if (state.focusTrackingState.isEnabled) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(Modifier.width(spacing.md))

                        Column {
                            Text(
                                text = "Camera Focus & Drowsiness",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "On-device ML eye openness & posture tracking",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "100% On-Device ML • Zero Video Saved",
                                    fontSize = 10.sp,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Switch(
                        checked = state.focusTrackingState.isEnabled,
                        onCheckedChange = { onToggleCameraFocus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surfaceVariant
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = if (state.focusTrackingState.isEnabled) "Camera focus detection enabled" else "Camera focus detection disabled"
                        }
                    )
                }
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
    onOpenSoundscapeSheet: () -> Unit,
    onToggleSoundscapePlay: () -> Unit,
    onCameraPermissionResult: (Boolean) -> Unit,
    onFrameAnalyzed: (FocusFrameResult) -> Unit,
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
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    style = TextStyle(
                        fontFeatureSettings = "tnum"
                    ),
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

        // Ambient Soundscape & Camera Focus Mini Controllers
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            // Ambient Soundscape Mini Controller Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceElevated)
                    .border(1.dp, if (state.soundscapeState.isPlaying) colors.primary else colors.border, RoundedCornerShape(20.dp))
                    .clickable(onClick = onOpenSoundscapeSheet)
                    .padding(horizontal = spacing.md, vertical = spacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = state.soundscapeState.selectedPreset.iconEmoji,
                        fontSize = 14.sp
                    )
                    Text(
                        text = state.soundscapeState.selectedPreset.title,
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.soundscapeState.isPlaying) colors.primary else colors.textSecondary
                    )
                    if (state.soundscapeState.isPlaying) {
                        SoundscapeWaveformVisualizer(
                            isPlaying = true,
                            barCount = 6,
                            maxHeight = 14.dp,
                            barWidth = 2.dp,
                            barSpacing = 2.dp
                        )
                    }
                    if (state.soundscapeState.selectedPreset.id != "none") {
                        IconButton(
                            onClick = onToggleSoundscapePlay,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (state.soundscapeState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle audio",
                                tint = if (state.soundscapeState.isPlaying) colors.primary else colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Camera AI Focus Preview / Status Pill
            if (state.focusTrackingState.isEnabled) {
                CameraFocusPreview(
                    focusState = state.focusTrackingState,
                    onPermissionResult = onCameraPermissionResult,
                    onFrameAnalyzed = onFrameAnalyzed
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
                    .semantics { contentDescription = "End focus session early" }
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
            .padding(horizontal = spacing.xl, vertical = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Trophy / Success Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(colors.primaryGlow, CircleShape)
                    .border(2.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (summary.isCompleted) Icons.Filled.EmojiEvents else Icons.Filled.HourglassBottom,
                    contentDescription = null,
                    tint = if (summary.isCompleted) colors.primary else colors.warning,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(spacing.base))

            Text(
                text = if (summary.isCompleted) "Focus Goal Crushed!" else "Session Stopped",
                style = QuovexTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (summary.isCompleted)
                    "Unbroken deep concentration recorded."
                else
                    "Every minute counts toward mastery. Keep going!",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(spacing.xxl))

            // Metrics Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                QuovexCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${summary.actualDurationMinutes}",
                            style = QuovexTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primary
                        )
                        Text(
                            text = "MINUTES FOCUSED",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                QuovexCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(spacing.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = summary.subject,
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "SUBJECT",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // AI Camera Focus Score Card if enabled
            if (summary.cameraTrackingEnabled || summary.focusScore != null) {
                Spacer(Modifier.height(spacing.md))

                QuovexCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = colors.surfaceElevated,
                    borderColor = colors.primary,
                    borderWidth = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.base),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "👁️ AI Camera Focus Score",
                                    style = QuovexTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Distractions: ${summary.distractionsCount} • Drowsiness: ${summary.drowsinessCount}",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Text(
                            text = "${summary.focusScore ?: 100}%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(spacing.md))

            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.base),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(spacing.sm))
                        Text(
                            text = "Mode: ${summary.modeName}",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textPrimary
                        )
                    }

                    if (summary.strictFocusEnabled) {
                        QuovexChip(
                            label = "Strict Mode",
                            isSelected = true
                        )
                    }
                }
            }
        }

        // Return to Setup CTA
        QuovexButton(
            text = "Back to Focus Zone",
            onClick = onDismiss,
            variant = QuovexButtonVariant.Primary,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Dismiss summary and return to timer setup" }
        )
    }
}

// ── 4. Dialogs ────────────────────────────────────────────────────────────────

@Composable
private fun CustomDurationDialog(
    initialFocusMinutes: Int,
    initialBreakMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (focusMinutes: Int, breakMinutes: Int) -> Unit
) {
    var focusText by remember { androidx.compose.runtime.mutableStateOf(initialFocusMinutes.toString()) }
    var breakText by remember { androidx.compose.runtime.mutableStateOf(initialBreakMinutes.toString()) }
    val spacing = QuovexTheme.spacing

    QuovexDialog(
        title = "Custom Session Duration",
        onDismissRequest = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            QuovexTextField(
                value = focusText,
                onValueChange = { focusText = it },
                label = "Focus Duration (Minutes)",
                placeholder = "e.g. 45"
            )
            QuovexTextField(
                value = breakText,
                onValueChange = { breakText = it },
                label = "Break Duration (Minutes)",
                placeholder = "e.g. 10"
            )

            Spacer(Modifier.height(spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                QuovexButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = QuovexButtonVariant.Secondary,
                    modifier = Modifier.width(100.dp)
                )
                Spacer(Modifier.width(spacing.sm))
                QuovexButton(
                    text = "Apply",
                    onClick = {
                        val focus = focusText.toIntOrNull() ?: 30
                        val breakMins = breakText.toIntOrNull() ?: 5
                        onConfirm(focus, breakMins)
                    },
                    modifier = Modifier.width(100.dp)
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
    com.quovex.ui.components.QuovexConfirmDialog(
        onDismissRequest = onDismiss,
        title = "End Focus Session Early?",
        message = "Are you sure you want to stop now? Your study minutes will be saved to your daily progress.",
        confirmText = "End Session",
        dismissText = "Keep Focusing",
        isDanger = true,
        onConfirm = onConfirm
    )
}
