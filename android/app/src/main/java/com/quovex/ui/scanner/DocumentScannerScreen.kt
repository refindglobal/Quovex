package com.quovex.ui.scanner

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.quovex.domain.model.EditableScannedChapter
import com.quovex.domain.model.ScanPhase
import com.quovex.domain.model.ScannedPage
import com.quovex.theme.QuovexTheme
import com.quovex.ui.components.QuovexButton
import com.quovex.ui.components.QuovexButtonVariant
import com.quovex.ui.components.QuovexCard
import com.quovex.ui.components.QuovexChip
import com.quovex.ui.components.QuovexErrorState
import com.quovex.ui.components.QuovexLoading
import com.quovex.ui.components.QuovexTextField
import com.quovex.ui.components.QuovexTopAppBar
import java.io.File

/**
 * Adobe Scan-style multi-page Document Scanner Screen.
 *
 * Workflow:
 * 1. ML Kit Document Scanner: Capture / Edge Detection / Perspective Crop / Enhance
 * 2. Multi-Page Review: Page-strip display, reorder, delete, add more pages
 * 3. AI Document Intelligence: Batched analysis via ai/document/analyze
 * 4. Editable Confirmation: Chapter/Subtopic tree review & inline editing
 * 5. Save: Ingested as structured notes and Learning Material into Room
 */
