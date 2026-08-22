package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GatewayQuizQuestionDto(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("question") val question: String = "",
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("correctIndex") val correctIndex: Int = 0,
    @SerializedName("explanation") val explanation: String = "",
    @SerializedName("relatedConcept") val relatedConcept: String = ""
)

data class GatewayQuizDataDto(
    @SerializedName("questions") val questions: List<GatewayQuizQuestionDto> = emptyList()
)

data class GatewayQuizRequest(
    @SerializedName("subject") val subject: String = "General",
    @SerializedName("topic") val topic: String = "",
    @SerializedName("difficulty") val difficulty: String = "Medium",
    @SerializedName("keyPoints") val keyPoints: List<String> = emptyList()
)

data class GatewayQuizResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: GatewayQuizDataDto? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("error") val error: String? = null
)
