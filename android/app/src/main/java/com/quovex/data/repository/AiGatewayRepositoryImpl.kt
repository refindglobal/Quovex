package com.quovex.data.repository

import com.quovex.data.batch.DocumentAnalysisBatcher
import com.quovex.data.remote.AiGatewayApiService
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.data.remote.dto.DocumentPageDto
import com.quovex.data.remote.dto.GatewayChatRequest
import com.quovex.data.remote.dto.GatewayClassifyRequest
import com.quovex.data.remote.dto.GatewayDocumentAnalyzeRequest
import com.quovex.data.remote.dto.GatewayDoubtRequest
import com.quovex.data.remote.dto.GatewayPlanRequest
import com.quovex.data.remote.dto.GatewayQuizRequest
import com.quovex.data.remote.dto.GatewaySummarizeRequest
import com.quovex.data.remote.dto.GatewayUrlExtractRequest
import com.quovex.data.remote.dto.toDomain
import com.quovex.domain.model.AiError
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.ScannedDocumentOrganization
import com.quovex.domain.model.SubjectInference
import com.quovex.domain.repository.AIRepository
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiGatewayRepositoryImpl @Inject constructor(
    private val apiService: AiGatewayApiService,
    private val authService: FirebaseAuthService
) : AIRepository {

    private suspend fun getAuthHeader(): String {
        return try {
            val token = authService.getIdToken(false)
            if (!token.isNullOrBlank()) {
                "Bearer $token"
            } else {
                "Bearer guest_mode_token"
            }
        } catch (_: Exception) {
            "Bearer guest_mode_token"
        }
    }

    private fun mapException(e: Throwable): AiError {
        return when (e) {
            is AiError -> e
            is SocketTimeoutException -> AiError.TimeoutError("The AI gateway timed out. Please retry.", e)
            is IOException -> AiError.NetworkError("Network connectivity issue. Please check your connection.", e)
            else -> AiError.UnknownAIError(e.message ?: "An unexpected error occurred", e)
        }
    }

    override suspend fun sendChatMessage(
        message: String,
        subject: String,
        history: List<ChatMessageDto>
    ): Result<String> {
        return sendTutorMessage(
            message = message,
            subject = subject,
            topic = "",
            materialSummary = null,
            recentMistakes = emptyList(),
            history = history
        )
    }

    override suspend fun sendTutorMessage(
        message: String,
        subject: String,
        topic: String,
        materialSummary: String?,
        recentMistakes: List<String>,
        history: List<ChatMessageDto>
    ): Result<String> {
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.chat(
                authHeader = authHeader,
                request = GatewayChatRequest(
                    message = message,
                    subject = subject,
                    topic = topic,
                    materialSummary = materialSummary,
                    recentMistakes = recentMistakes,
                    history = history
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.response != null) {
                    Result.success(body.response)
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Invalid response from AI gateway"))
                }
            } else {
                val error = when (response.code()) {
                    401 -> AiError.AuthenticationError("Session expired or invalid. Please sign in again.")
                    429 -> AiError.RateLimitError("Daily or hourly AI limit reached. Please wait.")
                    503, 502 -> AiError.ProviderUnavailableError("AI service temporarily unavailable. Please try again.")
                    else -> AiError.UnknownAIError("Gateway error (HTTP ${response.code()})")
                }
                Result.failure(error)
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun classifyMaterial(
        textSample: String,
        filename: String?
    ): Result<SubjectInference> {
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.classify(
                authHeader = authHeader,
                request = GatewayClassifyRequest(
                    textSample = textSample,
                    filename = filename
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    Result.success(
                        SubjectInference(
                            subject = body.subject,
                            topic = body.topic,
                            subtopic = body.subtopic,
                            examRelevance = body.examRelevance,
                            confidence = body.confidence
                        )
                    )
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to classify material"))
                }
            } else {
                Result.failure(AiError.UnknownAIError("Classification error (HTTP ${response.code()})"))
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun summarizeNote(
        rawText: String,
        subject: String
    ): Result<AiSummaryResult> {
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.summarize(
                authHeader = authHeader,
                request = GatewaySummarizeRequest(
                    text = rawText,
                    subject = subject
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to parse note summary"))
                }
            } else {
                val error = when (response.code()) {
                    401 -> AiError.AuthenticationError("Session expired or invalid. Please sign in again.")
                    429 -> AiError.RateLimitError("AI request quota exceeded.")
                    503, 502 -> AiError.ProviderUnavailableError("AI note summarizer is currently busy.")
                    else -> AiError.UnknownAIError("Gateway error (HTTP ${response.code()})")
                }
                Result.failure(error)
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun generateQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        keyPoints: List<String>
    ): Result<List<QuizQuestion>> {
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.generateQuiz(
                authHeader = authHeader,
                request = GatewayQuizRequest(
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    keyPoints = keyPoints
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val questions = body.data.questions.map { dto ->
                        QuizQuestion(
                            id = 0L,
                            materialId = 0L,
                            question = dto.question,
                            options = dto.options,
                            correctIndex = dto.correctIndex,
                            explanation = dto.explanation,
                            relatedConcept = dto.relatedConcept.ifBlank { topic }
                        )
                    }
                    Result.success(questions)
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to generate quiz questions"))
                }
            } else {
                Result.failure(AiError.UnknownAIError("Quiz generation error (HTTP ${response.code()})"))
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun solveImageDoubt(
        imageInput: DomainImageInput,
        subject: String,
        questionText: String
    ): Result<ImageDoubtSolution> {
        return try {
            val authHeader = getAuthHeader()
            val base64 = java.util.Base64.getEncoder().encodeToString(imageInput.bytes)
            val dataUri = "data:${imageInput.mimeType};base64,$base64"

            val response = apiService.solveImageDoubt(
                authHeader = authHeader,
                request = GatewayDoubtRequest(
                    imageUrl = dataUri,
                    base64Image = base64,
                    subject = subject,
                    questionText = questionText
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.solution != null) {
                    Result.success(
                        ImageDoubtSolution(
                            solution = body.solution,
                            provider = body.provider ?: "groq",
                            model = body.model
                        )
                    )
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to solve problem image"))
                }
            } else {
                val error = when (response.code()) {
                    401 -> AiError.AuthenticationError("Session expired or invalid. Please sign in again.")
                    429 -> AiError.RateLimitError("Daily vision AI doubt limit reached.")
                    503, 502 -> AiError.ProviderUnavailableError("AI vision tutor is temporarily unavailable.")
                    else -> AiError.UnknownAIError("Gateway error (HTTP ${response.code()})")
                }
                Result.failure(error)
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun extractUrlContent(url: String): Result<Pair<String, String>> {
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.extractUrlContent(
                authHeader = authHeader,
                request = GatewayUrlExtractRequest(url = url)
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.text != null) {
                    Result.success(Pair(body.title ?: url, body.text))
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to extract web content"))
                }
            } else {
                val error = when (response.code()) {
                    401 -> AiError.AuthenticationError("Session expired or invalid.")
                    429 -> AiError.RateLimitError("Request limit exceeded.")
                    else -> AiError.UnknownAIError("Failed to extract URL (HTTP ${response.code()})")
                }
                Result.failure(error)
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun generateStudyPlan(
        examName: String,
        targetHours: Int,
        subjects: List<String>,
        days: Int
    ): Result<String> {
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.generatePlan(
                authHeader = authHeader,
                request = GatewayPlanRequest(
                    examName = examName,
                    targetHours = targetHours,
                    subjects = subjects,
                    days = days
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.plan != null) {
                    Result.success(body.plan)
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to generate study plan"))
                }
            } else {
                Result.failure(AiError.UnknownAIError("Gateway error (HTTP ${response.code()})"))
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    override suspend fun getDailyQuote(streak: Int): Result<Pair<String, String>> {
        return try {
            val response = apiService.getQuote(streak)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.success(Pair(body.quote, body.author))
            } else {
                Result.success(Pair("Discipline is the bridge between goals and accomplishment.", "Jim Rohn"))
            }
        } catch (e: Throwable) {
            Result.success(Pair("Action is the foundational key to all success.", "Pablo Picasso"))
        }
    }

    /**
     * AI Chat with image attachment.
     *
     * Reuses the ai/doubt/image endpoint infrastructure since both involve
     * vision + text question → contextual answer.
     * The framing/prompt on the gateway side will differ from academic problem solving.
     *
     * Shared with: image compression, base64 encoding, auth header
     * NOT shared with: Document Intelligence (separate endpoint + use case)
     */
    override suspend fun sendMessageWithImage(
        imageInput: DomainImageInput,
        message: String,
        subject: String,
        history: List<ChatMessageDto>
    ): Result<String> {
        return try {
            val authHeader = getAuthHeader()
            val base64 = java.util.Base64.getEncoder().encodeToString(imageInput.bytes)
            val dataUri = "data:${imageInput.mimeType};base64,$base64"

            val response = apiService.solveImageDoubt(
                authHeader = authHeader,
                request = GatewayDoubtRequest(
                    imageUrl = dataUri,
                    base64Image = base64,
                    subject = subject,
                    // Use message as the question — gateway will infer chat context from subject
                    questionText = message.ifBlank { "Please analyze and explain what you see in this image." }
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.solution != null) {
                    Result.success(body.solution)
                } else {
                    Result.failure(AiError.InvalidResponseError(body?.error ?: "Failed to analyze image"))
                }
            } else {
                val error = when (response.code()) {
                    401 -> AiError.AuthenticationError("Session expired or invalid. Please sign in again.")
                    429 -> AiError.RateLimitError("Daily vision AI limit reached.")
                    503, 502 -> AiError.ProviderUnavailableError("Quovex AI is temporarily unavailable.")
                    else -> AiError.UnknownAIError("Gateway error (HTTP ${response.code()})")
                }
                Result.failure(error)
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    /**
     * Document Intelligence — multi-page document analysis.
     *
     * SEPARATE from solveImageDoubt (ai/doubt/image) and sendMessageWithImage.
     * Uses dedicated endpoint: ai/document/analyze
     *
     * Batching strategy:
     *   - Pages split into chunks of BATCH_SIZE (5)
     *   - Each chunk sent as a separate request
     *   - Partial analysis JSON accumulated across batches
     *   - Final batch triggers synthesis → returns full ScannedDocumentOrganization
     *
     * Cost efficiency:
     *   - 10-page document = 2 batches (not 10 individual requests)
     *   - 30-page document = 6 batches
     *   - Pages compressed to JPEG ≤ 256KB before sending
     */
    override suspend fun analyzeDocumentImages(
        pageImages: List<DomainImageInput>,
        subjectHint: String
    ): Result<ScannedDocumentOrganization> {
        if (pageImages.isEmpty()) {
            return Result.failure(AiError.InvalidResponseError("No pages to analyze"))
        }

        return try {
            val authHeader = getAuthHeader()
            val batches = pageImages.chunked(DOCUMENT_ANALYSIS_BATCH_SIZE)
            val totalBatches = batches.size
            val totalPages = pageImages.size
            var partialResults: String? = null
            var lastResponse: com.quovex.data.remote.dto.GatewayDocumentAnalyzeResponse? = null

            batches.forEachIndexed { batchIndex, batchPages ->
                val isFinalBatch = batchIndex == totalBatches - 1

                val pageDtos = batchPages.mapIndexed { localIndex, imageInput ->
                    val globalPageIndex = batchIndex * DOCUMENT_ANALYSIS_BATCH_SIZE + localIndex
                    val base64 = java.util.Base64.getEncoder().encodeToString(imageInput.bytes)
                    DocumentPageDto(
                        pageIndex = globalPageIndex,
                        imageDataUri = "data:${imageInput.mimeType};base64,$base64"
                    )
                }

                val response = apiService.analyzeDocument(
                    authHeader = authHeader,
                    request = GatewayDocumentAnalyzeRequest(
                        pages = pageDtos,
                        totalPages = totalPages,
                        batchIndex = batchIndex,
                        totalBatches = totalBatches,
                        subject = subjectHint,
                        isFinalBatch = isFinalBatch,
                        partialResults = partialResults
                    )
                )

                if (!response.isSuccessful || response.body() == null) {
                    val errorCode = response.code()
                    val error = when (errorCode) {
                        401 -> AiError.AuthenticationError("Session expired or invalid.")
                        429 -> AiError.RateLimitError("Document analysis quota reached.")
                        503, 502 -> AiError.ProviderUnavailableError("Document AI is temporarily unavailable.")
                        else -> AiError.UnknownAIError("Document analysis error (HTTP $errorCode)")
                    }
                    return Result.failure(error)
                }

                val body = response.body()!!
                if (!body.success) {
                    return Result.failure(
                        AiError.InvalidResponseError(body.error ?: "Document analysis failed")
                    )
                }

                lastResponse = body
                // Accumulate partial analysis for next batch
                if (!isFinalBatch) {
                    partialResults = body.partialAnalysis
                }
            }

            val finalResponse = lastResponse
            if (finalResponse != null && finalResponse.chapters != null) {
                Result.success(finalResponse.toDomain())
            } else {
                Result.failure(AiError.InvalidResponseError("Document analysis did not return organization data"))
            }
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    companion object {
        /**
         * Pages per analysis batch — sourced from [DocumentAnalysisBatcher.DEFAULT_BATCH_SIZE].
         * Change [DocumentAnalysisBatcher.DEFAULT_BATCH_SIZE] to adjust globally.
         */
        private val DOCUMENT_ANALYSIS_BATCH_SIZE
            get() = DocumentAnalysisBatcher.DEFAULT_BATCH_SIZE
    }
}
