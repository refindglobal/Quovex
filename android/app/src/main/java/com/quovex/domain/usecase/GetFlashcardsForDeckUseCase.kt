package com.quovex.domain.usecase

import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns ALL flashcards for a given deck (used by Review All mode).
 * For due-cards-only mode, use [GetDueFlashcardsUseCase].
 */
class GetFlashcardsForDeckUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    operator fun invoke(deckId: Long): Flow<List<FlashcardItem>> {
        return repository.getAllFlashcardsForDeck(deckId)
    }
}
