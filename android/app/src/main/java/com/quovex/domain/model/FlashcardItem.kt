package com.quovex.domain.model

/**
 * Pure Kotlin domain model for a flashcard.
 * Must never import or depend on Room entity types.
 */
data class FlashcardItem(
    val id: Int,
    val deckId: Int,
    val frontContent: String,
    val backContent: String,
    // SuperMemo-2 scheduling fields
    val easeFactor: Float = 2.5f,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis()
) {
    /** True when this card's scheduled review date is in the past or now. */
    fun isDue(currentTimeMillis: Long = System.currentTimeMillis()): Boolean =
        nextReviewDate <= currentTimeMillis
}
