package com.quovex.ui.quiz

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.QuizQuestion
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isFinished) {
        QuizResultScreen(
            score = uiState.finalScore,
            total = uiState.totalQuestions,
            mistakes = uiState.mistakes,
            remedialCreated = uiState.remedialDeckCreated,
            onCreateRemedial = { viewModel.createRemedialFlashcards() },
            onFinish = onNavigateBack
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Practice Quiz",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandEmerald)
            }
        } else if (uiState.questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No questions available for this material.", color = TextSecondary)
            }
        } else {
            val currentQuestion = uiState.questions[uiState.currentIndex]
            val progress = (uiState.currentIndex + 1).toFloat() / uiState.totalQuestions

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${uiState.currentIndex + 1} of ${uiState.totalQuestions}",
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald,
                        fontSize = 14.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandEmerald,
                    trackColor = SurfaceGlass
                )

                // Question Card
                Surface(
                    color = SurfaceGlass,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(18.dp)) {
                        com.quovex.ui.components.QuovexMathText(
                            text = currentQuestion.question,
                            color = TextPrimary,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Options
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = uiState.selectedOptionIndex == index
                    val isCorrect = index == currentQuestion.correctIndex

                    val borderColor = when {
                        !uiState.isSubmitted && isSelected -> BrandEmerald
                        uiState.isSubmitted && isCorrect -> BrandEmerald
                        uiState.isSubmitted && isSelected && !isCorrect -> Color(0xFFEF5350)
                        else -> SurfaceGlass
                    }

                    val containerColor = when {
                        !uiState.isSubmitted && isSelected -> BrandEmeraldDim.copy(alpha = 0.2f)
                        uiState.isSubmitted && isCorrect -> BrandEmeraldDim.copy(alpha = 0.25f)
                        uiState.isSubmitted && isSelected && !isCorrect -> Color(0xFFEF5350).copy(alpha = 0.15f)
                        else -> SurfaceGlass
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable(enabled = !uiState.isSubmitted) {
                                viewModel.selectOption(index)
                            },
                        color = containerColor,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isSelected || (uiState.isSubmitted && isCorrect)) BrandEmerald else SurfaceGlass,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val letter = ('A' + index).toString()
                                Text(
                                    text = letter,
                                    color = if (isSelected || (uiState.isSubmitted && isCorrect)) Color.Black else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                com.quovex.ui.components.QuovexMathText(
                                    text = option,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Explanation Banner after Submit
                if (uiState.isSubmitted) {
                    Surface(
                        color = BrandEmeraldDim.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Explanation",
                                fontWeight = FontWeight.Bold,
                                color = BrandEmerald,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            com.quovex.ui.components.QuovexMathText(
                                text = currentQuestion.explanation,
                                color = TextPrimary,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Button
                if (!uiState.isSubmitted) {
                    QuovexButton(
                        text = "Submit Answer",
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedOptionIndex != null
                    )
                } else {
                    QuovexButton(
                        text = if (uiState.currentIndex + 1 < uiState.totalQuestions) "Next Question" else "View Results",
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
