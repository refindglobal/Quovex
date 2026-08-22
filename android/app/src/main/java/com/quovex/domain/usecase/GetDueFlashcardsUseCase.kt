package com.quovex.domain.usecase

import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns only flashcards whose nextReviewDate <= currentTimeMillis.
 * This is the default study mode — only due cards are presented.
 *
 * SM-2 scheduling determines which cards are due via the nextReviewDate field.
 * Cards are ordered by nextReviewDate ASC (most overdue first).
 */
class GetDueFlashcardsUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    operator fun invoke(deckId: Long, currentTimeMillis: Long = System.currentTimeMillis()): Flow<List<FlashcardItem>> {
        return repository.getDueFlashcards(deckId, currentTimeMillis)
    }
}
