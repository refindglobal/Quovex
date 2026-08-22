package com.quovex.domain.usecase

import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SolveImageDoubtUseCaseTest {

    private lateinit var aiRepository: FakeAIRepository
    private lateinit var useCase: SolveImageDoubtUseCase

    @Before
    fun setup() {
        aiRepository = FakeAIRepository()
        useCase = SolveImageDoubtUseCase(aiRepository)
    }

    @Test
    fun `invoke rejects empty image bytes with IllegalArgumentException`() = runTest {
        val emptyInput = DomainImageInput(bytes = ByteArray(0))
        val result = useCase(emptyInput, subject = "Physics")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke returns successful solution from vision AI`() = runTest {
        val sampleInput = DomainImageInput(bytes = byteArrayOf(1, 2, 3, 4, 5), mimeType = "image/jpeg")
        val result = useCase(sampleInput, subject = "Physics", questionText = "Find acceleration")

        assertTrue(result.isSuccess)
        val solution = result.getOrNull()
        assertNotNull(solution)
        assertTrue(solution!!.solution.contains("Newton's Second Law"))
        assertEquals("groq", solution.provider)
    }

    @Test
    fun `invoke propagates failure when AI Gateway is unavailable`() = runTest {
        aiRepository.shouldFailDoubt = true
        val sampleInput = DomainImageInput(bytes = byteArrayOf(1, 2, 3), mimeType = "image/jpeg")
        val result = useCase(sampleInput, subject = "Maths")

        assertTrue(result.isFailure)
        assertEquals("Vision AI Provider unavailable", result.exceptionOrNull()?.message)
    }
}
