package com.quovex.ui.originals

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Quiz
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
import com.quovex.domain.model.originals.OriginalChapter
import com.quovex.theme.BrandEmerald
import com.quovex.theme.SurfaceDark
import com.quovex.theme.SurfaceGlass
import com.quovex.theme.TextPrimary
import com.quovex.theme.TextSecondary
import com.quovex.ui.components.QuovexButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginalBookDetailScreen(
    bookId: String,
    viewModel: OriginalsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChapterReader: (bookId: String, chapterNumber: Int) -> Unit
) {
    LaunchedEffect(bookId) {
        viewModel.loadBookDetails(bookId)
    }

    val state by viewModel.bookDetailState.collectAsState()
    val book = state.book

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book?.title ?: "Book Overview",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp,
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandEmerald, modifier = Modifier.size(32.dp))
            }
        } else if (book == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.errorMessage ?: "Book not found",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceGlass,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandEmerald.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = BrandEmerald.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "QUOVEX ORIGINAL",
                                        color = BrandEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${book.curriculum} • ${book.gradeClass}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }

                            Text(
                                text = book.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 22.sp,
                                lineHeight = 28.sp
                            )

                            if (!book.subtitle.isNullOrBlank()) {
                                Text(
                                    text = book.subtitle,
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }

                            Text(
                                text = book.description,
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📚 ${book.chapters.size} Chapters",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "⏱ ~${book.targetReadingTimeMinutes} mins",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "🎯 ${book.difficulty}",
                                    color = BrandEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (book.chapters.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                QuovexButton(
                                    text = "Start Reading Chapter 1",
                                    onClick = { onNavigateToChapterReader(book.id, 1) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Learning Objectives
                if (book.learningObjectives.isNotEmpty()) {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = BrandEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Core Learning Objectives",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                }
                                book.learningObjectives.forEach { objective ->
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
                                            text = objective,
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

                // Table of Contents Section Title
                item {
                    Text(
                        text = "Table of Contents",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Chapter Cards
                items(book.chapters, key = { it.chapterNumber }) { chapter ->
                    ChapterDirectoryCard(
                        chapter = chapter,
                        onClick = { onNavigateToChapterReader(book.id, chapter.chapterNumber) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChapterDirectoryCard(
    chapter: OriginalChapter,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = SurfaceGlass,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${chapter.chapterNumber}",
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald,
                        fontSize = 15.sp
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = chapter.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${chapter.sections.size} Sections • ${chapter.flashcards.size} Flashcards • ${chapter.quizQuestions.size} Quiz Qs",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Read Chapter",
                tint = BrandEmerald,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
