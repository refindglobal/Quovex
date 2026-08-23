package com.quovex.data.batch

import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.model.ScannedDocumentOrganization
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles batch splitting and sequential submission of page images for document analysis.
 *
 * Why batching:
 * A 10–30 page handwritten notebook must NOT trigger 30 independent full AI requests.
 * Pages are chunked into batches of [maxPagesPerRequest] to balance:
 *   - Context window limits per AI model call
 *   - API cost per image
 *   - Latency vs. accuracy tradeoffs
 *
 * Batching strategy:
 *   Pages 1–5  → batch 0 → partial analysis
 *   Pages 6–10 → batch 1 → partial analysis
 *   ...
 *   Final batch → synthesis → full ScannedDocumentOrganization
 *
 * Configurable:
 * [maxPagesPerRequest] can be changed based on backend/model limits, cost,
 * and performance requirements. It is NOT a permanent hardcoded business rule.
 * Current initial value: 5 (matches ai/document/analyze contract).
 *
 * Shared infrastructure used by [AnalyzeDocumentImagesUseCase] only.
 * NOT used by SolveImageDoubtUseCase (single-image, different endpoint).
 */
@Singleton
class DocumentAnalysisBatcher @Inject constructor(
    private val aiRepository: AIRepository
) {

    /**
     * Maximum pages per API batch request.
     * Change this value to tune cost vs. context window vs. accuracy.
     * Backend gateway must support [maxPagesPerRequest] images per call.
     */
    var maxPagesPerRequest: Int = DEFAULT_BATCH_SIZE
        set(value) {
            require(value in 1..MAX_ALLOWED_BATCH_SIZE) {
                "maxPagesPerRequest must be between 1 and $MAX_ALLOWED_BATCH_SIZE"
            }
            field = value
        }

    /**
     * Submit [pageImages] in batches of [maxPagesPerRequest] to the document analysis API.
     *
     * @param pageImages Ordered list of compressed page images (ByteArray, domain-safe)
     * @param subjectHint Optional user-provided subject hint to guide AI inference
     * @return Full [ScannedDocumentOrganization], or failure
     */
    suspend fun analyze(
        pageImages: List<DomainImageInput>,
        subjectHint: String = ""
    ): Result<ScannedDocumentOrganization> {
        return aiRepository.analyzeDocumentImages(
            pageImages = pageImages,
            subjectHint = subjectHint
        )
    }

    companion object {
        /** Initial batch size — 5 pages per request */
        const val DEFAULT_BATCH_SIZE = 5

        /** Safety upper limit — prevent accidental misconfiguration */
        const val MAX_ALLOWED_BATCH_SIZE = 20
    }
}
