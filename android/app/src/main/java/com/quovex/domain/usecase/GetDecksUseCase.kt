package com.quovex.domain.usecase

import com.quovex.domain.model.DeckItem
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDecksUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    operator fun invoke(): Flow<List<DeckItem>> {
        return repository.getDecks()
    }
}
