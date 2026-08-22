package com.quovex.ui.decks

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quovex.R
import com.quovex.domain.model.NoteItem
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexDialog
import com.quovex.ui.components.QuovexEmptyState
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLinearProgressIndicator
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar
import com.quovex.ui.notes.CreateNoteBottomSheet
import com.quovex.ui.notes.NotesUiState
import com.quovex.ui.notes.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val libraryTabs = listOf("Flashcards", "Notes", "Plans")
private val subjectCategories = listOf("All", "Physics", "Chemistry", "Mathematics", "Biology")

@Composable
fun LibraryScreen(
    deckViewModel: DeckListViewModel,
    notesViewModel: NotesViewModel,
    onDeckClick: (deckId: Int) -> Unit,
    onNoteClick: (noteId: Long) -> Unit = {},
    onScanDocumentClick: () -> Unit = {},
    onImageDoubtClick: () -> Unit = {}
) {
    val deckState by deckViewModel.uiState.collectAsState()
    val notesState by notesViewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showCreateDeckDialog by remember { mutableStateOf(false) }
    var newDeckTitle by remember { mutableStateOf("") }
    var newDeckSubject by remember { mutableStateOf("") }

    if (notesState.showCreateSheet) {
        CreateNoteBottomSheet(
            availableSubjects = notesState.availableSubjects,
            isProcessing = notesState.isCreatingNote,
            errorMessage = notesState.creationError,
            onDismiss = { notesViewModel.closeCreateSheet() },
            onQuickNoteCreate = { title, subject, content, summarizeWithAi ->
                notesViewModel.createQuickNote(title, subject, content, summarizeWithAi) { noteId ->
                    onNoteClick(noteId)
                }
            },
            onUrlImport = { url, subject ->
                notesViewModel.importUrlNote(url, subject) { noteId ->
                    onNoteClick(noteId)
                }
            },
            onScanDocumentClick = {
                notesViewModel.closeCreateSheet()
                onScanDocumentClick()
            }
        )
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(title = "Your Knowledge Base")
        },
        containerColor = colors.background,
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showCreateDeckDialog = true },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = QuovexTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = QuovexTheme.spacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Create Deck")
                        Spacer(Modifier.size(QuovexTheme.spacing.sm))
                        Text(
                            "Create Deck",
                            style = QuovexTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { notesViewModel.openCreateSheet() },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    shape = QuovexTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = QuovexTheme.spacing.base),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Note")
                        Spacer(Modifier.size(QuovexTheme.spacing.sm))
                        Text(
                            "Add Note",
                            style = QuovexTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->

        // Create Deck dialog
        if (showCreateDeckDialog) {
            CreateDeckDialog(
                newDeckTitle = newDeckTitle,
                onTitleChange = { newDeckTitle = it },
                newDeckSubject = newDeckSubject,
                onSubjectChange = { newDeckSubject = it },
                onDismiss = {
                    showCreateDeckDialog = false
                    newDeckTitle = ""
                    newDeckSubject = ""
                },
                onConfirm = {
                    if (newDeckTitle.isNotBlank() && newDeckSubject.isNotBlank()) {
                        deckViewModel.createDeck(newDeckTitle.trim(), newDeckSubject.trim())
                        showCreateDeckDialog = false
                        newDeckTitle = ""
                        newDeckSubject = ""
                    }
                }
            )
        }

        // Create Note bottom sheet
        if (notesState.showCreateSheet) {
            com.quovex.ui.notes.CreateNoteBottomSheet(
                availableSubjects = notesState.availableSubjects,
                isProcessing = notesState.isCreatingNote,
                errorMessage = notesState.creationError ?: notesState.error,
                onDismiss = { notesViewModel.closeCreateSheet() },
                onQuickNoteCreate = { title, subject, content, summarizeWithAi ->
                    notesViewModel.createQuickNote(title, subject, content, summarizeWithAi) { createdId ->
                        onNoteClick(createdId)
                    }
                },
                onUrlImport = { url, subject ->
                    notesViewModel.importUrlNote(url, subject) { createdId ->
                        onNoteClick(createdId)
                    }
                },
                onScanDocumentClick = {
                    notesViewModel.closeCreateSheet()
                    onScanDocumentClick()
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = colors.surface,
                contentColor = colors.primary
            ) {
                libraryTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                style = QuovexTheme.typography.labelLarge,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) colors.primary else colors.textSecondary
                            )
                        }
                    )
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> FlashcardsTabContent(
                    state = deckState,
                    onDeckClick = onDeckClick,
                    onCategorySelected = { deckViewModel.selectCategory(it) },
                    onCreateDeckClick = { showCreateDeckDialog = true }
                )
                1 -> NotesTabContent(
                    state = notesState,
                    onNoteClick = onNoteClick,
                    onSubjectSelected = { notesViewModel.selectSubject(it) },
                    onSearchQueryChange = { notesViewModel.onSearchQueryChange(it) },
                    onAddNoteClick = { notesViewModel.openCreateSheet() },
                    onScanDocumentClick = onScanDocumentClick,
                    onImageDoubtClick = onImageDoubtClick
                )
                2 -> ComingSoonTab(
                    title = "Study Plans",
                    description = "Create and manage your AI study plans here. This feature is coming in Phase 7."
                )
            }
        }
    }
}

