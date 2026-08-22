package com.quovex.domain.usecase

import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class ReviewCardUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    /**
     * Processes a card review using the SM-2 algorithm and persists the result.
     *
     * @param cardId The ID of the flashcard to review.
     * @param quality SM-2 rating:
     *   0 = Again (failed recall — resets repetitions)
     *   3 = Hard (difficult recall — intervals stay conservative)
     *   4 = Good (correct recall with slight hesitation)
     *   5 = Easy (instant, perfect recall)
     * @return The updated [FlashcardItem] with new SM-2 scheduling state, or null if card not found.
     */
    suspend operator fun invoke(cardId: Long, quality: Int): FlashcardItem? {
        return repository.processCardReview(cardId, quality)
    }
}
