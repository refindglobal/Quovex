package com.quovex.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.QuizMistake
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultScreen(
    score: Int,
    total: Int,
    mistakes: List<QuizMistake>,
    remedialCreated: Boolean,
    onCreateRemedial: () -> Unit,
    onFinish: () -> Unit
) {
    val accuracy = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quiz Results",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = SurfaceDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score Banner
            Surface(
                color = SurfaceGlass,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(
                                if (accuracy >= 70) BrandEmeraldDim.copy(alpha = 0.3f) else Color(0xFFFFB74D).copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$accuracy%",
                            color = if (accuracy >= 70) BrandEmerald else Color(0xFFFFB74D),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "You scored $score out of $total",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (accuracy >= 80) "Exceptional mastery! Keep up the momentum." else "Great effort! Review weak areas to reinforce concepts.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Remedial Flashcards Call-to-Action
            if (mistakes.isNotEmpty()) {
                Surface(
                    color = Color(0xFF64B5F6).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quovex Remedial Flashcards",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64B5F6),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Generate targeted spaced-repetition flashcards for the ${mistakes.size} concept(s) you missed in this quiz.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        QuovexButton(
                            text = if (remedialCreated) "Flashcards Created!" else "Create Remedial Flashcards",
                            onClick = onCreateRemedial,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !remedialCreated
                        )
                    }
                }

                // Mistakes Breakdown
                Text(
                    text = "Review Mistakes (${mistakes.size})",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                mistakes.forEachIndexed { index, mistake ->
                    Surface(
                        color = SurfaceGlass,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${index + 1}. ${mistake.questionText}",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Your answer: ${mistake.studentAnswer}",
                                color = Color(0xFFEF5350),
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Correct answer: ${mistake.correctAnswer}",
                                color = BrandEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (mistake.explanation.isNotBlank()) {
                                Text(
                                    text = mistake.explanation,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            QuovexButton(
                text = "Back to Knowledge Hub",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
