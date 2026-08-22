package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /notes/extract-pdf
 * Android uploads PDF to Firebase Storage, then calls this endpoint with the storage path.
 */
data class GatewayPdfExtractRequest(
    @SerializedName("storageRef") val storageRef: String,
    @SerializedName("noteId") val noteId: String,
    @SerializedName("subject") val subject: String = "General"
)

/**
 * Response from POST /notes/extract-pdf
 * Contains AI-generated structured summary of the PDF content.
 */
data class GatewayPdfExtractResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("summary") val summary: String?,
    @SerializedName("keyPoints") val keyPoints: List<String>?,
    @SerializedName("flashcards") val flashcards: List<GeneratedFlashcardDto>?,
    @SerializedName("wordCount") val wordCount: Int?,
    @SerializedName("provider") val provider: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("error") val error: String?
)
