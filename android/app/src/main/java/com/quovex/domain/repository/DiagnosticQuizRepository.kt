package com.quovex.domain.repository

import com.quovex.domain.model.DiagnosticQuestion
import com.quovex.domain.model.DiagnosticQuizRequest
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.RemedialCardSynthesis

/**
 * Domain interface for generating Daily Diagnostic Quizzes and synthesizing Remedial Flashcards.
 */
interface DiagnosticQuizRepository {
    suspend fun generateDailyDiagnosticQuiz(request: DiagnosticQuizRequest): Result<List<DiagnosticQuestion>>

    suspend fun synthesizeRemedialFlashcards(
        mistakes: List<QuizMistake>,
        targetExam: String
    ): Result<List<RemedialCardSynthesis>>
}
