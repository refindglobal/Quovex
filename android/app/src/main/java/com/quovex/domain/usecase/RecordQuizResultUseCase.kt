package com.quovex.domain.usecase

import com.quovex.domain.model.QuizResult
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class RecordQuizResultUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(result: QuizResult, deckId: Int? = null): Long {
        val resultId = repository.recordQuizResult(result)
        if (deckId != null && result.mistakes.isNotEmpty()) {
            result.mistakes.forEach { mistake ->
                repository.createRemedialFlashcard(mistake, deckId)
            }
        }
        return resultId
    }
}
