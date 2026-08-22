package com.quovex.ui.onboarding

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quovex.R
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexLinearProgressIndicator
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar

@Composable
fun OnboardingWizardScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val colors = QuovexTheme.colors

    val avatarList = listOf(
        1 to R.drawable.avatar_1,
        2 to R.drawable.avatar_2,
        3 to R.drawable.avatar_3,
        4 to R.drawable.avatar_4,
        5 to R.drawable.avatar_5,
        6 to R.drawable.avatar_6,
        7 to R.drawable.avatar_7,
        8 to R.drawable.avatar_8,
        9 to R.drawable.avatar_9,
        10 to R.drawable.avatar_10,
        11 to R.drawable.avatar_11,
        12 to R.drawable.avatar_12
    )

    val examOptions = listOf(
        "JEE Advanced / Mains",
        "NEET UG Medical",
        "UPSC CSE / Civil Services",
        "Class 12 Board Prep",
        "Software Engineering & DSA",
        "Custom Learning Goal"
    )

    Scaffold(
        topBar = {
            Column {
                QuovexTopAppBar(
                    title = "Profile Setup",
                    onBackClick = if (state.currentStep > 0) { { viewModel.previousStep() } } else null
                )
                QuovexLinearProgressIndicator(
                    progress = (state.currentStep + 1) / 4f,
                    height = 4.dp,
                    color = colors.primary,
                    trackColor = colors.surfaceVariant
                )
            }
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = QuovexTheme.spacing.xl, vertical = QuovexTheme.spacing.base),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                when (state.currentStep) {
                    // --- STEP 0: Avatar & Name ---
                    0 -> {
                        Text(
                            text = "Let's set up your profile.",
                            style = QuovexTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Choose your avatar and enter your name.",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                        Text(
                            text = "Select Avatar",
                            style = QuovexTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.sm))

                        // 12 Avatar Grid
                        Box(modifier = Modifier.height(200.dp)) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(avatarList) { (id, resId) ->
                                    val isSelected = state.selectedAvatarId == id
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) colors.primary.copy(alpha = 0.2f) else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) colors.primary else colors.border,
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.selectAvatar(id) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = resId),
                                            contentDescription = "Avatar $id",
                                            modifier = Modifier.size(56.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                        QuovexTextField(
                            value = state.nameInput,
                            onValueChange = { viewModel.onNameChanged(it) },
                            label = "What should we call you?",
                            placeholder = "e.g. Arjun Sharma",
                            singleLine = true
                        )
                    }

                    // --- STEP 1: Target Exam ---
                    1 -> {
                        Text(
                            text = "What are you preparing for?",
                            style = QuovexTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "We will tailor your AI tutor, decks and study roadmap accordingly.",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                        Column(verticalArrangement = Arrangement.spacedBy(QuovexTheme.spacing.sm)) {
                            examOptions.forEach { exam ->
                                val isSelected = state.selectedExam == exam
                                QuovexCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backgroundColor = if (isSelected) colors.surface else colors.surfaceVariant,
                                    borderColor = if (isSelected) colors.primary else colors.border,
                                    onClick = { viewModel.selectExam(exam) }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(QuovexTheme.spacing.base),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = exam,
                                            style = QuovexTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) colors.primary else colors.textPrimary
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = "Selected",
                                                tint = colors.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- STEP 2: Daily Commitment ---
                    2 -> {
                        Text(
                            text = "Commit to your daily focus goal.",
                            style = QuovexTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Spaced repetition and deep focus build massive momentum.",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.xl))

                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surface,
                            elevation = QuovexTheme.elevation.card
                        ) {
                            Column(
                                modifier = Modifier.padding(QuovexTheme.spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${String.format("%.1f", state.dailyGoalHours)} Hours / Day",
                                    style = QuovexTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colors.primary
                                )

                                Text(
                                    text = if (state.dailyGoalHours >= 6f) "⚡ Extreme Aspirant Mode" else if (state.dailyGoalHours >= 4f) "🔥 High Intensity Focus" else "✨ Balanced Daily Consistency",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                                Slider(
                                    value = state.dailyGoalHours,
                                    onValueChange = { viewModel.setDailyGoal(it) },
                                    valueRange = 1.0f..10.0f,
                                    steps = 17, // 0.5h steps
                                    colors = SliderDefaults.colors(
                                        thumbColor = colors.primary,
                                        activeTrackColor = colors.primary,
                                        inactiveTrackColor = colors.surfaceVariant
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("1h (Light)", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                                    Text("5h (Standard)", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                                    Text("10h (Hardcore)", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary)
                                }
                            }
                        }
                    }

                    // --- STEP 3: Summary Confirmation ---
                    3 -> {
                        Text(
                            text = "You're all set, ${state.nameInput.ifEmpty { "Aspirant" }}!",
                            style = QuovexTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Here is your personalized academic workspace summary:",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(QuovexTheme.spacing.lg))

                        val avatarRes = avatarList.find { it.first == state.selectedAvatarId }?.second ?: R.drawable.avatar_1

                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surface,
                            elevation = QuovexTheme.elevation.card
                        ) {
                            Column(
                                modifier = Modifier.padding(QuovexTheme.spacing.xl),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = "Avatar preview",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, colors.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))

                                Text(
                                    text = state.nameInput.ifEmpty { "Aspirant" },
                                    style = QuovexTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("TARGET EXAM", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                                        Text(state.selectedExam, style = QuovexTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("DAILY GOAL", style = QuovexTheme.typography.labelSmall, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                                        Text("${String.format("%.1f", state.dailyGoalHours)} hrs", style = QuovexTheme.typography.labelLarge, color = colors.primary, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(QuovexTheme.spacing.base))

                                QuovexChip(
                                    label = "+250 Welcome XP Granted 🏆",
                                    isSelected = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AutoAwesome,
                                            contentDescription = "XP",
                                            tint = colors.onPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(QuovexTheme.spacing.md))
                    Text(
                        text = state.errorMessage ?: "",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Persistent Bottom CTA Button
            Column(modifier = Modifier.fillMaxWidth().padding(top = QuovexTheme.spacing.base)) {
                if (state.currentStep < 3) {
                    QuovexButton(
                        text = "Next →",
                        onClick = { viewModel.nextStep() },
                        variant = QuovexButtonVariant.Primary
                    )
                } else {
                    QuovexButton(
                        text = if (state.isSaving) "Saving to Firebase..." else "Enter Quovex →",
                        onClick = { viewModel.finishOnboarding(onOnboardingComplete) },
                        enabled = !state.isSaving,
                        isLoading = state.isSaving,
                        variant = QuovexButtonVariant.Primary
                    )
                }
            }
        }
    }
}
