package com.quovex.ui.ncert

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertChapter
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertBookDetailScreen(
    viewModel: NcertBookDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToChapterDetail: (chapterId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.book?.title ?: "NCERT Textbook",
                                style = QuovexTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.width(spacing.xs))
                            NcertOfficialBadge()
                        }
                        Text(
                            text = if (uiState.book != null) "Class ${uiState.book!!.classLevel} • ${uiState.book!!.subject}" else "Official NCERT",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.surface
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                QuovexLoading(message = "Loading official NCERT textbook...")
            }
        } else if (!uiState.error.isNullOrBlank()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                QuovexErrorState(message = uiState.error!!)
            }
        } else if (uiState.book != null) {
            val book = uiState.book!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── BOOK METADATA HEADER CARD ────────────────────────────────
                item {
                    BookMetadataCard(book = book)
                }

                item {
                    Spacer(Modifier.height(spacing.xs))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chapters (${uiState.chapters.size})",
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Select chapter to study",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }

                // ── CHAPTER LIST ─────────────────────────────────────────────
                if (uiState.chapters.isEmpty()) {
                    item {
                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No chapters cataloged yet",
                                    style = QuovexTheme.typography.bodyMedium,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.chapters, key = { it.id }) { chapter ->
                        NcertChapterItemCard(
                            chapter = chapter,
                            onClick = { onNavigateToChapterDetail(chapter.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookMetadataCard(book: NcertBook) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OFFICIAL NCERT RESOURCE",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                NcertOfficialBadge()
            }

            Text(
                text = book.title,
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
            ) {
                InfoTag(label = "Class ${book.classLevel}")
                InfoTag(label = book.subject)
                InfoTag(label = book.language)
                InfoTag(label = "Code: ${book.bookCode}")
            }

            Text(
                text = "Publisher: ${book.publisher} • Curriculum: ${book.curriculum} (${book.edition})",
                style = QuovexTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun InfoTag(label: String) {
    val colors = QuovexTheme.colors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = QuovexTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
    }
}

@Composable
fun NcertChapterItemCard(
    chapter: NcertChapter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Chapter Number Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%02d", chapter.chapterNumber),
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }

            Spacer(Modifier.width(spacing.md))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                Text(
                    text = chapter.chapterTitle,
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Official CBSE Chapter",
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.width(spacing.xs))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Chapter",
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
