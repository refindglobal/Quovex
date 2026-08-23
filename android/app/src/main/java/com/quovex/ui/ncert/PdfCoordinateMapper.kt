package com.quovex.ui.ncert

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.quovex.domain.model.PdfPageText
import com.quovex.domain.model.PdfRect
import com.quovex.domain.model.PdfSelection
import com.quovex.domain.model.PdfWordBlock
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Pure coordinate transformation and hit-testing layer for PDF text selection.
 *
 * Maps between:
 * - PDF Document-space coordinates (PDF points: 0..pageWidth, 0..pageHeight)
 * - Android Screen / Compose Viewport coordinates (pixels)
 *
 * Accounts for:
 * - PDF document page dimensions
 * - Rendered page dimensions
 * - Zoom scale (1.0x, 1.5x, 2.0x, etc.)
 * - Translation / pan offsets (panX, panY)
 * - Page offset within a multi-page or centered viewport (pageOffsetX, pageOffsetY)
 */
object PdfCoordinateMapper {

    /**
     * Maps a PDF document-space rectangle to Compose screen pixels.
     */
    fun mapPdfRectToScreen(
        pdfRect: PdfRect,
        pdfPageWidth: Float,
        pdfPageHeight: Float,
        renderedPageWidth: Float,
        renderedPageHeight: Float,
        zoom: Float = 1.0f,
        panX: Float = 0.0f,
        panY: Float = 0.0f,
        pageOffsetX: Float = 0.0f,
        pageOffsetY: Float = 0.0f
    ): Rect {
        if (pdfPageWidth <= 0f || pdfPageHeight <= 0f) {
            return Rect.Zero
        }

        val scale = (renderedPageWidth * zoom) / pdfPageWidth

        val pageScreenLeft = pageOffsetX + panX
        val pageScreenTop = pageOffsetY + panY

        val left = pageScreenLeft + (pdfRect.left * scale)
        val top = pageScreenTop + (pdfRect.top * scale)
        val right = pageScreenLeft + (pdfRect.right * scale)
        val bottom = pageScreenTop + (pdfRect.bottom * scale)

        return Rect(left = left, top = top, right = right, bottom = bottom)
    }

    /**
     * Maps a Compose screen pixel point back into PDF document-space coordinates.
     */
    fun mapScreenPointToPdf(
        screenPoint: Offset,
        pdfPageWidth: Float,
        pdfPageHeight: Float,
        renderedPageWidth: Float,
        renderedPageHeight: Float,
        zoom: Float = 1.0f,
        panX: Float = 0.0f,
        panY: Float = 0.0f,
        pageOffsetX: Float = 0.0f,
        pageOffsetY: Float = 0.0f
    ): Offset {
        if (renderedPageWidth <= 0f || pdfPageWidth <= 0f || zoom <= 0f) {
            return Offset.Zero
        }

        val scale = (renderedPageWidth * zoom) / pdfPageWidth

        val pageScreenLeft = pageOffsetX + panX
        val pageScreenTop = pageOffsetY + panY

        val pdfX = (screenPoint.x - pageScreenLeft) / scale
        val pdfY = (screenPoint.y - pageScreenTop) / scale

        return Offset(
            x = pdfX.coerceIn(0f, pdfPageWidth),
            y = pdfY.coerceIn(0f, pdfPageHeight)
        )
    }

    /**
     * Locates the exact [PdfWordBlock] under or closest to a PDF coordinate point.
     *
     * @param pdfPoint Touch point in PDF document coordinates
     * @param pageText Extracted page text model
     * @param touchToleranceDp Tolerance in PDF points for nearby word detection
     */
    fun findWordAtPoint(
        pdfPoint: Offset,
        pageText: PdfPageText,
        touchToleranceDp: Float = 12f
    ): PdfWordBlock? {
        val allWords = pageText.allWords
        if (allWords.isEmpty()) return null

        // 1. Direct hit check
        for (word in allWords) {
            if (word.bounds.contains(pdfPoint.x, pdfPoint.y)) {
                return word
            }
        }

        // 2. Proximity hit check within line bounds with horizontal tolerance
        var closestWord: PdfWordBlock? = null
        var minDistance = Float.MAX_VALUE

        for (word in allWords) {
            val bounds = word.bounds
            // Expand bounds by tolerance
            val expandedLeft = bounds.left - touchToleranceDp
            val expandedRight = bounds.right + touchToleranceDp
            val expandedTop = bounds.top - touchToleranceDp
            val expandedBottom = bounds.bottom + touchToleranceDp

            if (pdfPoint.x in expandedLeft..expandedRight && pdfPoint.y in expandedTop..expandedBottom) {
                val dx = pdfPoint.x - bounds.centerX
                val dy = pdfPoint.y - bounds.centerY
                val dist = (dx * dx) + (dy * dy)
                if (dist < minDistance) {
                    minDistance = dist
                    closestWord = word
                }
            }
        }

        return closestWord
    }

    /**
     * Builds a [PdfSelection] spanning from [startWordIndex] to [endWordIndex] in reading order.
     * Reconstructs natural multiline text with spaces and line breaks.
     */
    fun createSelection(
        pageText: PdfPageText,
        startWordIndex: Int,
        endWordIndex: Int,
        chapterId: String = "",
        chapterTitle: String = "",
        bookTitle: String = "",
        subject: String = ""
    ): PdfSelection? {
        val allWords = pageText.allWords
        if (allWords.isEmpty()) return null

        val firstIdx = min(startWordIndex, endWordIndex).coerceIn(0, allWords.lastIndex)
        val lastIdx = max(startWordIndex, endWordIndex).coerceIn(0, allWords.lastIndex)

        val selectedWords = allWords.subList(firstIdx, lastIdx + 1)
        if (selectedWords.isEmpty()) return null

        // Group by line to reconstruct natural spacing and bounding boxes
        val lineGroups = selectedWords.groupBy { it.lineIndex }
        val textBuilder = StringBuilder()
        val highlightRects = mutableListOf<PdfRect>()

        var isFirstLine = true
        for ((_, wordsOnLine) in lineGroups) {
            if (!isFirstLine) {
                textBuilder.append(" ")
            }
            isFirstLine = false

            // Join words with space
            val lineText = wordsOnLine.joinToString(" ") { it.text }
            textBuilder.append(lineText)

            // Compute line bounding box in PDF space
            val lineLeft = wordsOnLine.minOf { it.bounds.left }
            val lineTop = wordsOnLine.minOf { it.bounds.top }
            val lineRight = wordsOnLine.maxOf { it.bounds.right }
            val lineBottom = wordsOnLine.maxOf { it.bounds.bottom }

            highlightRects.add(
                PdfRect(
                    left = lineLeft,
                    top = lineTop,
                    right = lineRight,
                    bottom = lineBottom
                )
            )
        }

        return PdfSelection(
            pageNumber = pageText.pageNumber,
            selectedText = textBuilder.toString().trim(),
            startWordIndex = firstIdx,
            endWordIndex = lastIdx,
            highlightRects = highlightRects,
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            bookTitle = bookTitle,
            subject = subject
        )
    }
}
