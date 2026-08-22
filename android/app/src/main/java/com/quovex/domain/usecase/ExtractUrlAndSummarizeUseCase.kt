package com.quovex.domain.usecase

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

data class UrlSummarizeResult(
    val title: String,
    val extractedText: String,
    val summaryResult: AiSummaryResult,
    val inputType: NoteInputType
)

class ExtractUrlAndSummarizeUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        url: String,
        subject: String = "General"
    ): Result<UrlSummarizeResult> {
        val trimmedUrl = url.trim()
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            return Result.failure(IllegalArgumentException("Please enter a valid URL starting with http:// or https://"))
        }

        val isYouTube = trimmedUrl.contains("youtube.com", ignoreCase = true) ||
                trimmedUrl.contains("youtu.be", ignoreCase = true)

        val inputType = if (isYouTube) NoteInputType.YOUTUBE else NoteInputType.URL

        // 1. Extract content via backend proxy
        val extractResult = aiRepository.extractUrlContent(trimmedUrl)
        if (extractResult.isFailure) {
            return Result.failure(extractResult.exceptionOrNull() ?: Exception("Failed to extract content from URL"))
        }

        val (title, textBody) = extractResult.getOrThrow()

        // 2. Summarize via AI Gateway
        val summarizeResult = aiRepository.summarizeNote(
            rawText = textBody,
            subject = subject
        )

        if (summarizeResult.isFailure) {
            return Result.failure(summarizeResult.exceptionOrNull() ?: Exception("Failed to generate AI summary for URL"))
        }

        val summaryData = summarizeResult.getOrThrow()

        return Result.success(
            UrlSummarizeResult(
                title = if (title.isNotBlank() && title != trimmedUrl) title else "Web Note: $subject",
                extractedText = textBody,
                summaryResult = summaryData,
                inputType = inputType
            )
        )
    }
}
