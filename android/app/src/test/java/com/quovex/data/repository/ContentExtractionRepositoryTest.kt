package com.quovex.data.repository

import io.mockk.mockk
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ContentExtractionRepositoryTest {

    private lateinit var repository: ContentExtractionRepositoryImpl
    private val mockOkHttpClient = mockk<OkHttpClient>(relaxed = true)

    @Before
    fun setup() {
        repository = ContentExtractionRepositoryImpl(mockOkHttpClient)
    }

    @Test
    fun parseYouTubeVideoId_extractsFromVariousUrlFormats() {
        val urls = mapOf(
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/shorts/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "https://www.youtube.com/embed/dQw4w9WgXcQ" to "dQw4w9WgXcQ",
            "http://m.youtube.com/watch?v=dQw4w9WgXcQ&feature=share" to "dQw4w9WgXcQ"
        )

        for ((url, expectedId) in urls) {
            val extracted = repository.parseYouTubeVideoId(url)
            assertEquals("Failed for URL: $url", expectedId, extracted)
        }
    }

    @Test
    fun extractHtmlTitle_extractsAndDecodesCorrectly() {
        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Newton&#39;s Laws of Motion &amp; Applications &mdash; Physics</title>
            </head>
            <body>
                <p>Hello world</p>
            </body>
            </html>
        """.trimIndent()

        val title = repository.extractHtmlTitle(html)
        assertEquals("Newton's Laws of Motion & Applications — Physics", title)
    }

    @Test
    fun sanitizeHtmlToPlainText_stripsNonContentTagsAndDecodesEntities() {
        val html = """
            <html>
            <head><title>Test</title><style>.hidden { display: none; }</style></head>
            <body>
                <header><nav>Home &gt; Physics</nav></header>
                <article>
                    <h1>Newton's First Law</h1>
                    <p>An object remains at <b>rest</b>, or in <i>uniform motion</i> in a straight line, unless acted upon by a force.</p>
                    <script>console.log('tracking');</script>
                    <p>Formula: F &equals; ma &amp; p &equals; mv</p>
                </article>
                <footer>&copy; 2026 Quovex. All rights reserved.</footer>
            </body>
            </html>
        """.trimIndent()

        val cleaned = repository.sanitizeHtmlToPlainText(html)
        assertTrue(cleaned.contains("Newton's First Law"))
        assertTrue(cleaned.contains("An object remains at rest"))
        assertTrue(!cleaned.contains("console.log"))
        assertTrue(!cleaned.contains("display: none"))
    }

    @Test
    fun parseTimedTextXml_formatsTimestampsAndGroupsSpeech() {
        val xml = """
            <?xml version="1.0" encoding="utf-8" ?>
            <transcript>
                <text start="0.0" dur="2.5">Welcome to today&apos;s physics lecture.</text>
                <text start="2.6" dur="3.0">Today we will discuss thermodynamics and entropy.</text>
                <text start="60.0" dur="4.0">The second law states that entropy never decreases.</text>
            </transcript>
        """.trimIndent()

        val parsed = repository.parseTimedTextXml(xml)
        assertNotNull(parsed)
        assertTrue(parsed.contains("`[00:00]`"))
        assertTrue(parsed.contains("Welcome to today's physics lecture."))
        assertTrue(parsed.contains("`[01:00]`"))
        assertTrue(parsed.contains("The second law states that entropy never decreases."))
    }
}
