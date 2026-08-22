package com.quovex.domain.usecase

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

/**
 * Requests AI to summarize raw text into structured output (summary, keyPoints, flashcards).
 * This is a pure AI operation — it does NOT write to any repository.
 * Use [GenerateFlashcardsFromNoteUseCase] for the full note→flashcard pipeline.
 */
class SummarizeNoteUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        rawText: String,
        subject: String = "Physics"
    ): Result<AiSummaryResult> {
        return aiRepository.summarizeNote(rawText, subject)
    }
}
