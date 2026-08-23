package com.quovex.ui.ncert

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quovex.domain.model.PdfPageText
import com.quovex.domain.model.PdfSelection
import kotlin.math.roundToInt

private val QuovexEmerald = Color(0xFF00C896)
private val SelectionHighlightColor = Color(0x5000C896)
private val SelectionBorderColor = Color(0x9000C896)

/**
 * Real PDF text selection transparent overlay.
 *
 * Sits directly on top of AndroidPdfViewer, intercepting gestures,
 * rendering in-place word highlight rectangles, draggable handles,
 * and the floating Quovex AI contextual toolbar.
 */
@Composable
fun SelectablePdfOverlay(
    pageText: PdfPageText?,
    selection: PdfSelection?,
    renderedPageWidth: Float,
    renderedPageHeight: Float,
    zoom: Float = 1.0f,
    panX: Float = 0.0f,
    panY: Float = 0.0f,
    pageOffsetX: Float = 0.0f,
    pageOffsetY: Float = 0.0f,
    onWordLongPressed: (startWordIndex: Int) -> Unit = {},
    onSelectionHandleDragged: (isStartHandle: Boolean, dragPoint: Offset) -> Unit,
    onClearSelection: () -> Unit,
    onExplainAction: (selectedText: String) -> Unit,
    onSimplifyAction: (selectedText: String) -> Unit,
    onSummarizeAction: (selectedText: String) -> Unit,
    onAddToNotesAction: (selectedText: String) -> Unit,
    onMakeFlashcardsAction: (selectedText: String) -> Unit,
    onMakeQuizAction: (selectedText: String) -> Unit,
    onAskAiAction: (selectedText: String) -> Unit,
    onAskAboutPageAction: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Only intercept touch on background if selection is active (to dismiss on tap outside)
    val backgroundModifier = if (selection != null) {
        Modifier.pointerInput(selection) {
            detectTapGestures(
                onTap = { onClearSelection() }
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(backgroundModifier)
    ) {
        // 1. Render text highlights
        if (pageText != null && selection != null && selection.highlightRects.isNotEmpty()) {
            val screenRects = selection.highlightRects.map { pdfRect ->
                PdfCoordinateMapper.mapPdfRectToScreen(
                    pdfRect = pdfRect,
                    pdfPageWidth = pageText.pageWidth,
                    pdfPageHeight = pageText.pageHeight,
                    renderedPageWidth = renderedPageWidth,
                    renderedPageHeight = renderedPageHeight,
                    zoom = zoom,
                    panX = panX,
                    panY = panY,
                    pageOffsetX = pageOffsetX,
                    pageOffsetY = pageOffsetY
                )
            }

            for (rect in screenRects) {
                if (rect.width > 0 && rect.height > 0) {
                    val leftDp = with(density) { rect.left.toDp() }
                    val topDp = with(density) { rect.top.toDp() }
                    val widthDp = with(density) { rect.width.toDp() }
                    val heightDp = with(density) { rect.height.toDp() }

                    Box(
                        modifier = Modifier
                            .offset(x = leftDp, y = topDp)
                            .size(width = widthDp, height = heightDp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SelectionHighlightColor)
                            .border(0.75.dp, SelectionBorderColor, RoundedCornerShape(3.dp))
                            .semantics {
                                contentDescription = "Selected text highlight: "
                            }
                    )
                }
            }

            // 2. Selection Handles (Start & End)
            val firstRect = screenRects.firstOrNull()
            val lastRect = screenRects.lastOrNull()

            if (firstRect != null) {
                SelectionHandle(
                    position = Offset(firstRect.left, firstRect.bottom),
                    isStart = true,
                    onDrag = { dragOffset ->
                        onSelectionHandleDragged(true, dragOffset)
                    }
                )
            }

            if (lastRect != null) {
                SelectionHandle(
                    position = Offset(lastRect.right, lastRect.bottom),
                    isStart = false,
                    onDrag = { dragOffset ->
                        onSelectionHandleDragged(false, dragOffset)
                    }
                )
            }

            // 3. Floating Contextual Action Bar
            val topMostY = screenRects.minOfOrNull { it.top } ?: 100f
            val centerX = screenRects.map { (it.left + it.right) / 2f }.average().toFloat()

            PdfSelectionActionBar(
                topPosition = topMostY,
                centerPosition = centerX,
                selectedText = selection.selectedText,
                onExplain = { onExplainAction(selection.selectedText) },
                onSimplify = { onSimplifyAction(selection.selectedText) },
                onSummarize = { onSummarizeAction(selection.selectedText) },
                onAddToNotes = { onAddToNotesAction(selection.selectedText) },
                onMakeFlashcards = { onMakeFlashcardsAction(selection.selectedText) },
                onMakeQuiz = { onMakeQuizAction(selection.selectedText) },
                onAskAi = { onAskAiAction(selection.selectedText) }
            )
        }

        // 4. Image-Only Page Fallback
        if (pageText != null && pageText.isImageOnly) {
            ImagePageFallbackPill(
                onAskAboutPage = onAskAboutPageAction,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

/**
 * Draggable pin handle at start/end of text selection.
 */
@Composable
private fun BoxScope.SelectionHandle(
    position: Offset,
    isStart: Boolean,
    onDrag: (dragPosition: Offset) -> Unit
) {
    val density = LocalDensity.current
    val handleSize = 22.dp
    val handleSizePx = with(density) { handleSize.toPx() }

    val xOffset = if (isStart) position.x - handleSizePx else position.x
    val yOffset = position.y

    val xDp = with(density) { xOffset.toDp() }
    val yDp = with(density) { yOffset.toDp() }

    Box(
        modifier = Modifier
            .offset(x = xDp, y = yDp)
            .size(handleSize)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    onDrag(change.position + Offset(xOffset, yOffset))
                }
            }
            .semantics {
                contentDescription = if (isStart) "Start text selection handle" else "End text selection handle"
            },
        contentAlignment = Alignment.Center
    ) {
        // Circular teardrop handle
        Box(
            modifier = Modifier
                .size(18.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(QuovexEmerald)
                .border(1.5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/**
 * Floating Contextual Quovex AI Action Toolbar.
 */
@Composable
private fun BoxScope.PdfSelectionActionBar(
    topPosition: Float,
    centerPosition: Float,
    selectedText: String,
    onExplain: () -> Unit,
    onSimplify: () -> Unit,
    onSummarize: () -> Unit,
    onAddToNotes: () -> Unit,
    onMakeFlashcards: () -> Unit,
    onMakeQuiz: () -> Unit,
    onAskAi: () -> Unit
) {
    val density = LocalDensity.current
    val topDp = with(density) { (topPosition - 65f).coerceAtLeast(16f).toDp() }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = topDp)
            .padding(horizontal = 12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF141916),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26332C)),
            modifier = Modifier.semantics {
                contentDescription = "Quovex AI Selection Toolbar"
            }
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ActionButtonItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "Explain",
                    accessibilityLabel = "Explain selected text",
                    tint = QuovexEmerald,
                    onClick = onExplain
                )
                ActionButtonItem(
                    icon = Icons.Default.Lightbulb,
                    label = "Simplify",
                    accessibilityLabel = "Simplify selected text",
                    tint = Color(0xFFFFD166),
                    onClick = onSimplify
                )
                ActionButtonItem(
                    icon = Icons.Default.Description,
                    label = "Summarize",
                    accessibilityLabel = "Summarize selected text",
                    tint = Color(0xFF4EA8DE),
                    onClick = onSummarize
                )
                ActionButtonItem(
                    icon = Icons.Default.NoteAdd,
                    label = "Add to Notes",
                    accessibilityLabel = "Add selection to Notes",
                    tint = Color(0xFFE0AAFF),
                    onClick = onAddToNotes
                )
                ActionButtonItem(
                    icon = Icons.Default.Style,
                    label = "Flashcards",
                    accessibilityLabel = "Make Flashcards from selected text",
                    tint = Color(0xFF06D6A0),
                    onClick = onMakeFlashcards
                )
                ActionButtonItem(
                    icon = Icons.Default.Quiz,
                    label = "Quiz",
                    accessibilityLabel = "Make Quiz from selected text",
                    tint = Color(0xFFFF9F1C),
                    onClick = onMakeQuiz
                )
                ActionButtonItem(
                    icon = Icons.Default.HelpOutline,
                    label = "Ask AI",
                    accessibilityLabel = "Ask Quovex AI about selected text",
                    tint = Color(0xFFE2E8F0),
                    onClick = onAskAi
                )
            }
        }
    }
}

@Composable
private fun ActionButtonItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accessibilityLabel: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E2521),
        modifier = Modifier.semantics {
            contentDescription = accessibilityLabel
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}

/**
 * Fallback badge for image-only (scanned) pages.
 */
@Composable
private fun ImagePageFallbackPill(
    onAskAboutPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141916).copy(alpha = 0.92f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26332C)),
        shadowElevation = 6.dp,
        modifier = modifier.semantics {
            contentDescription = "Scanned image page detected"
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = QuovexEmerald,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Scanned Page",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Surface(
                onClick = onAskAboutPage,
                shape = RoundedCornerShape(12.dp),
                color = QuovexEmerald.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "Ask about this page",
                    fontSize = 12.sp,
                    color = QuovexEmerald,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
