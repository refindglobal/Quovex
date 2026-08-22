package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GatewayDoubtRequest(
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("subject") val subject: String = "General",
    @SerializedName("questionText") val questionText: String = ""
)

data class GatewayDoubtResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("solution") val solution: String? = null,
    @SerializedName("provider") val provider: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("error") val error: String? = null
)

data class GatewayUrlExtractRequest(
    @SerializedName("url") val url: String
)

data class GatewayUrlExtractResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("title") val title: String? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("wordCount") val wordCount: Int? = null,
    @SerializedName("error") val error: String? = null
)
