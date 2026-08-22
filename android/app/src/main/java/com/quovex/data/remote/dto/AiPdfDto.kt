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
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("keyPoints") val keyPoints: List<String>? = null,
    @SerializedName("flashcards") val flashcards: List<GeneratedFlashcardDto>? = null,
    @SerializedName("wordCount") val wordCount: Int? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("error") val error: String? = null
)
