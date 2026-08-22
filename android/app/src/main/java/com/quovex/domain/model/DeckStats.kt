package com.quovex.domain.model

/**
 * Aggregated statistics for a single deck.
 * Computed in the data layer from Room projections — no entity leakage into domain.
 *
 * @param masteredCards Cards with repetitions >= 2, meaning they have been successfully
 *   recalled at least twice and have begun interval spacing.
 * @param learningCards Cards that have been seen but are not yet mastered.
 * @param dueCards Cards whose nextReviewDate <= currentTimeMillis.
 */
data class DeckStats(
    val deckId: Int,
    val title: String,
    val subject: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val learningCards: Int,
    val xpValue: Int = 100
) {
    /** Mastery percentage (0–100), based on mastered cards vs total. */
    val masteryPercent: Int
        get() = if (totalCards > 0) ((masteredCards * 100) / totalCards).coerceIn(0, 100) else 0

    /** True when no cards are due for review. */
    val isAllCaughtUp: Boolean
        get() = dueCards == 0 && totalCards > 0

    /** True when the deck has no cards at all. */
    val isEmpty: Boolean
        get() = totalCards == 0
}
