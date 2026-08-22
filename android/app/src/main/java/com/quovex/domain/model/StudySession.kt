package com.quovex.domain.model

/**
 * Transient state for a single flashcard study session.
 * Stored in ViewModel only — never persisted to Room.
 */
data class StudySession(
    val totalCards: Int = 0,
    val reviewedCount: Int = 0,
    val againCount: Int = 0,
    val hardCount: Int = 0,
    val goodCount: Int = 0,
    val easyCount: Int = 0
) {
    /** Quality breakdown for display on the completion screen. */
    val accuracyPercent: Int
        get() = if (reviewedCount > 0) {
            (((goodCount + easyCount) * 100) / reviewedCount).coerceIn(0, 100)
        } else 0

    fun recordReview(quality: Int): StudySession = copy(
        reviewedCount = reviewedCount + 1,
        againCount = if (quality == 0) againCount + 1 else againCount,
        hardCount = if (quality == 3) hardCount + 1 else hardCount,
        goodCount = if (quality == 4) goodCount + 1 else goodCount,
        easyCount = if (quality == 5) easyCount + 1 else easyCount
    )
}
