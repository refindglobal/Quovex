package com.quovex.domain.repository

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.ScannedDocumentOrganization
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

    /** Image attachment in AI Chat: single image + text question → contextual response */
    suspend fun sendMessageWithImage(
        imageInput: DomainImageInput,
        message: String,
        subject: String = "General",
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

    /**
     * Image Doubt Solver — single image, single academic problem.
     * Uses: ai/doubt/image
     * Model: openai/gpt-oss-120b (vision, Groq) or gemma-4-31b (Cerebras fallback)
     *
     * DIFFERENT from analyzeDocumentImages which handles multi-page documents.
     */
    suspend fun solveImageDoubt(
        imageInput: DomainImageInput,
        subject: String = "General",
        questionText: String = ""
    ): Result<ImageDoubtSolution>

    /**
     * Document Intelligence — multi-page document analysis.
     * Uses: ai/document/analyze (DEDICATED endpoint — NOT ai/doubt/image)
     *
     * Accepts multiple page images and returns structured chapter/subtopic organization.
     * Internally uses 5-page batching to avoid per-page cost explosion.
     *
     * DIFFERENT from solveImageDoubt which handles single academic problem images.
     *
     * @param pageImages List of compressed page images in order
     * @param subjectHint Optional user-selected subject to guide AI (e.g. "Physics", "Accountancy")
     */
    suspend fun analyzeDocumentImages(
        pageImages: List<DomainImageInput>,
        subjectHint: String = ""
    ): Result<ScannedDocumentOrganization>

    suspend fun extractUrlContent(url: String): Result<Pair<String, String>>

    suspend fun generateStudyPlan(
        examName: String,
        targetHours: Int,
        subjects: List<String>,
        days: Int = 30
    ): Result<String>

    suspend fun getDailyQuote(streak: Int = 1): Result<Pair<String, String>>
}

