package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.quovex.domain.model.ScannedChapter
import com.quovex.domain.model.ScannedDocumentOrganization
import com.quovex.domain.model.ScannedNoteSection
import com.quovex.domain.model.ScannedSubtopic

/**
 * DTOs for the POST ai/document/analyze gateway endpoint.
 *
 * This endpoint is SEPARATE from ai/doubt/image (Image Doubt).
 *
 * Document Analysis purpose:
 *   Multi-page document → understand pages → identify sections → classify topics
 *   → group pages → create chapter structure → create subtopics → create notes
 *
 * Pages are sent in batches of up to BATCH_SIZE to avoid per-page cost explosion.
 * The gateway performs document-level synthesis after receiving all batches.
 *
 * Architecture:
 *   Pages 1–5  → batch request → partial analysis
 *   Pages 6–10 → batch request → partial analysis
 *   ...
 *   All batches → synthesis → ScannedDocumentOrganization
 */

/** One page in a batch — compressed JPEG as base64 data URI */
data class DocumentPageDto(
    @SerializedName("pageIndex") val pageIndex: Int,
    @SerializedName("imageDataUri") val imageDataUri: String  // "data:image/jpeg;base64,..."
)

/**
 * Request for POST ai/document/analyze
 *
 * @param pages Batch of pages (max BATCH_SIZE per request)
 * @param totalPages Total number of pages in the full document
 * @param batchIndex Zero-based index of this batch
 * @param totalBatches Total number of batches
 * @param subject Optional hint: user-selected subject (e.g. "Physics", "Accountancy")
 * @param isFinalBatch Whether this is the last batch — triggers synthesis
 * @param partialResults Accumulated partial results from prior batches (for synthesis)
 */
data class GatewayDocumentAnalyzeRequest(
    @SerializedName("pages") val pages: List<DocumentPageDto>,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("batchIndex") val batchIndex: Int,
    @SerializedName("totalBatches") val totalBatches: Int,
    @SerializedName("subject") val subject: String = "",
    @SerializedName("isFinalBatch") val isFinalBatch: Boolean,
    @SerializedName("partialResults") val partialResults: String? = null  // JSON of prior batches
)

/**
 * Response from POST ai/document/analyze
 *
 * On intermediate batches: returns partial analysis.
 * On final batch (isFinalBatch=true): returns full organization.
 */
data class GatewayDocumentAnalyzeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("isFinal") val isFinal: Boolean = false,
    @SerializedName("detectedSubject") val detectedSubject: String? = null,
    @SerializedName("detectedStream") val detectedStream: String? = null,
    @SerializedName("chapters") val chapters: List<DocumentChapterDto>? = null,
    @SerializedName("partialAnalysis") val partialAnalysis: String? = null,  // JSON for intermediate batches
    @SerializedName("confidence") val confidence: Float = 0.0f,
    @SerializedName("error") val error: String? = null
)

data class DocumentChapterDto(
    @SerializedName("title") val title: String,
    @SerializedName("subtopics") val subtopics: List<DocumentSubtopicDto> = emptyList(),
    @SerializedName("startPage") val startPage: Int = 0,
    @SerializedName("endPage") val endPage: Int = 0
)

data class DocumentSubtopicDto(
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String = "",
    @SerializedName("keyPoints") val keyPoints: List<String> = emptyList(),
    @SerializedName("startPage") val startPage: Int = 0,
    @SerializedName("endPage") val endPage: Int = 0
)

/** Map gateway response to domain model */
fun GatewayDocumentAnalyzeResponse.toDomain(): ScannedDocumentOrganization {
    return ScannedDocumentOrganization(
        detectedSubject = detectedSubject ?: "",
        detectedStream = detectedStream ?: "",
        chapters = chapters?.map { it.toDomain() } ?: emptyList(),
        confidence = confidence
    )
}

fun DocumentChapterDto.toDomain(): ScannedChapter {
    return ScannedChapter(
        title = title,
        subtopics = subtopics.map { it.toDomain() },
        pageRange = startPage..endPage
    )
}

fun DocumentSubtopicDto.toDomain(): ScannedSubtopic {
    return ScannedSubtopic(
        title = title,
        noteSections = if (content.isBlank()) emptyList() else listOf(
            ScannedNoteSection(
                sectionTitle = "Notes",
                content = content,
                keyPoints = keyPoints,
                sourcePageRange = startPage..endPage
            )
        ),
        pageRange = startPage..endPage
    )
}
