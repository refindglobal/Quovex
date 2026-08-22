package com.quovex.domain.model

data class SubjectInference(
    val subject: String,
    val topic: String,
    val subtopic: String? = null,
    val examRelevance: List<String> = emptyList(),
    val confidence: Float = 0f
)
