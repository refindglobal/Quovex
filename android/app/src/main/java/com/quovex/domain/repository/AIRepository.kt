package com.quovex.domain.repository

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.SubjectInference

interface AIRepository {
    suspend fun sendChatMessage(
        message: String,
        subject: String = "General",
        history: List<ChatMessageDto> = emptyList()
    ): Result<String>

    suspend fun sendTutorMessage(
        message: String,
        subject: String = "General",
        topic: String = "",
        materialSummary: String? = null,
        recentMistakes: List<String> = emptyList(),
        history: List<ChatMessageDto> = emptyList()
    ): Result<String>

    suspend fun classifyMaterial(
        textSample: String,
        filename: String? = null
    ): Result<SubjectInference>

    suspend fun summarizeNote(
        rawText: String,
        subject: String = "General"
    ): Result<AiSummaryResult>

    suspend fun generateQuiz(
        subject: String = "General",
        topic: String = "",
        difficulty: String = "Medium",
        keyPoints: List<String> = emptyList()
    ): Result<List<QuizQuestion>>

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
