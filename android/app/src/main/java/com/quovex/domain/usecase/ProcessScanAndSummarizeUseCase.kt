package com.quovex.domain.usecase

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.domain.model.SubjectInference
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

data class ScanProcessingResult(
    val summaryResult: AiSummaryResult,
    val inference: SubjectInference
)

class ProcessScanAndSummarizeUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(rawOcrText: String, subjectHint: String = "General"): Result<ScanProcessingResult> {
        val summaryResult = aiRepository.summarizeNote(rawOcrText, subjectHint)
        if (summaryResult.isFailure) {
            return Result.failure(summaryResult.exceptionOrNull() ?: Exception("Summarization failed"))
        }

        val inferenceResult = aiRepository.classifyMaterial(rawOcrText.take(2000))
        val inference = inferenceResult.getOrDefault(
            SubjectInference(
                subject = subjectHint,
                topic = "",
                confidence = 0.5f
            )
        )

        return Result.success(
            ScanProcessingResult(
                summaryResult = summaryResult.getOrThrow(),
                inference = inference
            )
        )
    }
}