@Composable
private fun NotesTabContent(
    state: NotesUiState,
    onNoteClick: (noteId: Long) -> Unit,
    onSubjectSelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onAddNoteClick: () -> Unit,
    onScanDocumentClick: () -> Unit,
    onImageDoubtClick: () -> Unit
) {
    val spacing = QuovexTheme.spacing
    val colors = QuovexTheme.colors

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Smart Actions Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            QuovexCard(
                modifier = Modifier.weight(1f),
                backgroundColor = colors.surfaceVariant,
                onClick = onScanDocumentClick
            ) {
                Row(
                    modifier = Modifier.padding(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        "Scan Notes",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            QuovexCard(
                modifier = Modifier.weight(1f),
                backgroundColor = colors.surfaceVariant,
                onClick = onImageDoubtClick
            ) {
                Row(
                    modifier = Modifier.padding(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(spacing.xs))
                    Text(
                        "Photo Doubt",
                        style = QuovexTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }

        // Subject filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            items(subjectCategories) { cat ->
                QuovexChip(
                    label = cat,
                    isSelected = state.selectedSubject.equals(cat, ignoreCase = true),
                    onClick = { onSubjectSelected(cat) }
                )
            }
        }

        when {
            state.isLoading -> {
                QuovexLoading()
            }

            state.error != null -> {
                QuovexErrorState(message = state.error)
            }

            state.notes.isEmpty() -> {
                QuovexEmptyState(
                    title = "No Study Notes Yet",
                    description = "Capture notes from physical notebooks using OCR, upload PDFs, or paste web/video links.",
                    actionText = "Create Note",
                    onActionClick = onAddNoteClick
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = spacing.lg,
                        end = spacing.lg,
                        bottom = 96.dp,
                        top = spacing.sm
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.base)
                ) {
                    items(state.notes, key = { it.id }) { note ->
                        NoteCard(note = note, onClick = { onNoteClick(note.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteItem,
    onClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    QuovexCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${note.title} note in ${note.subject}" },
        elevation = QuovexTheme.elevation.card,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.base)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    QuovexChip(label = note.subject.uppercase(), isSelected = false)
                    QuovexChip(label = note.inputType.name, isSelected = false)
                }

                val formattedDate = remember(note.updatedAt) {
                    SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(note.updatedAt))
                }
                Text(
                    text = formattedDate,
                    style = QuovexTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }

            Spacer(Modifier.height(spacing.sm))

            Text(
                text = note.title,
                style = QuovexTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                maxLines = 1
            )

            Spacer(Modifier.height(spacing.xxs))

            Text(
                text = note.content,
                style = QuovexTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 2
            )

            if (note.keyPoints.isNotEmpty() || note.flashcardCount > 0) {
                Spacer(Modifier.height(spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (note.keyPoints.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(spacing.xxs))
                            Text(
                                text = "${note.keyPoints.size} Key Points",
                                style = QuovexTheme.typography.labelSmall,
                                color = colors.primary
                            )
                        }
                    }

                    if (note.flashcardCount > 0) {
                        Text(
                            text = "⚡ ${note.flashcardCount} Flashcards",
                            style = QuovexTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlashcardsTabContent(
    state: DeckListUiState,
    onDeckClick: (deckId: Int) -> Unit,
    onCategorySelected: (String) -> Unit,
    onCreateDeckClick: () -> Unit
) {
    val spacing = QuovexTheme.spacing

    when {
        state.isLoading -> {
            QuovexLoading()
        }

        state.error != null -> {
            QuovexErrorState(message = state.error ?: "Error loading decks")
        }

        state.decks.isEmpty() -> {
            QuovexEmptyState(
                title = "No Flashcard Decks",
                description = "Create a custom flashcard deck or generate one using our AI study tools.",
                illustration = painterResource(id = R.drawable.ill_empty_deck),
                actionText = "Create Deck",
                onActionClick = onCreateDeckClick
            )
        }

        else -> {
            Column {
                // Category filter pills
                LazyRow(
                    contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    items(subjectCategories) { cat ->
                        QuovexChip(
                            label = cat,
                            isSelected = state.selectedCategory == cat,
                            onClick = { onCategorySelected(cat) }
                        )
                    }
                }

                val displayDecks = state.filteredDecks
                if (displayDecks.isEmpty()) {
                    QuovexEmptyState(
                        title = "No Decks in ${state.selectedCategory}",
                        description = "No decks match this subject filter."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = spacing.lg, end = spacing.lg, bottom = 96.dp, top = spacing.xs
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.base)
                    ) {
                        items(displayDecks, key = { it.id }) { deck ->
                            DeckCard(deck = deck, onClick = { onDeckClick(deck.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckCard(
    deck: DeckUiModel,
    onClick: () -> Unit
) {
    val colors = QuovexTheme.colors

    QuovexCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .semantics { contentDescription = "${deck.title} deck, ${deck.dueCards} due today, ${deck.masteryPercent} percent mastered" },
        elevation = QuovexTheme.elevation.card,
        onClick = onClick
    ) {
        // Background image
        painterResource(id = deck.bgResId).let { painter ->
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.90f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(QuovexTheme.spacing.base),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuovexChip(label = deck.subject.uppercase(), isSelected = false)
                if (deck.dueCards > 0) {
                    Text(
                        text = "${deck.dueCards} Due",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.warning
                    )
                }
            }

            Column {
                Text(
                    text = deck.title,
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(QuovexTheme.spacing.xxs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${deck.totalCards} Cards • ${deck.masteredCards} Mastered",
                        style = QuovexTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${deck.masteryPercent}%",
                        style = QuovexTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                }
                Spacer(Modifier.height(QuovexTheme.spacing.xs))
                QuovexLinearProgressIndicator(
                    progress = deck.masteryPercent / 100f,
                    height = 4.dp,
                    color = colors.primary,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun ComingSoonTab(
    title: String,
    description: String
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(QuovexTheme.spacing.xl)
        ) {
            Text(
                title,
                style = QuovexTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = QuovexTheme.colors.textPrimary
            )
            Spacer(Modifier.height(QuovexTheme.spacing.md))
            Text(
                description,
                style = QuovexTheme.typography.bodyMedium,
                color = QuovexTheme.colors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun CreateDeckDialog(
    newDeckTitle: String,
    onTitleChange: (String) -> Unit,
    newDeckSubject: String,
    onSubjectChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val spacing = QuovexTheme.spacing
    QuovexDialog(
        onDismissRequest = onDismiss,
        title = "Create New Deck"
    ) {
        Column {
            QuovexTextField(
                value = newDeckTitle,
                onValueChange = onTitleChange,
                label = "Deck Title",
                placeholder = "e.g. Thermodynamics & Heat Transfer"
            )
            Spacer(Modifier.height(spacing.md))
            QuovexTextField(
                value = newDeckSubject,
                onValueChange = onSubjectChange,
                label = "Subject",
                placeholder = "e.g. Physics"
            )
            Spacer(Modifier.height(spacing.xl))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                QuovexButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = QuovexButtonVariant.Ghost,
                    modifier = Modifier.padding(end = spacing.sm),
                    height = 44.dp
                )
                QuovexButton(
                    text = "Create",
                    onClick = onConfirm,
                    enabled = newDeckTitle.isNotBlank() && newDeckSubject.isNotBlank(),
                    variant = QuovexButtonVariant.Primary,
                    height = 44.dp
                )
            }
        }
    }
}