@Composable
fun DocumentScannerScreen(
    viewModel: DocumentScannerViewModel,
    onBackClick: () -> Unit,
    onNoteSaved: (noteId: Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    val context = LocalContext.current
    val activity = context as? Activity

    // ── ML KIT DOCUMENT SCANNER LAUNCHER ──────────────────────────────────────
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pageUris = scanResult?.pages?.map { it.imageUri } ?: emptyList()
            if (pageUris.isNotEmpty()) {
                viewModel.onPagesCaptured(pageUris)
            }
        }
    }

    // Function to launch ML Kit Document Scanner
    val launchMlKitScanner: () -> Unit = {
        if (activity != null) {
            try {
                val options = GmsDocumentScannerOptions.Builder()
                    .setGalleryImportAllowed(true)
                    .setPageLimit(30)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                    .build()

                val scanner = GmsDocumentScanning.getClient(options)
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { intentSender ->
                        scannerLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build()
                        )
                    }
                    .addOnFailureListener {
                        // ML Kit scanner unavailable (e.g. non-GMS device)
                    }
            } catch (_: Exception) {}
        }
    }

    // ── GALLERY MULTI-SELECT FALLBACK LAUNCHER ─────────────────────────────────
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.onPagesCaptured(uris)
        }
    }

    Scaffold(
        topBar = {
            QuovexTopAppBar(
                title = when (state.phase) {
                    ScanPhase.IDLE -> "Document Scanner"
                    ScanPhase.SCANNING, ScanPhase.REVIEWING_PAGES -> "Review Pages (${state.scannedPages.size})"
                    ScanPhase.ANALYZING -> "Quovex AI Organizing..."
                    ScanPhase.CONFIRMING_ORGANIZATION -> "Confirm Notes"
                    ScanPhase.SAVING -> "Saving Notes..."
                    ScanPhase.SAVED -> "Saved"
                    ScanPhase.ERROR -> "Scanner"
                },
                onBackClick = {
                    if (state.phase == ScanPhase.CONFIRMING_ORGANIZATION) {
                        // Go back to reviewing pages
                    }
                    onBackClick()
                }
            )
        },
        containerColor = colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.phase) {
                ScanPhase.IDLE -> {
                    InitialScannerPrompt(
                        onScanClick = launchMlKitScanner,
                        onGalleryClick = { galleryLauncher.launch("image/*") }
                    )
                }

                ScanPhase.SCANNING -> {
                    QuovexLoading(
                        message = "Processing captured pages...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                ScanPhase.REVIEWING_PAGES -> {
                    ReviewPagesContent(
                        state = state,
                        onAddMorePages = launchMlKitScanner,
                        onImportFromGallery = { galleryLauncher.launch("image/*") },
                        onMoveUp = { viewModel.movePageUp(it) },
                        onMoveDown = { viewModel.movePageDown(it) },
                        onDeletePage = { viewModel.deletePage(it) },
                        onSelectSubject = { viewModel.selectSubject(it) },
                        onAnalyzeWithAi = { viewModel.analyzeDocumentWithAi() }
                    )
                }

                ScanPhase.ANALYZING -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = colors.primary
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Analyzing Document Structure",
                            style = QuovexTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Quovex AI is reading pages in batches, identifying chapters, and structuring notes...",
                            style = QuovexTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                ScanPhase.CONFIRMING_ORGANIZATION -> {
                    ConfirmOrganizationContent(
                        state = state,
                        onTitleChanged = { viewModel.onDocumentTitleChanged(it) },
                        onChapterTitleChanged = { id, title -> viewModel.onChapterTitleChanged(id, title) },
                        onDeleteChapter = { viewModel.deleteChapter(it) },
                        onSubtopicContentChanged = { chId, subId, content ->
                            viewModel.onSubtopicContentChanged(chId, subId, content)
                        },
                        onSave = { viewModel.saveStructuredNotes(onNoteSaved) }
                    )
                }

                ScanPhase.SAVING -> {
                    QuovexLoading(
                        message = "Saving structured notes to your Knowledge Hub...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                ScanPhase.SAVED -> {
                    // Handled via callback
                }

                ScanPhase.ERROR -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        QuovexErrorState(message = state.error ?: "An unexpected error occurred.")
                        Spacer(Modifier.height(16.dp))
                        QuovexButton(
                            text = "Try Again",
                            onClick = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

// ── 1. INITIAL SCANNER PROMPT ─────────────────────────────────────────────────

@Composable
private fun InitialScannerPrompt(
    onScanClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        QuovexCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(spacing.base))

                Text(
                    text = "Smart Multi-Page Scanner",
                    style = QuovexTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(Modifier.height(spacing.xs))

                Text(
                    text = "Scan handwritten notebooks, textbooks, or documents with automatic edge detection, perspective crop, and AI-powered chapter organization.",
                    style = QuovexTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(Modifier.height(spacing.xl))

                QuovexButton(
                    text = "Scan Document",
                    leadingIcon = {
                        Icon(Icons.Filled.DocumentScanner, contentDescription = null)
                    },
                    onClick = onScanClick,
                    variant = QuovexButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(spacing.sm))

                QuovexButton(
                    text = "Import Pages from Gallery",
                    leadingIcon = {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    },
                    onClick = onGalleryClick,
                    variant = QuovexButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ── 2. REVIEW PAGES CONTENT ───────────────────────────────────────────────────

@Composable
private fun ReviewPagesContent(
    state: DocumentScannerUiState,
    onAddMorePages: () -> Unit,
    onImportFromGallery: () -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onSelectSubject: (String) -> Unit,
    onAnalyzeWithAi: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // Error banner if any
        if (!state.error.isNullOrBlank()) {
            item {
                QuovexErrorState(message = state.error!!)
            }
        }

        // Subject selector (Universal streams)
        item {
            Column {
                Text(
                    text = "Subject Context (helps Quovex AI)",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(spacing.xs))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    items(state.availableSubjects) { subj ->
                        QuovexChip(
                            label = subj,
                            isSelected = state.selectedSubject.equals(subj, ignoreCase = true),
                            onClick = { onSelectSubject(subj) }
                        )
                    }
                }
            }
        }

        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Captured Pages (${state.scannedPages.size})",
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onAddMorePages, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "Add page", tint = colors.primary)
                    }
                    IconButton(onClick = onImportFromGallery, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Collections, contentDescription = "Import", tint = colors.textSecondary)
                    }
                }
            }
        }

        // Page list with reorder and delete controls
        itemsIndexed(state.scannedPages) { index, page ->
            ScannedPageRowItem(
                page = page,
                index = index,
                isFirst = index == 0,
                isLast = index == state.scannedPages.size - 1,
                onMoveUp = { onMoveUp(index) },
                onMoveDown = { onMoveDown(index) },
                onDelete = { onDeletePage(index) }
            )
        }

        // Action CTA
        item {
            Spacer(Modifier.height(spacing.sm))
            QuovexButton(
                text = "Analyze & Organize with Quovex AI",
                leadingIcon = {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                },
                onClick = onAnalyzeWithAi,
                variant = QuovexButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(spacing.lg))
        }
    }
}

