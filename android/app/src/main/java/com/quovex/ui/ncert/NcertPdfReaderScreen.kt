package com.quovex.ui.ncert

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnDrawListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnRenderListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.quovex.ui.components.AiActionContext
import com.quovex.ui.components.QuovexAiActionSheet
import java.io.File

/**
 * In-app NCERT PDF Reader Screen with Real Two-Layer Text Selection Overlay.
 *
 * Layer 1: AndroidPdfViewer (PDFView) — Visual PDF rendering.
 * Layer 2: SelectablePdfOverlay — Native PDF document text layer & geometry (PDFBox).
 *
 * Pipeline Separation:
 * A. Native Text PDF: PDFBox text layer + document-space coordinate mapping.
 * B. Image/Scanned PDF: "Ask about this page" / "Select Area" vision flow.
 * Fallback: Top-bar "Extract Text" review panel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NcertPdfReaderScreen(
    onBack: () -> Unit,
    onAskAi: (text: String, context: AiActionContext) -> Unit = { _, _ -> },
    viewModel: NcertPdfReaderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var pdfViewRef by remember { mutableStateOf<PDFView?>(null) }
    var renderedWidth by remember { mutableFloatStateOf(1080f) }
    var renderedHeight by remember { mutableFloatStateOf(1920f) }
    var currentZoom by remember { mutableFloatStateOf(1.0f) }
    var currentPanX by remember { mutableFloatStateOf(0.0f) }
    var currentPanY by remember { mutableFloatStateOf(0.0f) }

    LaunchedEffect(state.actionMessage) {
        val msg = state.actionMessage
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.chapter?.chapterTitle ?: "NCERT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.loadPhase == PdfLoadPhase.READY && state.totalPages > 0) {
                            Text(
                                text = "Page ${state.currentPage + 1} of ${state.totalPages}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.loadPhase == PdfLoadPhase.READY) {
                        // Fallback text extraction review panel
                        IconButton(onClick = { viewModel.extractPageText() }) {
                            Icon(
                                Icons.Default.TextFields,
                                contentDescription = "Review extracted text",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Quovex AI actions
                        IconButton(onClick = { viewModel.toggleAiActionSheet(true) }) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Quovex AI actions",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.loadPhase) {
                PdfLoadPhase.IDLE, PdfLoadPhase.CHECKING_CACHE -> {
                    PdfLoadingState("Checking cache…")
                }

                PdfLoadPhase.DOWNLOADING -> {
                    PdfLoadingState("Downloading from NCERT…")
                }

                PdfLoadPhase.LOADING, PdfLoadPhase.READY -> {
                    val pdfPath = state.localPdfPath
                    if (pdfPath != null) {
                        // Gesture detector for long-press selection on native PDFView
                        val gestureDetector = remember(state.nativePageText, renderedWidth, renderedHeight, currentZoom, currentPanX, currentPanY) {
                            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                                override fun onLongPress(e: MotionEvent) {
                                    val pageText = state.nativePageText ?: return
                                    if (pageText.isImageOnly) return
                                    val pdfPoint = PdfCoordinateMapper.mapScreenPointToPdf(
                                        screenPoint = androidx.compose.ui.geometry.Offset(e.x, e.y),
                                        pdfPageWidth = pageText.pageWidth,
                                        pdfPageHeight = pageText.pageHeight,
                                        renderedPageWidth = renderedWidth,
                                        renderedPageHeight = renderedHeight,
                                        zoom = currentZoom,
                                        panX = currentPanX,
                                        panY = currentPanY
                                    )
                                    val word = PdfCoordinateMapper.findWordAtPoint(pdfPoint, pageText, touchToleranceDp = 20f)
                                    if (word != null) {
                                        viewModel.onWordLongPressed(word.globalWordIndex)
                                    }
                                }

                                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                                    if (state.currentSelection != null) {
                                        viewModel.clearSelection()
                                        return true
                                    }
                                    return false
                                }
                            })
                        }

                        // Layer 1: Visual PDF Renderer
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { ctx ->
                                PDFView(ctx, null).apply {
                                    pdfViewRef = this
                                    setOnTouchListener { _, event ->
                                        gestureDetector.onTouchEvent(event)
                                        false // Return false so PDFView handles native scrolling/swiping smoothly!
                                    }
                                    fromFile(File(pdfPath))
                                        .defaultPage(state.currentPage)
                                        .enableSwipe(true)
                                        .swipeHorizontal(false)
                                        .enableDoubletap(true)
                                        .enableAntialiasing(true)
                                        .spacing(10)
                                        .autoSpacing(false)
                                        .pageFitPolicy(FitPolicy.WIDTH)
                                        .pageSnap(true)
                                        .pageFling(true)
                                        .onLoad(OnLoadCompleteListener { nbPages ->
                                            viewModel.onPdfLoaded(nbPages)
                                            renderedWidth = width.toFloat().takeIf { it > 0f } ?: 1080f
                                            renderedHeight = height.toFloat().takeIf { it > 0f } ?: 1920f
                                            currentZoom = zoom
                                            currentPanX = currentXOffset
                                            currentPanY = currentYOffset
                                        })
                                        .onPageChange(OnPageChangeListener { page, _ ->
                                            viewModel.onPageChanged(page)
                                            renderedWidth = width.toFloat().takeIf { it > 0f } ?: 1080f
                                            renderedHeight = height.toFloat().takeIf { it > 0f } ?: 1920f
                                            currentZoom = zoom
                                            currentPanX = currentXOffset
                                            currentPanY = currentYOffset
                                        })
                                        .onRender(OnRenderListener { _ ->
                                            renderedWidth = width.toFloat().takeIf { it > 0f } ?: 1080f
                                            renderedHeight = height.toFloat().takeIf { it > 0f } ?: 1920f
                                            currentZoom = zoom
                                            currentPanX = currentXOffset
                                            currentPanY = currentYOffset
                                        })
                                        .onDraw(OnDrawListener { _, _, _, _ ->
                                            renderedWidth = width.toFloat().takeIf { it > 0f } ?: 1080f
                                            renderedHeight = height.toFloat().takeIf { it > 0f } ?: 1920f
                                            currentZoom = zoom
                                            currentPanX = currentXOffset
                                            currentPanY = currentYOffset
                                        })
                                        .load()
                                }
                            },
                            update = { view ->
                                pdfViewRef = view
                                if (view.currentPage != state.currentPage) {
                                    view.jumpTo(state.currentPage)
                                }
                                if (view.width > 0 && view.height > 0) {
                                    renderedWidth = view.width.toFloat()
                                    renderedHeight = view.height.toFloat()
                                    currentZoom = view.zoom
                                    currentPanX = view.currentXOffset
                                    currentPanY = view.currentYOffset
                                }
                            }
                        )

                        // Layer 2: Transparent Selectable Text Overlay (rendered only when selection is active)
                        if (state.currentSelection != null) {
                            SelectablePdfOverlay(
                                pageText = state.nativePageText,
                                selection = state.currentSelection,
                                renderedPageWidth = renderedWidth,
                                renderedPageHeight = renderedHeight,
                                zoom = currentZoom,
                                panX = currentPanX,
                                panY = currentPanY,
                                onSelectionHandleDragged = { isStartHandle, dragPoint ->
                                    viewModel.onSelectionHandleDragged(
                                        isStartHandle = isStartHandle,
                                        dragPoint = dragPoint,
                                        renderedPageWidth = renderedWidth,
                                        renderedPageHeight = renderedHeight,
                                        zoom = currentZoom,
                                        panX = currentPanX,
                                        panY = currentPanY
                                    )
                                },
                                onClearSelection = {
                                    viewModel.clearSelection()
                                },
                                onExplainAction = { _ ->
                                    viewModel.askAiAboutSelection("explain")
                                },
                                onSimplifyAction = { _ ->
                                    viewModel.askAiAboutSelection("simplify")
                                },
                                onSummarizeAction = { _ ->
                                    viewModel.askAiAboutSelection("summarize")
                                },
                                onAddToNotesAction = {
                                    viewModel.addSelectionToNotes()
                                },
                                onMakeFlashcardsAction = {
                                    viewModel.makeFlashcardsFromSelection()
                                },
                                onMakeQuizAction = {
                                    viewModel.makeQuizFromSelection()
                                },
                                onAskAiAction = { _ ->
                                    viewModel.askAiAboutSelection("ask_ai")
                                },
                                onAskAboutPageAction = {
                                    val pageNum = state.currentPage + 1
                                    onAskAi("Explain the key concepts on Page $pageNum of ${state.chapter?.chapterTitle ?: "this chapter"}", AiActionContext.PAGE)
                                }
                            )
                        }

                        // Layer 3: Floating Page Navigator Pill
                        if (state.currentSelection == null && state.loadPhase == PdfLoadPhase.READY && state.totalPages > 1) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                                tonalElevation = 6.dp,
                                shadowElevation = 8.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (state.currentPage > 0) {
                                                val newPage = state.currentPage - 1
                                                viewModel.onPageChanged(newPage)
                                                pdfViewRef?.jumpTo(newPage)
                                            }
                                        },
                                        enabled = state.currentPage > 0
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronLeft,
                                            contentDescription = "Previous page",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        text = "${state.currentPage + 1} / ${state.totalPages}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (state.currentPage < state.totalPages - 1) {
                                                val newPage = state.currentPage + 1
                                                viewModel.onPageChanged(newPage)
                                                pdfViewRef?.jumpTo(newPage)
                                            }
                                        },
                                        enabled = state.currentPage < state.totalPages - 1
                                    ) {
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Next page",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PdfLoadPhase.ERROR -> {
                    PdfErrorState(
                        message = state.error ?: "NCERT content is temporarily unavailable.",
                        onRetry = { viewModel.retry() },
                        onBack = onBack
                    )
                }
            }

            // Fallback Extracted Text Review Panel
            val extractedText = state.extractedPageText
            if (extractedText != null) {
                ExtractedTextPanel(
                    text = extractedText,
                    pageNumber = state.currentPage + 1,
                    onClose = { viewModel.clearExtractedText() },
                    onAiAction = { text -> onAskAi(text, AiActionContext.SELECTED_TEXT) }
                )
            }
        }
    }

    // In-Place AI Explanation Bottom Sheet
    if (state.isAiResultSheetVisible) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { viewModel.dismissAiResultSheet() },
            sheetState = sheetState,
            containerColor = Color(0xFF101613),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00C896),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = state.aiPromptTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { viewModel.dismissAiResultSheet() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                // Badge with Subject & Chapter
                if (state.chapter != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E2823),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = "${state.chapter?.subject ?: "NCERT"} • Chapter ${state.chapter?.chapterNumber}: ${state.chapter?.chapterTitle}",
                            fontSize = 12.sp,
                            color = Color(0xFF00C896),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Selected Quote preview
                if (state.aiSelectedQuote.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF161F1B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26352E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "\"${state.aiSelectedQuote}\"",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(12.dp),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // AI Response Content
                if (state.isAiResponseLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF00C896), modifier = Modifier.size(36.dp))
                        Text(
                            text = "Analyzing with Quovex AI...",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else if (state.aiResponseError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2C1517),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = state.aiResponseError ?: "Error getting explanation",
                                color = Color(0xFFFF6B6B),
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.askAiAboutSelection(state.aiPromptType) }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                } else if (state.aiResponseText != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .weight(weight = 1f, fill = false)
                    ) {
                        Text(
                            text = state.aiResponseText ?: "",
                            fontSize = 14.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action buttons: Copy, Save Note, Continue in Chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("AI Explanation", state.aiResponseText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Copy", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { viewModel.saveAiExplanationAsNote() },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save to Notes", fontSize = 13.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                val query = state.aiSelectedQuote
                                viewModel.dismissAiResultSheet()
                                onAskAi(query, AiActionContext.SELECTED_TEXT)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("AI Chat", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Full-Page AI Action Sheet
    if (state.isAiActionSheetVisible) {
        val contextLabel = state.chapter?.let {
            "Chapter ${it.chapterNumber}: ${it.chapterTitle}"
        } ?: ""

        QuovexAiActionSheet(
            context = AiActionContext.PAGE,
            contextLabel = contextLabel,
            onActionSelected = { actionId ->
                when (actionId) {
                    "ask_ai" -> onAskAi("Ask about page ${state.currentPage + 1}", AiActionContext.PAGE)
                    "explain" -> onAskAi("Explain page ${state.currentPage + 1} of ${state.chapter?.chapterTitle ?: "this chapter"}", AiActionContext.PAGE)
                    "summarize" -> onAskAi("Summarize page ${state.currentPage + 1} of ${state.chapter?.chapterTitle ?: "this chapter"}", AiActionContext.PAGE)
                    else -> onAskAi(actionId, AiActionContext.PAGE)
                }
            },
            onDismiss = { viewModel.toggleAiActionSheet(false) }
        )
    }
}

// ── LOADING STATE ─────────────────────────────────────────────────────────────

@Composable
private fun PdfLoadingState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── ERROR STATE ───────────────────────────────────────────────────────────────

@Composable
private fun PdfErrorState(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("Go Back")
        }
    }
}

// ── EXTRACTED TEXT PANEL (FALLBACK REVIEW) ────────────────────────────────────

@Composable
private fun ExtractedTextPanel(
    text: String,
    pageNumber: Int,
    onClose: () -> Unit,
    onAiAction: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Extracted Text — Page ",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Text(
                text = "Reviewing text extracted from native PDF layer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            if (text.isBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "This page uses scanned images — no text layer available.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Use \"Ask about this Page\" to analyze the page visually with Quovex AI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    FilledTonalButton(onClick = { onAiAction("") }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Ask about this Page")
                    }
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onAiAction(text) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ask AI")
                    }
                    FilledTonalButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
