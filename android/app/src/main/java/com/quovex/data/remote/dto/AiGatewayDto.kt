package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GatewayChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("subject") val subject: String = "General",
    @SerializedName("history") val history: List<ChatMessageDto> = emptyList()
)

data class GatewayChatResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("response") val response: String? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("error") val error: String? = null
)

data class GatewaySummarizeRequest(
    @SerializedName("text") val text: String,
    @SerializedName("subject") val subject: String = "General"
)

data class GatewaySummarizeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: AiSummaryResult? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("error") val error: String? = null
)

data class GatewayPlanRequest(
    @SerializedName("examName") val examName: String,
    @SerializedName("targetHours") val targetHours: Int,
    @SerializedName("subjects") val subjects: List<String>,
    @SerializedName("days") val days: Int = 30
)

data class GatewayPlanResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("plan") val plan: String? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("error") val error: String? = null
)

data class GatewayQuoteResponse(
    @SerializedName("quote") val quote: String = "",
    @SerializedName("author") val author: String = ""
)
