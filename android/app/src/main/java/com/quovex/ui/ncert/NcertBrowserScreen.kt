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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.quovex.domain.model.NcertBook
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertBrowserScreen(
    viewModel: NcertBrowserViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBookDetail: (bookId: String) -> Unit
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
                                text = "NCERT Library",
                                style = QuovexTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.width(spacing.xs))
                            NcertOfficialBadge()
                        }
                        Text(
                            text = "Official CBSE & NCERT Rationalised Curriculum",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── CLASS SELECTOR TABS ──────────────────────────────────────────
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colors.surface
                ) {
                    TabRow(
                        selectedTabIndex = uiState.availableClasses.indexOf(uiState.selectedClass).coerceAtLeast(0),
                        containerColor = colors.surface,
                        contentColor = colors.primary,
                        indicator = { tabPositions ->
                            val index = uiState.availableClasses.indexOf(uiState.selectedClass).coerceAtLeast(0)
                            if (index < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                    color = colors.primary
                                )
                            }
                        }
                    ) {
                        uiState.availableClasses.forEach { classLevel ->
                            val isSelected = uiState.selectedClass == classLevel
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.selectClass(classLevel) },
                                text = {
                                    Text(
                                        text = "Class $classLevel",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.primary else colors.textSecondary
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // ── SEARCH & SUBJECT FILTER BAR ──────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    QuovexTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = "Search books in Class ${uiState.selectedClass}...",
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = colors.textSecondary)
                        }
                    )

                    // Dynamic Subject Filter Chips
                    if (uiState.availableSubjects.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            items(uiState.availableSubjects) { subject ->
                                QuovexChip(
                                    label = subject,
                                    isSelected = uiState.selectedSubject == subject,
                                    onClick = { viewModel.selectSubject(subject) }
                                )
                            }
                        }
                    }
                }
            }

            // ── BOOK LISTING ─────────────────────────────────────────────────
            if (uiState.books.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Icon(
                                Icons.Filled.AutoStories,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No textbooks found",
                                style = QuovexTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Try selecting another subject or class.",
                                style = QuovexTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            } else {
                items(uiState.books, key = { it.id }) { book ->
                    NcertBookCard(
                        book = book,
                        onClick = { onNavigateToBookDetail(book.id) },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NcertBookCard(
    book: NcertBook,
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
            // Book Icon & Badge Column
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(spacing.md))

            // Book Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacing.xxs)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CLASS ${book.classLevel}",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        text = " • ${book.subject}",
                        style = QuovexTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }

                Text(
                    text = book.title,
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    Text(
                        text = "${book.totalChapters} Chapters",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "•",
                        color = colors.textSecondary
                    )
                    Text(
                        text = book.edition,
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.width(spacing.xs))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View Chapters",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NcertOfficialBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF00C896).copy(alpha = 0.15f),
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                tint = Color(0xFF00C896),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = "NCERT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00C896)
            )
        }
    }
}
