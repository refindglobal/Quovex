package com.quovex.domain.usecase

import com.quovex.domain.model.DeckStats
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

/**
 * Returns aggregated [DeckStats] for a single deck.
 * Used by the Deck Overview screen before the user starts studying.
 */
class GetDeckStatsUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(deckId: Int, currentTimeMillis: Long = System.currentTimeMillis()): DeckStats? {
        return repository.getDeckStats(deckId, currentTimeMillis)
    }
}
