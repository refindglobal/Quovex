package com.quovex.data.remote

import com.quovex.data.remote.dto.GatewayChatRequest
import com.quovex.data.remote.dto.GatewayChatResponse
import com.quovex.data.remote.dto.GatewayClassifyRequest
import com.quovex.data.remote.dto.GatewayClassifyResponse
import com.quovex.data.remote.dto.GatewayDoubtRequest
import com.quovex.data.remote.dto.GatewayDoubtResponse
import com.quovex.data.remote.dto.GatewayPdfExtractRequest
import com.quovex.data.remote.dto.GatewayPdfExtractResponse
import com.quovex.data.remote.dto.GatewayPlanRequest
import com.quovex.data.remote.dto.GatewayPlanResponse
import com.quovex.data.remote.dto.GatewayQuizRequest
import com.quovex.data.remote.dto.GatewayQuizResponse
import com.quovex.data.remote.dto.GatewayQuoteResponse
import com.quovex.data.remote.dto.GatewaySummarizeRequest
import com.quovex.data.remote.dto.GatewaySummarizeResponse
import com.quovex.data.remote.dto.GatewayUrlExtractRequest
import com.quovex.data.remote.dto.GatewayUrlExtractResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AiGatewayApiService {

    @POST("ai/chat")
    suspend fun chat(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayChatRequest
    ): Response<GatewayChatResponse>

    @POST("ai/classify")
    suspend fun classify(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayClassifyRequest
    ): Response<GatewayClassifyResponse>

    @POST("ai/summarize")
    suspend fun summarize(
        @Header("Authorization") authHeader: String,
        @Body request: GatewaySummarizeRequest
    ): Response<GatewaySummarizeResponse>

    @POST("ai/quiz/generate")
    suspend fun generateQuiz(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayQuizRequest
    ): Response<GatewayQuizResponse>

    @POST("ai/doubt/image")
    suspend fun solveImageDoubt(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayDoubtRequest
    ): Response<GatewayDoubtResponse>

    @POST("notes/extract-url")
    suspend fun extractUrlContent(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayUrlExtractRequest
    ): Response<GatewayUrlExtractResponse>

    /**
     * POST /notes/extract-pdf
     * Triggers server-side PDF text extraction + AI summarization.
     */
    @POST("notes/extract-pdf")
    suspend fun extractPdf(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayPdfExtractRequest
    ): Response<GatewayPdfExtractResponse>

    @POST("ai/plan/generate")
    suspend fun generatePlan(
        @Header("Authorization") authHeader: String,
        @Body request: GatewayPlanRequest
    ): Response<GatewayPlanResponse>

    @GET("ai/quote")
    suspend fun getQuote(
        @Query("streak") streak: Int
    ): Response<GatewayQuoteResponse>
}
