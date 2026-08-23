package com.quovex.ui.ncert

import androidx.compose.ui.geometry.Offset
import com.quovex.domain.model.PdfLineBlock
import com.quovex.domain.model.PdfPageText
import com.quovex.domain.model.PdfRect
import com.quovex.domain.model.PdfTextBlock
import com.quovex.domain.model.PdfWordBlock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfCoordinateMapperTest {

    private fun createSamplePageText(): PdfPageText {
        // Line 0: "Electric Charges and Fields"
        val word0 = PdfWordBlock("Electric", 1, PdfRect(50f, 100f, 100f, 120f), 0, 0, 0)
        val word1 = PdfWordBlock("Charges", 1, PdfRect(110f, 100f, 170f, 120f), 0, 1, 1)
        val word2 = PdfWordBlock("and", 1, PdfRect(180f, 100f, 210f, 120f), 0, 2, 2)
        val word3 = PdfWordBlock("Fields", 1, PdfRect(220f, 100f, 270f, 120f), 0, 3, 3)
        val line0 = PdfLineBlock("Electric Charges and Fields", 1, PdfRect(50f, 100f, 270f, 120f), 0, listOf(word0, word1, word2, word3))

        // Line 1: "Like charges repel"
        val word4 = PdfWordBlock("Like", 1, PdfRect(50f, 140f, 80f, 160f), 1, 0, 4)
        val word5 = PdfWordBlock("charges", 1, PdfRect(90f, 140f, 150f, 160f), 1, 1, 5)
        val word6 = PdfWordBlock("repel", 1, PdfRect(160f, 140f, 200f, 160f), 1, 2, 6)
        val line1 = PdfLineBlock("Like charges repel", 1, PdfRect(50f, 140f, 200f, 160f), 1, listOf(word4, word5, word6))

        val block = PdfTextBlock(
            text = "Electric Charges and Fields\nLike charges repel",
            pageNumber = 1,
            bounds = PdfRect(50f, 100f, 270f, 160f),
            lines = listOf(line0, line1)
        )

        return PdfPageText(
            pageNumber = 1,
            pageWidth = 595f,
            pageHeight = 842f,
            blocks = listOf(block),
            isImageOnly = false
        )
    }

    @Test
    fun mapPdfRectToScreen_zoom1x_scalesCorrectly() {
        val pdfRect = PdfRect(100f, 200f, 300f, 400f)
        val screenRect = PdfCoordinateMapper.mapPdfRectToScreen(
            pdfRect = pdfRect,
            pdfPageWidth = 595f,
            pdfPageHeight = 842f,
            renderedPageWidth = 595f,
            renderedPageHeight = 842f,
            zoom = 1.0f
        )

        assertEquals(100f, screenRect.left, 0.01f)
        assertEquals(200f, screenRect.top, 0.01f)
        assertEquals(300f, screenRect.right, 0.01f)
        assertEquals(400f, screenRect.bottom, 0.01f)
    }

    @Test
    fun mapPdfRectToScreen_zoom2x_doublesDimensions() {
        val pdfRect = PdfRect(100f, 200f, 300f, 400f)
        val screenRect = PdfCoordinateMapper.mapPdfRectToScreen(
            pdfRect = pdfRect,
            pdfPageWidth = 500f,
            pdfPageHeight = 1000f,
            renderedPageWidth = 500f,
            renderedPageHeight = 1000f,
            zoom = 2.0f
        )

        assertEquals(200f, screenRect.left, 0.01f)
        assertEquals(400f, screenRect.top, 0.01f)
        assertEquals(600f, screenRect.right, 0.01f)
        assertEquals(800f, screenRect.bottom, 0.01f)
    }

    @Test
    fun mapPdfRectToScreen_withPanOffset_appliesTranslation() {
        val pdfRect = PdfRect(10f, 20f, 50f, 60f)
        val screenRect = PdfCoordinateMapper.mapPdfRectToScreen(
            pdfRect = pdfRect,
            pdfPageWidth = 100f,
            pdfPageHeight = 200f,
            renderedPageWidth = 100f,
            renderedPageHeight = 200f,
            zoom = 1.0f,
            panX = 25f,
            panY = 50f
        )

        assertEquals(35f, screenRect.left, 0.01f)
        assertEquals(70f, screenRect.top, 0.01f)
    }

    @Test
    fun mapScreenPointToPdf_invertsAccurately() {
        val originalPdf = Offset(150f, 300f)
        val screenRect = PdfCoordinateMapper.mapPdfRectToScreen(
            pdfRect = PdfRect(originalPdf.x, originalPdf.y, originalPdf.x + 10f, originalPdf.y + 10f),
            pdfPageWidth = 595f,
            pdfPageHeight = 842f,
            renderedPageWidth = 1080f,
            renderedPageHeight = 1920f,
            zoom = 1.75f,
            panX = -50f,
            panY = 120f
        )

        val reconstructedPdf = PdfCoordinateMapper.mapScreenPointToPdf(
            screenPoint = Offset(screenRect.left, screenRect.top),
            pdfPageWidth = 595f,
            pdfPageHeight = 842f,
            renderedPageWidth = 1080f,
            renderedPageHeight = 1920f,
            zoom = 1.75f,
            panX = -50f,
            panY = 120f
        )

        assertEquals(originalPdf.x, reconstructedPdf.x, 0.1f)
        assertEquals(originalPdf.y, reconstructedPdf.y, 0.1f)
    }

    @Test
    fun findWordAtPoint_directHit_findsCorrectWord() {
        val pageText = createSamplePageText()
        val hit = PdfCoordinateMapper.findWordAtPoint(Offset(130f, 110f), pageText)

        assertNotNull(hit)
        assertEquals("Charges", hit?.text)
        assertEquals(1, hit?.globalWordIndex)
    }

    @Test
    fun findWordAtPoint_nearWord_findsWithinTolerance() {
        val pageText = createSamplePageText()
        // word0 is [50, 100, 100, 120], point at (46, 110) is within 12pt tolerance
        val hit = PdfCoordinateMapper.findWordAtPoint(Offset(46f, 110f), pageText, touchToleranceDp = 10f)

        assertNotNull(hit)
        assertEquals("Electric", hit?.text)
    }

    @Test
    fun findWordAtPoint_farAway_returnsNull() {
        val pageText = createSamplePageText()
        val hit = PdfCoordinateMapper.findWordAtPoint(Offset(500f, 700f), pageText)
        assertNull(hit)
    }

    @Test
    fun createSelection_singleWord_returnsSingleWordSelection() {
        val pageText = createSamplePageText()
        val sel = PdfCoordinateMapper.createSelection(pageText, 0, 0)

        assertNotNull(sel)
        assertEquals("Electric", sel?.selectedText)
        assertEquals(0, sel?.startWordIndex)
        assertEquals(0, sel?.endWordIndex)
        assertEquals(1, sel?.highlightRects?.size)
    }

    @Test
    fun createSelection_multiline_reconstructsNaturalSpacing() {
        val pageText = createSamplePageText()
        // Select word 2 ("and") on line 0 through word 5 ("charges") on line 1
        val sel = PdfCoordinateMapper.createSelection(pageText, 2, 5)

        assertNotNull(sel)
        assertEquals("and Fields Like charges", sel?.selectedText)
        assertEquals(2, sel?.startWordIndex)
        assertEquals(5, sel?.endWordIndex)
        assertEquals(2, sel?.highlightRects?.size) // 2 distinct lines
    }

    @Test
    fun createSelection_reversedIndices_normalizesOrder() {
        val pageText = createSamplePageText()
        // Dragging end before start: from index 3 down to 1
        val sel = PdfCoordinateMapper.createSelection(pageText, 3, 1)

        assertNotNull(sel)
        assertEquals("Charges and Fields", sel?.selectedText)
        assertEquals(1, sel?.startWordIndex)
        assertEquals(3, sel?.endWordIndex)
    }

    @Test
    fun createSelection_imageOnlyPage_returnsNull() {
        val imagePage = PdfPageText(1, 595f, 842f, emptyList(), isImageOnly = true)
        val sel = PdfCoordinateMapper.createSelection(imagePage, 0, 0)
        assertNull(sel)
    }
}
