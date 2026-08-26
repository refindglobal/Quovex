package com.quovex.domain.usecase

import com.quovex.domain.model.DoubtFollowUpMessage
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AskDoubtFollowUpUseCaseTest {

    private lateinit var aiRepository: FakeAIRepository
    private lateinit var useCase: AskDoubtFollowUpUseCase

    @Before
    fun setup() {
        aiRepository = FakeAIRepository()
        useCase = AskDoubtFollowUpUseCase(aiRepository)
    }

    @Test
    fun `invoke with blank question returns failure`() = runTest {
        val result = useCase(
            subject = "Physics",
            problemContext = "F = 10N, m = 2kg",
            solutionContext = "a = F/m = 5 m/s²",
            previousMessages = emptyList(),
            newQuestion = "   "
        )

        assertTrue(result.isFailure)
        assertEquals("Question cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with valid question returns contextual response`() = runTest {
        val history = listOf(
            DoubtFollowUpMessage(isUser = true, text = "Why is friction ignored?"),
            DoubtFollowUpMessage(isUser = false, text = "Because the surface was stated as smooth/frictionless.")
        )

        val result = useCase(
            subject = "Physics",
            problemContext = "F = 10N on smooth floor",
            solutionContext = "a = 5 m/s²",
            previousMessages = history,
            newQuestion = "What if coefficient of friction was 0.2?"
        )

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertTrue(response!!.contains("Tutor response for Physics"))
    }
}
