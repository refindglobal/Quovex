package com.quovex.domain.usecase

import com.quovex.domain.model.DeckStats
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns aggregated [DeckStats] for ALL decks using a single SQL query.
 * Backed by a LEFT JOIN + conditional aggregation — no N+1 queries.
 */
class GetDeckStatsForAllDecksUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    operator fun invoke(currentTimeMillis: Long = System.currentTimeMillis()): Flow<List<DeckStats>> {
        return repository.getDeckStatsForAllDecks(currentTimeMillis)
    }
}
