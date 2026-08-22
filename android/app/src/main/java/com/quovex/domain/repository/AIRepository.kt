package com.quovex.domain.repository

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution

interface AIRepository {
    suspend fun sendChatMessage(
        message: String,
        subject: String = "General",
        history: List<ChatMessageDto> = emptyList()
    ): Result<String>

    suspend fun summarizeNote(
        rawText: String,
        subject: String = "General"
    ): Result<AiSummaryResult>

    suspend fun solveImageDoubt(
        imageInput: DomainImageInput,
        subject: String = "General",
        questionText: String = ""
    ): Result<ImageDoubtSolution>

    suspend fun extractUrlContent(url: String): Result<Pair<String, String>>

    suspend fun generateStudyPlan(
        examName: String,
        targetHours: Int,
        subjects: List<String>,
        days: Int = 30
    ): Result<String>

    suspend fun getDailyQuote(streak: Int = 1): Result<Pair<String, String>>
}
