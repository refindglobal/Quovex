package com.quovex.ui.originals

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.originals.OriginalCommonMistake
import com.quovex.domain.model.originals.OriginalRealWorldExample
import com.quovex.domain.model.originals.OriginalSection
import com.quovex.domain.model.originals.OriginalWorkedExample
import com.quovex.theme.BrandEmerald
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexMathText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalChapterReaderScreen(
    bookId: String,
    chapterNumber: Int,
    viewModel: OriginalsViewModel,
    onNavigateBack: () -> Unit,
    onOpenAiChat: (subject: String, topic: String, prompt: String) -> Unit,
    onStartQuiz: (materialId: Long) -> Unit,
    onStudyFlashcards: (deckId: Long) -> Unit
) {
    LaunchedEffect(bookId, chapterNumber) {
        viewModel.loadChapterForReading(bookId, chapterNumber)
    }

    val state by viewModel.readerState.collectAsState()
    val chapter = state.currentChapter
    val book = state.book
    val section = chapter?.sections?.getOrNull(state.selectedSectionIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapter?.title ?: "Chapter $chapterNumber",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${book?.title ?: "Quovex Original"} • Ch $chapterNumber",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
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
                actions = {
                    IconButton(
                        onClick = {
                            val subject = book?.subject ?: "Physics"
                            val topic = "${book?.title} - ${chapter?.title}"
                            onOpenAiChat(subject, topic, "Explain this chapter to me in simple terms with a real-world example.")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask Quovex AI",
                            tint = BrandEmerald
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        bottomBar = {
            if (chapter != null) {
                Surface(
                    color = SurfaceDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (chapter.flashcards.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.prepareChapterFlashcards(onStudyFlashcards) },
                                color = BrandEmerald.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Cards (${chapter.flashcards.size})",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandEmerald,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (chapter.quizQuestions.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.prepareChapterQuiz(onStartQuiz) },
                                color = BrandEmerald,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Quiz,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Quiz (${chapter.quizQuestions.size})",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = SurfaceDark
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandEmerald, modifier = Modifier.size(32.dp))
            }
        } else if (chapter == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Chapter content unavailable",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Section Tabs
                if (chapter.sections.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(chapter.sections) { index, sec ->
                            val isSelected = state.selectedSectionIndex == index
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.selectSection(index) },
                                color = if (isSelected) BrandEmerald else SurfaceGlass,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BrandEmerald else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "§ ${sec.sectionNumber} ${sec.title}",
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Section Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (section != null) {
                        // Section Heading
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    color = BrandEmerald.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SECTION ${section.sectionNumber}",
                                        color = BrandEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = section.title,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        // Conceptual Explanation with QuovexMathText
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SurfaceGlass,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    QuovexMathText(
                                        text = section.conceptualExplanation,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        // Visual Analogy Card
                        if (!section.visualAnalogy.isNullOrBlank()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BrandEmerald.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null,
                                            tint = BrandEmerald,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Visual Analogy",
                                                fontWeight = FontWeight.Bold,
                                                color = BrandEmerald,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = section.visualAnalogy,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                lineHeight = 19.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Worked Numericals
                        if (section.workedExamples.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Step-by-Step Worked Problems",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            itemsIndexed(section.workedExamples) { idx, example ->
                                WorkedExampleCard(example = example, index = idx + 1)
                            }
                        }

                        // Real-World Case Studies
                        if (section.realWorldExamples.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Real-World Engineering & Scientific Case Studies",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            itemsIndexed(section.realWorldExamples) { idx, rw ->
                                RealWorldExampleCard(example = rw)
                            }
                        }

                        // Common Student Misconceptions / Traps
                        if (section.commonMistakes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Common Misconceptions & Exam Traps",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            itemsIndexed(section.commonMistakes) { idx, mistake ->
                                CommonMistakeCard(mistake = mistake)
                            }
                        }

                        // Quick Revision Summary Points
                        if (section.summaryPoints.isNotEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = SurfaceGlass,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Quick Revision Key Takeaways",
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            fontSize = 13.sp
                                        )
                                        section.summaryPoints.forEach { pt ->
                                            Row(
                                                verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = BrandEmerald,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .padding(top = 2.dp)
                                                )
                                                Text(
                                                    text = pt,
                                                    color = TextSecondary,
                                                    fontSize = 12.sp,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WorkedExampleCard(example: OriginalWorkedExample, index: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceGlass,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Worked Problem $index",
                    fontWeight = FontWeight.Bold,
                    color = BrandEmerald,
                    fontSize = 13.sp
                )
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = example.difficulty,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            QuovexMathText(
                text = example.problemStatement,
                color = TextPrimary
            )

            example.stepByStepSolution.forEach { step ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Step ${step.stepNumber}:",
                            fontWeight = FontWeight.Bold,
                            color = BrandEmerald,
                            fontSize = 11.sp
                        )
                        QuovexMathText(
                            text = step.explanation,
                            color = TextPrimary
                        )
                        if (!step.mathFormula.isNullOrBlank()) {
                            QuovexMathText(
                                text = step.mathFormula,
                                color = BrandEmerald
                            )
                        }
                    }
                }
            }

            if (example.keyTakeaway.isNotBlank()) {
                Text(
                    text = "💡 Key Takeaway: ${example.keyTakeaway}",
                    color = BrandEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RealWorldExampleCard(example: OriginalRealWorldExample) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceGlass,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${example.domain} • ${example.title}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }

            Text(
                text = example.narrative,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Text(
                text = "Applied Principle: ${example.physicsOrConceptPrinciple}",
                color = BrandEmerald,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun CommonMistakeCard(mistake: OriginalCommonMistake) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A1515).copy(alpha = 0.5f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Trap: ${mistake.misconception}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp
                )
            }

            Text(
                text = "Why students make it: ${mistake.whyStudentsMakeIt}",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Text(
                text = "✓ Correct Understanding: ${mistake.correctUnderstanding}",
                color = BrandEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            if (mistake.quickCheck.isNotBlank()) {
                Text(
                    text = "Quick Check: ${mistake.quickCheck}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}
