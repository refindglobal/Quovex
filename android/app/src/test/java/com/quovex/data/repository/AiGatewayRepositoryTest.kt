package com.quovex.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.quovex.data.remote.AiGatewayApiService
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.GatewayChatRequest
import com.quovex.data.remote.dto.GatewayChatResponse
import com.quovex.data.remote.dto.GatewayPlanRequest
import com.quovex.data.remote.dto.GatewayPlanResponse
import com.quovex.data.remote.dto.GatewayQuoteResponse
import com.quovex.data.remote.dto.GatewaySummarizeRequest
import com.quovex.data.remote.dto.GatewaySummarizeResponse
import com.quovex.domain.model.AiError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AiGatewayRepositoryTest {

    private class FakeAuthService(
        var loggedIn: Boolean = false,
        var token: String? = null
    ) : FirebaseAuthService(null) {
        override val isUserLoggedIn: Boolean
            get() = loggedIn

        override suspend fun getIdToken(forceRefresh: Boolean): String? {
            return if (loggedIn) token ?: "mock_valid_token_123" else null
        }
    }

    private class FakeAiApiService(
        var shouldFailWithCode: Int? = null,
        var chatResponse: GatewayChatResponse = GatewayChatResponse(success = true, response = "Thermodynamics explanation")
    ) : AiGatewayApiService {
        override suspend fun chat(authHeader: String, request: GatewayChatRequest): Response<GatewayChatResponse> {
            val code = shouldFailWithCode
            return if (code != null) {
                Response.error(code, okhttp3.ResponseBody.create(null, "Error $code"))
            } else {
                Response.success(chatResponse)
            }
        }

        override suspend fun summarize(authHeader: String, request: GatewaySummarizeRequest): Response<GatewaySummarizeResponse> {
            val code = shouldFailWithCode
            return if (code != null) {
                Response.error(code, okhttp3.ResponseBody.create(null, "Error $code"))
            } else {
                Response.success(
                    GatewaySummarizeResponse(
                        success = true,
                        data = AiSummaryResult(
                            summary = "Generated summary",
                            keyPoints = listOf("Point 1", "Point 2")
                        )
                    )
                )
            }
        }

        override suspend fun solveImageDoubt(authHeader: String, request: com.quovex.data.remote.dto.GatewayDoubtRequest): Response<com.quovex.data.remote.dto.GatewayDoubtResponse> {
            val code = shouldFailWithCode
            return if (code != null) {
                Response.error(code, okhttp3.ResponseBody.create(null, "Error $code"))
            } else {
                Response.success(
                    com.quovex.data.remote.dto.GatewayDoubtResponse(
                        success = true,
                        solution = "Step by step solution",
                        provider = "groq"
                    )
                )
            }
        }

        override suspend fun extractUrlContent(authHeader: String, request: com.quovex.data.remote.dto.GatewayUrlExtractRequest): Response<com.quovex.data.remote.dto.GatewayUrlExtractResponse> {
            val code = shouldFailWithCode
            return if (code != null) {
                Response.error(code, okhttp3.ResponseBody.create(null, "Error $code"))
            } else {
                Response.success(
                    com.quovex.data.remote.dto.GatewayUrlExtractResponse(
                        success = true,
                        title = "Extracted Page",
                        text = "Page content body"
                    )
                )
            }
        }

        override suspend fun generatePlan(authHeader: String, request: GatewayPlanRequest): Response<GatewayPlanResponse> {
            return Response.success(GatewayPlanResponse(success = true, plan = "Study Plan"))
        }

        override suspend fun getQuote(streak: Int): Response<GatewayQuoteResponse> {
            return Response.success(GatewayQuoteResponse(quote = "Focus is everything", author = "Quovex"))
        }
    }

    @Test
    fun `sendChatMessage without logged in user uses guest fallback and succeeds`() = runTest {
        val fakeAuth = FakeAuthService(loggedIn = false)
        val fakeApi = FakeAiApiService()
        val repo = AiGatewayRepositoryImpl(fakeApi, fakeAuth)

        val result = repo.sendChatMessage("Explain entropy")
        assertTrue(result.isSuccess)
        assertEquals("Thermodynamics explanation", result.getOrThrow())
    }

    @Test
    fun `sendChatMessage with authenticated user succeeds`() = runTest {
        val fakeAuth = FakeAuthService(loggedIn = true, token = "test_valid_jwt")
        val fakeApi = FakeAiApiService()
        val repo = AiGatewayRepositoryImpl(fakeApi, fakeAuth)

        val result = repo.sendChatMessage("Explain entropy")
        assertTrue(result.isSuccess)
        assertEquals("Thermodynamics explanation", result.getOrThrow())
    }

    @Test
    fun `sendChatMessage with HTTP 429 returns RateLimitError`() = runTest {
        val fakeAuth = FakeAuthService(loggedIn = true, token = "test_valid_jwt")
        val fakeApi = FakeAiApiService(shouldFailWithCode = 429)
        val repo = AiGatewayRepositoryImpl(fakeApi, fakeAuth)

        val result = repo.sendChatMessage("Explain entropy")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AiError.RateLimitError)
    }

    @Test
    fun `sendChatMessage with HTTP 503 returns ProviderUnavailableError`() = runTest {
        val fakeAuth = FakeAuthService(loggedIn = true, token = "test_valid_jwt")
        val fakeApi = FakeAiApiService(shouldFailWithCode = 503)
        val repo = AiGatewayRepositoryImpl(fakeApi, fakeAuth)

        val result = repo.sendChatMessage("Explain entropy")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AiError.ProviderUnavailableError)
    }

    @Test
    fun `getDailyQuote succeeds without auth`() = runTest {
        val fakeAuth = FakeAuthService(loggedIn = false)
        val fakeApi = FakeAiApiService()
        val repo = AiGatewayRepositoryImpl(fakeApi, fakeAuth)

        val result = repo.getDailyQuote(14)
        assertTrue(result.isSuccess)
        val (quote, author) = result.getOrThrow()
        assertEquals("Focus is everything", quote)
        assertEquals("Quovex", author)
    }

    @Test
    fun `summarizeNote returns structured result on success`() = runTest {
        val fakeAuth = FakeAuthService(loggedIn = true, token = "test_valid_jwt")
        val fakeApi = FakeAiApiService()
        val repo = AiGatewayRepositoryImpl(fakeApi, fakeAuth)

        val result = repo.summarizeNote("Raw physics text", "Physics")
        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals("Generated summary", data.summary)
        assertEquals(2, data.keyPoints.size)
    }
}
