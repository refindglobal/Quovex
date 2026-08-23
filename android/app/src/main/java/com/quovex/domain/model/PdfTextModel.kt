package com.quovex.domain.model

/**
 * Model representing a rectangular bounding box in PDF document space.
 * Coordinate system:
 * - left: horizontal distance from the left edge of the PDF page (in PDF points)
 * - top: vertical distance from the top edge of the PDF page (in PDF points)
 * - right: horizontal distance from the left edge (left + width)
 * - bottom: vertical distance from the top edge (top + height)
 */
data class PdfRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }
}

/**
 * Represents a single word in the native PDF text layer with its document-space bounding box.
 */
data class PdfWordBlock(
    val text: String,
    val pageNumber: Int,
    val bounds: PdfRect,
    val lineIndex: Int,
    val wordIndex: Int,
    val globalWordIndex: Int = wordIndex
)

/**
 * Represents a line of text in the native PDF text layer containing one or more words.
 */
data class PdfLineBlock(
    val text: String,
    val pageNumber: Int,
    val bounds: PdfRect,
    val lineIndex: Int,
    val words: List<PdfWordBlock>
)

/**
 * Represents a paragraph / structural block in the native PDF text layer.
 */
data class PdfTextBlock(
    val text: String,
    val pageNumber: Int,
    val bounds: PdfRect,
    val lines: List<PdfLineBlock>
)

/**
 * Represents all native text geometry and content extracted for a single PDF page.
 */
data class PdfPageText(
    val pageNumber: Int,
    val pageWidth: Float,
    val pageHeight: Float,
    val blocks: List<PdfTextBlock>,
    val isImageOnly: Boolean = false
) {
    /** Flattened list of all words on this page in reading order */
    val allWords: List<PdfWordBlock> by lazy {
        blocks.flatMap { block ->
            block.lines.flatMap { line -> line.words }
        }
    }

    /** Flattened list of all lines on this page in reading order */
    val allLines: List<PdfLineBlock> by lazy {
        blocks.flatMap { it.lines }
    }

    /** Full text of the page */
    val fullText: String by lazy {
        blocks.joinToString("\n\n") { it.text }
    }
}

/**
 * Represents an active user text selection on a PDF page.
 */
data class PdfSelection(
    val pageNumber: Int,
    val selectedText: String,
    val startWordIndex: Int,
    val endWordIndex: Int,
    val highlightRects: List<PdfRect>,
    val chapterId: String = "",
    val chapterTitle: String = "",
    val bookTitle: String = "",
    val subject: String = ""
)
