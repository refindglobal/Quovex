package com.quovex.data.repository

import android.content.Context
import com.quovex.domain.model.PdfLineBlock
import com.quovex.domain.model.PdfPageText
import com.quovex.domain.model.PdfRect
import com.quovex.domain.model.PdfTextBlock
import com.quovex.domain.model.PdfWordBlock
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import com.tom_roush.pdfbox.text.TextPosition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Native PDF text layer extractor using PDFBox Android.
 *
 * Extracts exact word, line, and block positions in PDF document coordinates.
 * Operates purely on the PDF document text layer — NO OCR.
 */
@Singleton
open class PdfTextExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Secondary constructor for testing without Android Context */
    constructor() : this(android.app.Application())

    private var isInitialized = false

    private val pageCache = android.util.LruCache<String, PdfPageText>(30)

    private fun ensureInitialized() {
        if (!isInitialized && context != null) {
            try {
                PDFBoxResourceLoader.init(context)
                isInitialized = true
            } catch (e: Exception) {
                android.util.Log.e("PdfTextExtractor", "PDFBoxResourceLoader init error: ${e.message}")
            }
        }
    }

    open suspend fun extractPageText(pdfFile: File, pageIndex: Int): Result<PdfPageText> =
        withContext(Dispatchers.IO) {
            try {
                val cacheKey = "_"
                pageCache.get(cacheKey)?.let { return@withContext Result.success(it) }

                ensureInitialized()

                PDDocument.load(pdfFile).use { document ->
                    if (pageIndex < 0 || pageIndex >= document.numberOfPages) {
                        return@withContext Result.failure(IllegalArgumentException("Invalid page index: "))
                    }

                    val page = document.getPage(pageIndex)
                    val mediaBox = page.mediaBox ?: page.cropBox
                    val pageWidth = mediaBox?.width ?: 595f
                    val pageHeight = mediaBox?.height ?: 842f

                    val positionalStripper = PositionalTextStripper(pageIndex + 1, pageHeight)
                    positionalStripper.startPage = pageIndex + 1
                    positionalStripper.endPage = pageIndex + 1
                    positionalStripper.getText(document)

                    val pageText = positionalStripper.buildPageText(pageIndex + 1, pageWidth, pageHeight)
                    pageCache.put(cacheKey, pageText)
                    Result.success(pageText)
                }
            } catch (e: Exception) {
                android.util.Log.e("PdfTextExtractor", "Failed to extract native text for page ", e)
                Result.failure(e)
            }
        }

    private class PositionalTextStripper(
        private val pageNum: Int,
        private val pageHeight: Float
    ) : PDFTextStripper() {

        private val recordedCharacters = mutableListOf<CharInfo>()

        private data class CharInfo(
            val char: String,
            val x: Float,
            val y: Float,
            val width: Float,
            val height: Float
        )

        init {
            sortByPosition = true
        }

        override fun processTextPosition(text: TextPosition) {
            val unicode = text.unicode
            if (!unicode.isNullOrBlank() && unicode != "\u0000") {
                val x = text.xDirAdj
                val y = text.yDirAdj
                val width = text.widthDirAdj
                val height = text.heightDir

                recordedCharacters.add(
                    CharInfo(
                        char = unicode,
                        x = x,
                        y = y,
                        width = width,
                        height = height
                    )
                )
            }
            super.processTextPosition(text)
        }

        fun buildPageText(pageNumber: Int, pageWidth: Float, pageHeight: Float): PdfPageText {
            if (recordedCharacters.isEmpty()) {
                return PdfPageText(
                    pageNumber = pageNumber,
                    pageWidth = pageWidth,
                    pageHeight = pageHeight,
                    blocks = emptyList(),
                    isImageOnly = true
                )
            }

            // Group characters into lines based on vertical proximity (y delta <= ~5 points)
            val lines = mutableListOf<MutableList<CharInfo>>()
            var currentLine = mutableListOf<CharInfo>()
            var currentLineY = -1f

            for (c in recordedCharacters) {
                if (currentLine.isEmpty()) {
                    currentLine.add(c)
                    currentLineY = c.y
                } else {
                    val yDiff = abs(c.y - currentLineY)
                    if (yDiff <= 5.0f) {
                        currentLine.add(c)
                    } else {
                        lines.add(currentLine)
                        currentLine = mutableListOf(c)
                        currentLineY = c.y
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine)
            }

            // Build PdfLineBlocks and PdfWordBlocks
            val lineBlocks = mutableListOf<PdfLineBlock>()
            var globalWordIdx = 0

            for ((lineIdx, charList) in lines.withIndex()) {
                val sortedChars = charList.sortedBy { it.x }
                val wordsOnLine = mutableListOf<PdfWordBlock>()

                var currentWordChars = mutableListOf<CharInfo>()
                for (c in sortedChars) {
                    if (currentWordChars.isEmpty()) {
                        currentWordChars.add(c)
                    } else {
                        val prev = currentWordChars.last()
                        val spaceX = c.x - (prev.x + prev.width)
                        if (spaceX > 2.0f) {
                            val wordText = currentWordChars.joinToString("") { it.char }
                            val wLeft = currentWordChars.minOf { it.x }
                            val wTop = currentWordChars.minOf { it.y }
                            val wRight = currentWordChars.maxOf { it.x + it.width }
                            val wBottom = currentWordChars.maxOf { it.y + it.height }

                            wordsOnLine.add(
                                PdfWordBlock(
                                    text = wordText,
                                    pageNumber = pageNumber,
                                    bounds = PdfRect(wLeft, wTop, wRight, wBottom),
                                    lineIndex = lineIdx,
                                    wordIndex = wordsOnLine.size,
                                    globalWordIndex = globalWordIdx++
                                )
                            )
                            currentWordChars = mutableListOf(c)
                        } else {
                            currentWordChars.add(c)
                        }
                    }
                }

                if (currentWordChars.isNotEmpty()) {
                    val wordText = currentWordChars.joinToString("") { it.char }
                    val wLeft = currentWordChars.minOf { it.x }
                    val wTop = currentWordChars.minOf { it.y }
                    val wRight = currentWordChars.maxOf { it.x + it.width }
                    val wBottom = currentWordChars.maxOf { it.y + it.height }

                    wordsOnLine.add(
                        PdfWordBlock(
                            text = wordText,
                            pageNumber = pageNumber,
                            bounds = PdfRect(wLeft, wTop, wRight, wBottom),
                            lineIndex = lineIdx,
                            wordIndex = wordsOnLine.size,
                            globalWordIndex = globalWordIdx++
                        )
                    )
                }

                if (wordsOnLine.isNotEmpty()) {
                    val lLeft = wordsOnLine.minOf { it.bounds.left }
                    val lTop = wordsOnLine.minOf { it.bounds.top }
                    val lRight = wordsOnLine.maxOf { it.bounds.right }
                    val lBottom = wordsOnLine.maxOf { it.bounds.bottom }
                    val fullLineText = wordsOnLine.joinToString(" ") { it.text }

                    lineBlocks.add(
                        PdfLineBlock(
                            text = fullLineText,
                            pageNumber = pageNumber,
                            bounds = PdfRect(lLeft, lTop, lRight, lBottom),
                            lineIndex = lineIdx,
                            words = wordsOnLine
                        )
                    )
                }
            }

            val textBlocks = if (lineBlocks.isNotEmpty()) {
                val bLeft = lineBlocks.minOf { it.bounds.left }
                val bTop = lineBlocks.minOf { it.bounds.top }
                val bRight = lineBlocks.maxOf { it.bounds.right }
                val bBottom = lineBlocks.maxOf { it.bounds.bottom }
                val bText = lineBlocks.joinToString("\n") { it.text }
                listOf(
                    PdfTextBlock(
                        text = bText,
                        pageNumber = pageNumber,
                        bounds = PdfRect(bLeft, bTop, bRight, bBottom),
                        lines = lineBlocks
                    )
                )
            } else {
                emptyList()
            }

            return PdfPageText(
                pageNumber = pageNumber,
                pageWidth = pageWidth,
                pageHeight = pageHeight,
                blocks = textBlocks,
                isImageOnly = textBlocks.isEmpty()
            )
        }
    }
}
