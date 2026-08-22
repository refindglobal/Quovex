package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GatewayClassifyRequest(
    @SerializedName("textSample") val textSample: String,
    @SerializedName("filename") val filename: String? = null
)

data class GatewayClassifyResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("subject") val subject: String = "General",
    @SerializedName("topic") val topic: String = "",
    @SerializedName("subtopic") val subtopic: String? = null,
    @SerializedName("examRelevance") val examRelevance: List<String> = emptyList(),
    @SerializedName("confidence") val confidence: Float = 0f,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("error") val error: String? = null
)
