package com.quovex.domain.model

/**
 * Result data extracted from an external web article or YouTube lecture video.
 */
data class ExtractedContent(
    val title: String,
    val content: String,
    val inputType: NoteInputType,
    val sourceUrl: String,
    val authorOrChannel: String? = null,
    val durationSeconds: Int? = null
)
