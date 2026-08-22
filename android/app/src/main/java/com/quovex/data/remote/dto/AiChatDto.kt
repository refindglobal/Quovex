package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqChatRequest(
    @SerializedName("model") val model: String = "openai/gpt-oss-20b",
    @SerializedName("messages") val messages: List<ChatMessageDto>,
    @SerializedName("temperature") val temperature: Float = 0.3f,
    @SerializedName("max_tokens") val maxTokens: Int = 2048
)

data class GroqChoiceDto(
    @SerializedName("message") val message: ChatMessageDto
)

data class GroqChatResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("choices") val choices: List<GroqChoiceDto>
)
