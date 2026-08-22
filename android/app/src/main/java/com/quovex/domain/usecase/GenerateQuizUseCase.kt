package com.quovex.domain.usecase

import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class GenerateQuizUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val quovexRepository: QuovexRepository
) {
    suspend operator fun invoke(
        materialId: Long,
        subject: String,
        topic: String,
        difficulty: String = "Medium",
        keyPoints: List<String> = emptyList()
    ): Result<List<QuizQuestion>> {
        val result = aiRepository.generateQuiz(subject, topic, difficulty, keyPoints)
        if (result.isSuccess) {
            val questions = result.getOrThrow().map { it.copy(materialId = materialId) }
            quovexRepository.saveQuizQuestions(questions)
            return Result.success(questions)
        }
        return result
    }
}
