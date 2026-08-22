package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeneratedFlashcardDto(
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("formula") val formula: String? = null
)

data class AiSummaryResult(
    @SerializedName("summary") val summary: String = "",
    @SerializedName("keyPoints") val keyPoints: List<String> = emptyList(),
    @SerializedName("flashcards") val flashcards: List<GeneratedFlashcardDto> = emptyList()
)
