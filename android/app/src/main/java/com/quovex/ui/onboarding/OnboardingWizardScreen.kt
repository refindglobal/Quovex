package com.quovex.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexLinearProgressIndicator

data class OnboardingCardOption(
    val title: String,
    val subtitle: String,
    val badgeResId: Int,
    val id: String
)

@Composable
fun OnboardingWizardScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = QuovexTheme.colors

    val examOptions = listOf(
        OnboardingCardOption("JEE Main & Advanced", "Engineering Aspirant • Physics, Chem, Math", R.drawable.badge_exam_jee, "JEE"),
        OnboardingCardOption("NEET UG Medical", "Medical Aspirant • Bio, Chem, Physics", R.drawable.badge_exam_neet, "NEET"),
        OnboardingCardOption("CBSE Class 10/12 Boards", "95%+ Target • NCERT Mastered", R.drawable.badge_exam_cbse, "CBSE"),
        OnboardingCardOption("UPSC & Civil Services", "General Studies & Deep Focus", R.drawable.badge_exam_upsc, "UPSC")
    )

    val obstacleOptions = listOf(
        OnboardingCardOption("Social Media Doomscrolling", "Instagram, Reels, YouTube Shorts distraction", R.drawable.badge_obstacle_social_media, "Social Media Doomscrolling"),
        OnboardingCardOption("Procrastination & Brain Fog", "Trouble initiating deep study sessions", R.drawable.badge_obstacle_procrastination, "Procrastination & Brain Fog"),
        OnboardingCardOption("Hard Concepts & Doubts", "Getting stuck on complex problems & derivations", R.drawable.badge_obstacle_doubts, "Hard Concepts & Doubts")
    )

    val commitmentOptions = listOf(
        OnboardingCardOption("1 Hour / Day", "Consistent Daily Spark • 7h weekly", R.drawable.badge_goal_1hr, "1.0"),
        OnboardingCardOption("2.5 Hours / Day", "Serious Grind • Recommended for Aspirants", R.drawable.badge_goal_2_5hr, "2.5"),
        OnboardingCardOption("4+ Hours / Day", "God Mode • High-Yield Intense Lock-In", R.drawable.badge_goal_4hr, "4.0")
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column(modifier = Modifier.background(colors.background)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 0) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "Step ${state.currentStep + 1} of 3",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(48.dp))
                }

                QuovexLinearProgressIndicator(
                    progress = (state.currentStep + 1) / 3f,
                    height = 4.dp,
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .padding(QuovexTheme.spacing.base)
            ) {
                QuovexButton(
                    text = if (state.currentStep == 2) "Lock In & Build Shield" else "Next Step",
                    onClick = {
                        if (state.currentStep < 2) {
                            viewModel.nextStep()
                        } else {
                            viewModel.finishOnboarding(onOnboardingComplete)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (state.currentStep == 2) Icons.Filled.Lock else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    },
                    isLoading = state.isSaving
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(QuovexTheme.spacing.base)
        ) {
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "OnboardingStepContent"
            ) { step ->
                when (step) {
                    0 -> {
                        OnboardingStepSection(
                            title = "Choose Your Battlefield 🎯",
                            subtitle = "Select your primary target exam so Quovex can tailor your curriculum and AI tutor.",
                            options = examOptions,
                            selectedId = state.selectedExam,
                            onSelect = { viewModel.selectExam(it) }
                        )
                    }
                    1 -> {
                        OnboardingStepSection(
                            title = "What Kills Your Focus? 🛡️",
                            subtitle = "Identify your primary study obstacle. We will configure your Distraction Blocker shield.",
                            options = obstacleOptions,
                            selectedId = state.selectedObstacle,
                            onSelect = { viewModel.selectObstacle(it) }
                        )
                    }
                    2 -> {
                        OnboardingStepSection(
                            title = "Set Your Daily Lock-In Target ⏱️",
                            subtitle = "Commit to a daily deep work study goal. Consistency builds unbroken streaks.",
                            options = commitmentOptions,
                            selectedId = state.dailyGoalHours.toString(),
                            onSelect = { viewModel.setDailyGoal(it.toFloatOrNull() ?: 2.5f) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepSection(
    title: String,
    subtitle: String,
    options: List<OnboardingCardOption>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    val colors = QuovexTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = QuovexTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xs))

        Text(
            text = subtitle,
            style = QuovexTheme.typography.bodyMedium,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

        Column(
            verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.md),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                val isSelected = option.title == selectedId || option.id == selectedId || option.subtitle.contains(selectedId)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(QuovexTheme.shapes.large)
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(
                                    listOf(colors.primaryContainer.copy(alpha = 0.5f), colors.surface)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(colors.surfaceElevated, colors.surface)
                                )
                            }
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.primary else colors.border,
                            shape = QuovexTheme.shapes.large
                        )
                        .clickable { onSelect(option.title) }
                        .padding(QuovexTheme.spacing.base)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = option.badgeResId),
                            contentDescription = option.title,
                            modifier = Modifier.size(52.dp)
                        )

                        Spacer(modifier = Modifier.width(QuovexTheme.spacing.base))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                style = QuovexTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) colors.primary else colors.textPrimary
                            )
                            Text(
                                text = option.subtitle,
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = colors.background,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
