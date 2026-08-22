package com.quovex.domain.usecase

import com.quovex.domain.model.QuizMistake
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class CreateRemedialFlashcardsUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(mistakes: List<QuizMistake>, deckId: Int): List<Long> {
        return mistakes.map { mistake ->
            repository.createRemedialFlashcard(mistake, deckId)
        }
    }
}
