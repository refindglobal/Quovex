package com.quovex.domain.usecase

import com.quovex.domain.model.QuizResult
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class RecordQuizResultUseCase @Inject constructor(
    private val repository: QuovexRepository,
    private val awardXpUseCase: AwardXpUseCase
) {
    suspend operator fun invoke(result: QuizResult, deckId: Int? = null): Long {
        val resultId = repository.recordQuizResult(result)
        if (deckId != null && result.mistakes.isNotEmpty()) {
            result.mistakes.forEach { mistake ->
                repository.createRemedialFlashcard(mistake, deckId)
            }
        }

        // Award XP: 10 XP per correct question + 25 XP bonus for 100% accuracy
        val baseScoreXp = (result.score * 10L).coerceAtLeast(0L)
        val bonusXp = if (result.accuracyPercent >= 100f && result.totalQuestions > 0) 25L else 0L
        awardXpUseCase(baseScoreXp + bonusXp)

        return resultId
    }
}
