package com.quovex.ui.notes

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexDialog
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NoteDetailScreen(
    viewModel: NoteDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onBackClick()
        }
    }

    if (showDeleteDialog) {
        QuovexDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = "Delete Note?"
        ) {
            Column {
                Text(
                    text = "Are you sure you want to delete this study note? This action cannot be undone.",
                    style = QuovexTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    QuovexButton(
                        text = "Cancel",
                        onClick = { showDeleteDialog = false },
                        variant = QuovexButtonVariant.Ghost,
                        modifier = Modifier.padding(end = spacing.sm),
                        height = 44.dp
                    )
                    QuovexButton(
                        text = "Delete",
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteNote()
                        },
                        variant = QuovexButtonVariant.Danger,
                        height = 44.dp
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = if (state.isEditing) "Edit Note" else "Note Detail",
                onBackClick = onBackClick,
                actions = {
                    if (state.note != null) {
                        if (state.isEditing) {
                            IconButton(onClick = { viewModel.cancelEditing() }) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = colors.textSecondary)
                            }
                            IconButton(onClick = { viewModel.saveEdits() }) {
                                Icon(Icons.Filled.Check, contentDescription = "Save", tint = colors.primary)
                            }
                        } else {
                            IconButton(onClick = { viewModel.startEditing() }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Note", tint = colors.textSecondary)
                            }
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Note", tint = colors.error)
                            }
                        }
                    }
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        when {
            state.isLoading -> {
                QuovexLoading(modifier = Modifier.padding(padding))
            }

            state.error != null && state.note == null -> {
                QuovexErrorState(
                    message = state.error ?: "Failed to load note.",
                    onRetry = { viewModel.loadNote() },
                    modifier = Modifier.padding(padding)
                )
            }

            state.note != null -> {
                val note = state.note!!
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(scrollState)
                        .padding(horizontal = spacing.lg, vertical = spacing.md)
                ) {
                    if (state.isEditing) {
                        // ── EDIT MODE ────────────────────────────────────────────────
                        QuovexTextField(
                            value = state.editTitle,
                            onValueChange = { viewModel.onEditTitleChange(it) },
                            label = "Note Title"
                        )
                        Spacer(Modifier.height(spacing.md))
                        QuovexTextField(
                            value = state.editSubject,
                            onValueChange = { viewModel.onEditSubjectChange(it) },
                            label = "Subject"
                        )
                        Spacer(Modifier.height(spacing.md))
                        QuovexTextField(
                            value = state.editContent,
                            onValueChange = { viewModel.onEditContentChange(it) },
                            label = "Content (Markdown Supported)",
                            singleLine = false,
                            minLines = 8
                        )
                        Spacer(Modifier.height(spacing.xl))
                        QuovexButton(
                            text = if (state.isSaving) "Saving Changes..." else "Save Changes",
                            onClick = { viewModel.saveEdits() },
                            enabled = !state.isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // ── VIEW MODE ────────────────────────────────────────────────
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            QuovexChip(label = note.subject.uppercase(), isSelected = true)
                            QuovexChip(label = note.inputType.name, isSelected = false)
                        }

                        Spacer(Modifier.height(spacing.sm))

                        Text(
                            text = note.title,
                            style = QuovexTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )

                        Spacer(Modifier.height(spacing.xs))

                        val formattedDate = remember(note.createdAt) {
                            SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(note.createdAt))
                        }
                        Text(
                            text = "Created $formattedDate",
                            style = QuovexTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )

                        Spacer(Modifier.height(spacing.lg))

                        // Key Points Section if available
                        if (note.keyPoints.isNotEmpty()) {
                            QuovexCard(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = colors.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(spacing.base)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = colors.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(spacing.xs))
                                        Text(
                                            text = "Key Takeaways (${note.keyPoints.size})",
                                            style = QuovexTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.primary
                                        )
                                    }
                                    Spacer(Modifier.height(spacing.sm))
                                    note.keyPoints.forEach { point ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                "• ",
                                                color = colors.primary,
                                                fontWeight = FontWeight.Bold,
                                                style = QuovexTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = point,
                                                style = QuovexTheme.typography.bodyMedium,
                                                color = colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(spacing.lg))
                        }

                        // Content Body
                        Text(
                            text = note.content,
                            style = QuovexTheme.typography.bodyLarge,
                            color = colors.textSecondary,
                            lineHeight = QuovexTheme.typography.bodyLarge.lineHeight
                        )

                        Spacer(Modifier.height(spacing.xxl))

                        // Generate Flashcards Action Card
                        QuovexCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = colors.surface
                        ) {
                            Column(modifier = Modifier.padding(spacing.base)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Flashcard Generation",
                                            style = QuovexTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (note.flashcardCount > 0)
                                                "${note.flashcardCount} cards created from this note"
                                            else
                                                "Turn this note into spaced repetition flashcards",
                                            style = QuovexTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                    QuovexButton(
                                        text = if (state.isGeneratingFlashcards) "Creating..." else "Generate Cards",
                                        onClick = { viewModel.generateFlashcards() },
                                        enabled = !state.isGeneratingFlashcards,
                                        variant = QuovexButtonVariant.Secondary,
                                        height = 40.dp
                                    )
                                }

                                if (state.flashcardsGeneratedCount != null) {
                                    Spacer(Modifier.height(spacing.sm))
                                    Text(
                                        text = "✅ Created ${state.flashcardsGeneratedCount} new flashcards in Library!",
                                        style = QuovexTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.primary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(spacing.xxl))
                    }
                }
            }
        }
    }
}
