package com.quovex.domain.repository

import com.quovex.domain.model.ExtractedContent

/**
 * Contract for extracting readable study text and lecture transcripts from external URLs.
 */
interface ContentExtractionRepository {

    /**
     * Downloads and parses clean readable text from a web article or educational webpage.
     */
    suspend fun extractWebArticle(url: String): Result<ExtractedContent>

    /**
     * Parses a YouTube lecture URL, extracts video title, channel, and transcript/captions.
     */
    suspend fun extractYouTubeVideo(url: String): Result<ExtractedContent>
}
