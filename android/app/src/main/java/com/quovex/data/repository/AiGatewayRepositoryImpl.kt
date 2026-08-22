package com.quovex.data.repository

import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.AiGatewayApiService
import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.data.remote.dto.GatewayChatRequest
import com.quovex.data.remote.dto.GatewayPlanRequest
import com.quovex.data.remote.dto.GatewaySummarizeRequest
import com.quovex.domain.model.AiError
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution
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
        return try {
            val authHeader = getAuthHeader()
            val response = apiService.chat(
                authHeader = authHeader,
                request = GatewayChatRequest(
                    message = message,
                    subject = subject,
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
                request = com.quovex.data.remote.dto.GatewayDoubtRequest(
                    imageUrl = dataUri,
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
                request = com.quovex.data.remote.dto.GatewayUrlExtractRequest(url = url)
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
}
