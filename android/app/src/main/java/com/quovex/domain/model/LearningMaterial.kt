package com.quovex.domain.model

data class FormulaItem(
    val name: String,
    val latex: String,
    val description: String = ""
)

/**
 * Pure Kotlin domain model for a Learning Material in Quovex.
 * A Learning Material is a structured study asset built from any source (scan, PDF, YouTube, web, quick note).
 */
data class LearningMaterial(
    val id: Long = 0,
    val cloudId: String? = null,
    val title: String,
    val subject: String,
    val topic: String = "",
    val subtopic: String = "",
    val summary: String = "",
    val keyPoints: List<String> = emptyList(),
    val formulas: List<FormulaItem> = emptyList(),
    val inputType: NoteInputType = NoteInputType.TEXT,
    val status: NoteProcessingStatus = NoteProcessingStatus.READY,
    val sourceUrl: String? = null,
    val storageRef: String? = null,
    val flashcardDeckId: Int? = null,
    val flashcardCount: Int = 0,
    val quizGenerated: Boolean = false,
    val inferredSubject: String? = null,
    val inferredTopic: String? = null,
    val inferredConfidence: Float = 0f,
    val syncStatus: String = "PENDING_SYNC",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Checks if this material lacks core derived AI fields (e.g. imported from legacy v2 notes)
     * and requires processing before being displayed as fully transformed material.
     */
    val needsProcessing: Boolean
        get() = summary.isBlank() && keyPoints.isEmpty() && formulas.isEmpty()
}
