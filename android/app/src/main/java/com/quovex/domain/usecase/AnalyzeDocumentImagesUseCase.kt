package com.quovex.domain.usecase

import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ScannedDocumentOrganization
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

/**
 * Document Intelligence Use Case.
 *
 * COMPLETELY SEPARATE from [SolveImageDoubtUseCase]:
 *
 * [SolveImageDoubtUseCase]:
 *   Single image → identify academic problem → solve → explain → final answer
 *   Uses: ai/doubt/image
 *   Semantics: PROBLEM SOLVING
 *
 * [AnalyzeDocumentImagesUseCase]:
 *   Multi-page document → understand pages → identify sections → classify topics
 *   → group pages → create chapter structure → create subtopics → create notes
 *   Uses: ai/document/analyze
 *   Semantics: DOCUMENT INTELLIGENCE / STRUCTURE UNDERSTANDING
 *
 * Shared infrastructure:
 *   - DomainImageInput (platform-neutral ByteArray wrapper)
 *   - AIRepository gateway (different methods, different endpoints)
 *
 * DOMAIN PURITY:
 *   - No android.graphics.Bitmap in this use case
 *   - Accepts DomainImageInput (ByteArray-based) — callers must convert Bitmap to bytes
 *   - ViewModel layer handles Bitmap → ByteArray → DomainImageInput conversion
 *
 * @param aiRepository Provides [AIRepository.analyzeDocumentImages] with batching support
 */
class AnalyzeDocumentImagesUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {

    /**
     * Analyze a list of compressed page images and return AI-proposed document organization.
     *
     * The caller (ViewModel) is responsible for:
     * 1. Reading page images from file paths (ScannedPage.imageFilePath)
     * 2. Compressing Bitmaps to JPEG ByteArrays (≤ 256KB per page for cost efficiency)
     * 3. Wrapping in DomainImageInput
     *
     * @param pageImages Platform-neutral compressed images in document order
     * @param subjectHint Optional subject to help AI context (e.g. "Physics", "Accountancy")
     *                    Empty = AI infers from visual content — works for all streams
     * @return [ScannedDocumentOrganization] with chapters/subtopics, or failure
     */
    suspend operator fun invoke(
        pageImages: List<DomainImageInput>,
        subjectHint: String = ""
    ): Result<ScannedDocumentOrganization> {
        if (pageImages.isEmpty()) {
            return Result.failure(IllegalArgumentException("No pages provided for document analysis"))
        }

        if (pageImages.any { it.bytes.isEmpty() }) {
            return Result.failure(IllegalArgumentException("One or more page images have empty data"))
        }

        return aiRepository.analyzeDocumentImages(
            pageImages = pageImages,
            subjectHint = subjectHint
        )
    }
}
