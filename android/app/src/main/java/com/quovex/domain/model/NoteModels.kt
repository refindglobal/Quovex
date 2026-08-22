package com.quovex.domain.model

/**
 * Supported input types for notes in Quovex.
 */
enum class NoteInputType {
    TEXT,
    PDF,
    URL,
    YOUTUBE,
    SCAN
}

/**
 * Processing state lifecycle for notes.
 */
enum class NoteProcessingStatus {
    DRAFT,
    UPLOADING,
    PROCESSING,
    READY,
    FAILED
}

/**
 * Pure Kotlin domain model for a study note.
 * Zero Android or Room or Firebase SDK dependencies.
 */
data class NoteItem(
    val id: Long = 0,
    val cloudId: String? = null,
    val title: String,
    val subject: String,
    val content: String,
    val status: NoteProcessingStatus = NoteProcessingStatus.READY,
    val inputType: NoteInputType = NoteInputType.TEXT,
    val sourceUrl: String? = null,
    val storageRef: String? = null,
    val keyPoints: List<String> = emptyList(),
    val flashcardCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
