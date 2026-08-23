package com.quovex.ui.ncert

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NcertVerificationStatus
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.AiActionContext
import com.quovex.ui.components.QuovexAiActionSheet
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertChapterDetailScreen(
    viewModel: NcertChapterDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMaterialDetail: (materialId: Long) -> Unit,
    onReadInQuovex: (chapterId: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    var showAiSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Chapter ${uiState.chapter?.chapterNumber ?: ""}",
                                style = QuovexTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.width(spacing.xs))
                            NcertOfficialBadge()
                        }
                        Text(
                            text = uiState.chapter?.bookTitle ?: "Official NCERT",
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
                QuovexLoading(message = "Loading chapter details...")
            }
        } else if (!uiState.error.isNullOrBlank() && uiState.chapter == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                QuovexErrorState(message = uiState.error!!)
            }
        } else if (uiState.chapter != null) {
            val chapter = uiState.chapter!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── CHAPTER TITLE & CLASSIFICATION CARD ──────────────────────
                item {
                    ChapterHeaderCard(chapter = chapter)
                }

                // ── OFFICIAL SOURCE VERIFICATION CARD ────────────────────────
                item {
                    OfficialSourceCard(chapter = chapter)
                }

                // ── ACTIONS SECTION ──────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        // Action 1: Read in Quovex — in-app PDF reader (NO external browser)
                        QuovexButton(
                            text = "Read in Quovex",
                            leadingIcon = {
                                Icon(Icons.Filled.School, contentDescription = null)
                            },
                            variant = QuovexButtonVariant.Secondary,
                            onClick = {
                                uiState.chapter?.id?.let { id -> onReadInQuovex(id) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Action 2: Use Quovex AI — contextual tools (NOT auto-processing)
                        QuovexButton(
                            text = "Use Quovex AI",
                            leadingIcon = {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                            },
                            variant = QuovexButtonVariant.Primary,
                            onClick = { showAiSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // ── AI PROCESSING STATE ──────────────────────────────────────
                if (uiState.isProcessingAiStudy) {
                    item {
                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                QuovexLoading(message = uiState.aiStudyProgressMessage)
                                Spacer(Modifier.height(spacing.xs))
                                Text(
                                    text = "Creating Summary, Key Concepts, Formulas, Flashcards & Practice Quiz",
                                    style = QuovexTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }

                if (!uiState.error.isNullOrBlank() && uiState.chapter != null) {
                    item {
                        QuovexErrorState(message = uiState.error!!)
                    }
                }

                // ── CURRICULUM & LEARNING ECOSYSTEM CARD ─────────────────────
                item {
                    CurriculumGuideCard(chapter = chapter)
                }
            }
        }
    }

    // Contextual AI action sheet for CHAPTER scope
    if (showAiSheet) {
        val chapterLabel = uiState.chapter?.let {
            "Chapter ${it.chapterNumber}: ${it.chapterTitle}"
        } ?: ""
        QuovexAiActionSheet(
            context = AiActionContext.CHAPTER,
            contextLabel = chapterLabel,
            onActionSelected = { actionId ->
                showAiSheet = false
                when (actionId) {
                    "study_guide", "flashcards", "quiz" -> {
                        viewModel.studyWithQuovexAi { materialId ->
                            onNavigateToMaterialDetail(materialId)
                        }
                    }
                }
            },
            onDismiss = { showAiSheet = false }
        )
    }
}

@Composable
private fun ChapterHeaderCard(chapter: NcertChapter) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(spacing.xxs))
                    Text(
                        text = "CLASS ${chapter.classLevel} • ${chapter.subject.uppercase()}",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                NcertOfficialBadge()
            }

            Text(
                text = "Chapter ${chapter.chapterNumber}: ${chapter.chapterTitle}",
                style = QuovexTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Text(
                text = "Textbook: ${chapter.bookTitle}",
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun OfficialSourceCard(chapter: NcertChapter) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Verified,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(spacing.xs))
                Text(
                    text = "Official Resource Metadata",
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(Modifier.height(spacing.xxs))

            MetadataRow(label = "Publisher", value = chapter.publisher)
            MetadataRow(label = "Curriculum", value = chapter.curriculum)
            MetadataRow(label = "Language", value = chapter.language)
            MetadataRow(label = "Official Portal", value = "ncert.nic.in")
            MetadataRow(
                label = "Catalog Status",
                value = if (chapter.verificationStatus == NcertVerificationStatus.VERIFIED) "Active & Verified" else "Cataloged"
            )
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    val colors = QuovexTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = QuovexTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = QuovexTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun CurriculumGuideCard(chapter: NcertChapter) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.Psychology,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(spacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
                Text(
                    text = "Quovex AI Learning Ecosystem",
                    style = QuovexTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Text(
                    text = "Tap 'Study with Quovex AI' to generate structured active-recall study aids, smart flashcards with SM-2 spaced repetition, and an interactive quiz for this chapter.",
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}
