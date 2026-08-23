package com.quovex.ui.knowledge

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.LearningMaterial
import com.quovex.theme.BrandEmerald
import com.quovex.theme.BrandEmeraldDim
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDetailScreen(
    viewModel: MaterialDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: (materialId: Long) -> Unit,
    onNavigateToFlashcards: (deckId: Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Summary", "Key Concepts", "Flashcards", "Quiz")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.material?.title ?: "Material Detail",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
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
        } else {
            val material = uiState.material
            if (material == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Material not found", color = TextSecondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = SurfaceDark,
                        contentColor = BrandEmerald,
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = BrandEmerald
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) BrandEmerald else TextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        when (selectedTab) {
                            0 -> SummaryTab(material)
                            1 -> KeyConceptsTab(material)
                            2 -> FlashcardsTab(material, onNavigateToFlashcards)
                            3 -> QuizTab(
                                material = material,
                                questions = uiState.quizQuestions,
                                isGenerating = uiState.isGeneratingQuiz,
                                onGenerateQuiz = { viewModel.generateQuiz() },
                                onTakeQuiz = { onNavigateToQuiz(material.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryTab(material: LearningMaterial) {
    if (material.summary.isNotBlank()) {
        Surface(
            color = SurfaceGlass,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Overview",
                    fontWeight = FontWeight.Bold,
                    color = BrandEmerald,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                com.quovex.ui.components.QuovexMathText(
                    text = material.summary,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }
    } else {
        EmptyTabContent(
            title = "Summary Needs Processing",
            description = "This study material has not been processed with Quovex AI yet."
        )
    }
}

@Composable
private fun KeyConceptsTab(material: LearningMaterial) {
    if (material.keyPoints.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            material.keyPoints.forEachIndexed { index, point ->
                Surface(
                    color = SurfaceGlass,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(BrandEmeraldDim.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = BrandEmerald,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        com.quovex.ui.components.QuovexMathText(
                            text = point,
                            color = TextPrimary,
                            lineHeight = 20.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    } else {
        EmptyTabContent(
            title = "No Key Concepts Extracted",
            description = "Extract key formulas and principles by transforming your notes."
        )
    }
}

@Composable
private fun FlashcardsTab(
    material: LearningMaterial,
    onNavigateToFlashcards: (deckId: Long) -> Unit
) {
    Surface(
        color = SurfaceGlass,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Style,
                contentDescription = null,
                tint = BrandEmerald,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${material.flashcardCount} Active Recall Flashcards",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Spaced repetition using SuperMemo-2 algorithm",
                color = TextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            QuovexButton(
                text = "Practice Flashcards",
                onClick = {
                    val deckId = material.flashcardDeckId?.toLong() ?: material.id
                    onNavigateToFlashcards(deckId)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = material.flashcardCount > 0
            )
        }
    }
}

@Composable
private fun QuizTab(
    material: LearningMaterial,
    questions: List<com.quovex.domain.model.QuizQuestion>,
    isGenerating: Boolean,
    onGenerateQuiz: () -> Unit,
    onTakeQuiz: () -> Unit
) {
    Surface(
        color = SurfaceGlass,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = null,
                tint = Color(0xFF64B5F6),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (questions.isNotEmpty()) "${questions.size} Questions Available" else "High-Yield Practice Quiz",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Test your mastery on ${material.topic.ifBlank { material.subject }} with detailed explanations.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (questions.isNotEmpty()) {
                QuovexButton(
                    text = "Take Material Quiz",
                    onClick = onTakeQuiz,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                QuovexButton(
                    text = if (isGenerating) "Generating with Quovex AI..." else "Generate Quiz with Quovex AI",
                    onClick = onGenerateQuiz,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating
                )
            }

            if (material.summary.isBlank() && !isGenerating && questions.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Tip: Make sure you're connected to the internet to generate quizzes.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyTabContent(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