@Composable
private fun ScannedPageRowItem(
    page: ScannedPage,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    // Load thumbnail from file path
    val thumbnailBitmap = remember(page.thumbnailFilePath) {
        try {
            BitmapFactory.decodeFile(page.thumbnailFilePath)
        } catch (_: Exception) {
            null
        }
    }

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap.asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.DocumentScanner,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(Modifier.width(spacing.md))

            // Page info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Page ${index + 1}",
                    style = QuovexTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "${page.widthPx} × ${page.heightPx} px",
                    style = QuovexTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            // Move Up / Down / Delete actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.ArrowUpward,
                            contentDescription = "Move up",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.ArrowDownward,
                            contentDescription = "Move down",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete page",
                        tint = colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ── 3. CONFIRM ORGANIZATION CONTENT ───────────────────────────────────────────

@Composable
private fun ConfirmOrganizationContent(
    state: DocumentScannerUiState,
    onTitleChanged: (String) -> Unit,
    onChapterTitleChanged: (String, String) -> Unit,
    onDeleteChapter: (String) -> Unit,
    onSubtopicContentChanged: (String, String, String) -> Unit,
    onSave: () -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // AI detection summary header
        item {
            QuovexCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = colors.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(spacing.xs))
                        Text(
                            text = "AI-Organized Document",
                            style = QuovexTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Subject: ${state.detectedSubject} • Stream: ${state.detectedStream} • ${state.editableChapters.size} Chapters Identified",
                        style = QuovexTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // Document Title
        item {
            QuovexTextField(
                value = state.documentTitle,
                onValueChange = onTitleChanged,
                label = "Document Title"
            )
        }

        // Chapters List (Editable)
        item {
            Text(
                text = "Structure & Notes (Review & Edit)",
                style = QuovexTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }

        items(state.editableChapters) { chapter ->
            EditableChapterCard(
                chapter = chapter,
                onTitleChanged = { onChapterTitleChanged(chapter.id, it) },
                onDelete = { onDeleteChapter(chapter.id) },
                onSubtopicContentChanged = { subId, content ->
                    onSubtopicContentChanged(chapter.id, subId, content)
                }
            )
        }

        // Save CTA
        item {
            Spacer(Modifier.height(spacing.sm))
            QuovexButton(
                text = if (state.isSaving) "Saving Notes..." else "Save Structured Notes",
                leadingIcon = {
                    Icon(Icons.Filled.Save, contentDescription = null)
                },
                onClick = onSave,
                enabled = !state.isSaving,
                variant = QuovexButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(spacing.xl))
        }
    }
}

@Composable
private fun EditableChapterCard(
    chapter: EditableScannedChapter,
    onTitleChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onSubtopicContentChanged: (subtopicId: String, content: String) -> Unit
) {
    val colors = QuovexTheme.colors
    val spacing = QuovexTheme.spacing
    var isExpanded by remember { mutableStateOf(true) }

    QuovexCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.surface
    ) {
        Column(modifier = Modifier.padding(spacing.md)) {
            // Chapter header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chapter.title,
                    onValueChange = onTitleChanged,
                    label = { Text("Chapter Title") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = "Expand/Collapse",
                        tint = colors.textSecondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete chapter",
                        tint = colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Subtopics (collapsible)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    chapter.subtopics.forEach { sub ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surfaceVariant.copy(alpha = 0.4f))
                                .padding(spacing.sm)
                        ) {
                            Text(
                                text = "Subtopic: ${sub.title}",
                                style = QuovexTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            if (sub.keyPoints.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                sub.keyPoints.forEach { pt ->
                                    Text(
                                        text = "• $pt",
                                        style = QuovexTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = sub.content,
                                onValueChange = { onSubtopicContentChanged(sub.id, it) },
                                label = { Text("Notes & Details") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                maxLines = 6
                            )
                        }
                    }
                }
            }
        }
    }
}
