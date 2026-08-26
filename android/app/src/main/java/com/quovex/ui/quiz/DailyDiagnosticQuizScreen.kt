package com.quovex.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.DiagnosticQuestion
import com.quovex.domain.model.QuizMistake

// Brand Colors
private val BrandCharcoal = Color(0xFF0A0F0D)
private val BrandEmerald = Color(0xFF00C896)
private val EmeraldDark = Color(0xFF008F6B)
private val SurfaceGlass = Color(0xFF141C18)
private val CardSurface = Color(0xFF18221E)
private val TextPrimary = Color(0xFFE8F5EE)
private val TextSecondary = Color(0xFF8FA89B)
private val AmberAccent = Color(0xFFFFB74D)
private val ErrorRed = Color(0xFFFF5252)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDiagnosticQuizScreen(
    viewModel: DailyDiagnosticQuizViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFlashcards: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = BrandCharcoal,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Diagnostic Quiz",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "5 Questions • Tested from Today's Topics",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandCharcoal)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = BrandEmerald, strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Synthesizing Today's Diagnostic Quiz...",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }

                state.errorMessage != null && state.questions.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage ?: "Failed to generate diagnostic quiz",
                            color = ErrorRed,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadDailyQuiz() },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                        ) {
                            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry", color = BrandCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                state.isFinished -> {
                    QuizFinishedSummaryView(
                        state = state,
                        onNavigateBack = onNavigateBack,
                        onNavigateToFlashcards = onNavigateToFlashcards
                    )
                }

                else -> {
                    val currentQ = state.questions.getOrNull(state.currentIndex)
                    if (currentQ != null) {
                        ActiveDiagnosticQuestionView(
                            question = currentQ,
                            currentIndex = state.currentIndex,
                            totalQuestions = state.totalQuestions,
                            selectedOption = state.selectedOptionIndex,
                            isSubmitted = state.isSubmitted,
                            onOptionSelected = viewModel::selectOption,
                            onSubmit = viewModel::submitAnswer,
                            onNext = viewModel::nextQuestion
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveDiagnosticQuestionView(
    question: DiagnosticQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    isSubmitted: Boolean,
    onOptionSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / totalQuestions.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = BrandEmerald,
            trackColor = SurfaceGlass
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUESTION ${currentIndex + 1} OF $totalQuestions",
                color = BrandEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Subject Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceGlass
            ) {
                Text(
                    text = "${question.subject} • ${question.concept}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CardSurface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = question.questionText,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options List
        question.options.forEachIndexed { index, optionText ->
            val isSelected = selectedOption == index
            val isCorrect = question.correctOptionIndex == index

            val backgroundColor by animateColorAsState(
                targetValue = when {
                    !isSubmitted && isSelected -> BrandEmerald.copy(alpha = 0.15f)
                    isSubmitted && isCorrect -> BrandEmerald.copy(alpha = 0.25f)
                    isSubmitted && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.2f)
                    else -> SurfaceGlass
                },
                animationSpec = tween(300),
                label = "optionBg"
            )

            val borderColor by animateColorAsState(
                targetValue = when {
                    !isSubmitted && isSelected -> BrandEmerald
                    isSubmitted && isCorrect -> BrandEmerald
                    isSubmitted && isSelected && !isCorrect -> ErrorRed
                    else -> Color.Transparent
                },
                animationSpec = tween(300),
                label = "optionBorder"
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isSubmitted) { onOptionSelected(index) },
                color = backgroundColor
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (isSelected || (isSubmitted && isCorrect)) BrandEmerald.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('A' + index).toString(),
                            color = if (isSelected || (isSubmitted && isCorrect)) BrandEmerald else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = optionText,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSubmitted) {
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Correct",
                                tint = BrandEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Incorrect",
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Explanation Banner after submission
        AnimatedVisibility(visible = isSubmitted) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = CardSurface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = if (selectedOption == question.correctOptionIndex) BrandEmerald else AmberAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedOption == question.correctOptionIndex) "Great Job! Correct Answer" else "Concept Explanation",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = question.explanation,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Button
        if (!isSubmitted) {
            Button(
                onClick = onSubmit,
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandEmerald,
                    disabledContainerColor = SurfaceGlass
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Submit Answer",
                    color = if (selectedOption != null) BrandCharcoal else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (currentIndex + 1 < totalQuestions) "Next Question →" else "Finish & View Analysis",
                    color = BrandCharcoal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuizFinishedSummaryView(
    state: DailyDiagnosticQuizUiState,
    onNavigateBack: () -> Unit,
    onNavigateToFlashcards: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Score Badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(BrandEmerald.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, BrandEmerald, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${state.score}%",
                    color = BrandEmerald,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Score",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (state.score >= 80) "Exceptional Mastery!" else "Daily Diagnostic Complete",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "+50 XP earned • Progress recorded in Mastery Engine",
            color = BrandEmerald,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Remedial Spaced Repetition Banner (Q-009 & F-067)
        if (state.mistakes.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AmberAccent.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "🎯 Remedial Loop Activated",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${state.mistakes.size} cards queued for Spaced Repetition",
                                color = AmberAccent,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your misconceptions have been decomposed into targeted remedial cards and scheduled for review tomorrow with SM-2 high priority.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    if (state.isSynthesizingRemedial) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AmberAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Synthesizing AI concept cards...",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mistakes Breakdown
            Text(
                text = "Mistakes & Conceptual Traps",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            state.mistakes.forEachIndexed { idx, m ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceGlass
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${idx + 1}. ${m.questionText}",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "❌ Your Answer: ${m.studentAnswer}",
                            color = ErrorRed,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "✅ Correct Answer: ${m.correctAnswer}",
                            color = BrandEmerald,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "💡 ${m.explanation}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Return to Dashboard",
                color = BrandCharcoal,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onNavigateToFlashcards,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text(
                text = "Review Spaced Repetition Decks",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
