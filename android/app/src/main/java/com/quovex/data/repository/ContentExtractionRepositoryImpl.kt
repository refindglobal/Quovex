package com.quovex.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.quovex.domain.model.ExtractedContent
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.repository.ContentExtractionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentExtractionRepositoryImpl @Inject constructor(
    private val baseOkHttpClient: OkHttpClient
) : ContentExtractionRepository {

    private val client: OkHttpClient by lazy {
        baseOkHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private val gson = Gson()

    override suspend fun extractWebArticle(url: String): Result<ExtractedContent> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36 QuovexStudyApp/1.0")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to fetch webpage (HTTP ${response.code})"))
                }

                val html = response.body?.string() ?: return@withContext Result.failure(Exception("Empty webpage response"))
                val title = extractHtmlTitle(html).ifBlank { deriveTitleFromUrl(url) }
                val cleanText = sanitizeHtmlToPlainText(html)

                if (cleanText.isBlank() || cleanText.length < 50) {
                    return@withContext Result.failure(Exception("Could not extract readable study text from this webpage. Please try copying and pasting the text."))
                }

                val formattedContent = formatWebArticle(title, cleanText, url)
                val truncatedText = formattedContent.take(35000)

                Result.success(
                    ExtractedContent(
                        title = title,
                        content = truncatedText,
                        inputType = NoteInputType.URL,
                        sourceUrl = url
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun extractYouTubeVideo(url: String): Result<ExtractedContent> = withContext(Dispatchers.IO) {
        try {
            val videoId = parseYouTubeVideoId(url)
                ?: return@withContext Result.failure(IllegalArgumentException("Invalid YouTube URL. Please provide a valid YouTube video or Shorts link."))

            // 1. Fetch YouTube oEmbed metadata (Title, Author)
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val oembedRequest = Request.Builder().url(oembedUrl).build()

            var videoTitle = "YouTube Lecture ($videoId)"
            var authorName = "YouTube Creator"

            try {
                client.newCall(oembedRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val json = gson.fromJson(body, JsonObject::class.java)
                            videoTitle = json.get("title")?.asString ?: videoTitle
                            authorName = json.get("author_name")?.asString ?: authorName
                        }
                    }
                }
            } catch (_: Throwable) {
                // Non-fatal, proceed to transcript
            }

            // 2. Fetch YouTube timed text captions / transcript
            val transcript = fetchYouTubeCaptions(videoId)

            val studyContent = if (!transcript.isNullOrBlank() && transcript.length > 50) {
                """
                # 🎥 $videoTitle
                
                > 👤 **Channel / Instructor**: $authorName  
                > 🔗 **Video URL**: https://www.youtube.com/watch?v=$videoId  
                > 📚 **Type**: Transcribed Video Lecture
                
                ---
                
                ## 📝 Lecture Transcript & Timestamps
                
                $transcript
                """.trimIndent()
            } else {
                """
                # 🎥 $videoTitle
                
                > 👤 **Channel / Instructor**: $authorName  
                > 🔗 **Video URL**: https://www.youtube.com/watch?v=$videoId  
                > ℹ️ **Status**: Direct Captions Unavailable
                
                ---
                
                ## 📚 Study Briefing & Syllabus Context
                
                This educational material was imported from the lecture **"$videoTitle"** by **$authorName**.
                
                Quovex AI will synthesize a complete curriculum study guide, including core concepts, formulas, memory tricks, flashcards, and a practice quiz based on this lecture's topic.
                """.trimIndent()
            }

            Result.success(
                ExtractedContent(
                    title = videoTitle,
                    content = studyContent,
                    inputType = NoteInputType.YOUTUBE,
                    sourceUrl = "https://www.youtube.com/watch?v=$videoId",
                    authorOrChannel = authorName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchYouTubeCaptions(videoId: String): String? {
        val captionUrls = listOf(
            "https://www.youtube.com/api/timedtext?v=$videoId&lang=en",
            "https://www.youtube.com/api/timedtext?v=$videoId&lang=en-US",
            "https://www.youtube.com/api/timedtext?v=$videoId&lang=en&kind=asr"
        )

        for (captionUrl in captionUrls) {
            try {
                val request = Request.Builder().url(captionUrl).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val xml = response.body?.string()
                        if (!xml.isNullOrBlank() && xml.contains("<text")) {
                            val parsed = parseTimedTextXml(xml)
                            if (parsed.isNotBlank()) return parsed
                        }
                    }
                }
            } catch (_: Throwable) {
                // Try next caption URL
            }
        }
        return null
    }

    fun parseTimedTextXml(xml: String): String {
        val pattern = Pattern.compile("<text start=\"([^\"]+)\"[^>]*>([^<]+)</text>")
        val matcher = pattern.matcher(xml)
        val sb = StringBuilder()
        var currentChunkSeconds = -1
        var currentChunkText = StringBuilder()

        while (matcher.find()) {
            val startSecondsFloat = matcher.group(1)?.toFloatOrNull() ?: 0f
            val startSeconds = startSecondsFloat.toInt()
            val raw = matcher.group(2) ?: ""
            val decoded = decodeHtmlEntities(raw).trim()

            if (decoded.isNotBlank()) {
                if (currentChunkSeconds == -1 || startSeconds - currentChunkSeconds >= 45) {
                    if (currentChunkText.isNotBlank()) {
                        val timeFormatted = formatSecondsToTimestamp(currentChunkSeconds)
                        sb.append("`$timeFormatted` ").append(currentChunkText.toString().trim()).append("\n\n")
                        currentChunkText = StringBuilder()
                    }
                    currentChunkSeconds = startSeconds
                }
                currentChunkText.append(decoded).append(" ")
            }
        }

        if (currentChunkText.isNotBlank()) {
            val timeFormatted = formatSecondsToTimestamp(if (currentChunkSeconds >= 0) currentChunkSeconds else 0)
            sb.append("`$timeFormatted` ").append(currentChunkText.toString().trim()).append("\n\n")
        }

        return sb.toString().trim()
    }

    private fun formatSecondsToTimestamp(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(java.util.Locale.US, "[%02d:%02d]", minutes, seconds)
    }

    fun formatWebArticle(title: String, rawContent: String, sourceUrl: String): String {
        return """
        # 📄 $title
        
        > 🌐 **Source**: $sourceUrl  
        > 📖 **Content Type**: Extracted Academic / Web Article
        
        ---
        
        $rawContent
        """.trimIndent()
    }

    fun parseYouTubeVideoId(url: String): String? {
        val pattern = Pattern.compile(
            "(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/(?:watch\\?v=|embed\\/|v\\/|shorts\\/)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
        )
        val matcher = pattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    fun extractHtmlTitle(html: String): String {
        val pattern = Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            val rawTitle = matcher.group(1) ?: ""
            return decodeHtmlEntities(rawTitle).trim()
        }
        return ""
    }

    fun sanitizeHtmlToPlainText(html: String): String {
        var text = html

        // Remove script, style, nav, footer, header, noscript, svg, iframe tags
        val removeTagsPattern = Pattern.compile(
            "<(script|style|nav|footer|header|aside|noscript|svg|iframe)[^>]*?>.*?</\\1>",
            Pattern.CASE_INSENSITIVE or Pattern.DOTALL
        )
        text = removeTagsPattern.matcher(text).replaceAll(" ")

        // Replace common block tags with newlines
        text = text.replace(Regex("(?i)<(br|p|div|h1|h2|h3|h4|h5|h6|li|tr)[^>]*>"), "\n")

        // Strip all other HTML tags
        text = text.replace(Regex("<[^>]+>"), " ")

        // Decode HTML entities
        text = decodeHtmlEntities(text)

        // Normalize multiple whitespaces and newlines
        text = text.replace(Regex("[ \\t]+"), " ")
        text = text.replace(Regex("\\n\\s*\\n\\s*\\n+"), "\n\n")

        return text.trim()
    }

    fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&nbsp;", " ")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&equals;", "=")
            .replace("&copy;", "©")
            .replace("&reg;", "®")
            .replace("&trade;", "™")
            .replace("&bull;", "•")
            .replace("&#8217;", "’")
            .replace("&#8216;", "‘")
            .replace("&#8220;", "“")
            .replace("&#8221;", "”")
            .replace("&#8211;", "–")
            .replace("&#8212;", "—")
    }

    private fun deriveTitleFromUrl(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val path = uri.path.trim('/')
            if (path.isNotBlank()) {
                path.substringAfterLast('/').replace('-', ' ').replace('_', ' ').capitalizeWords()
            } else {
                uri.host ?: "Web Article"
            }
        } catch (_: Exception) {
            "Web Article"
        }
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
