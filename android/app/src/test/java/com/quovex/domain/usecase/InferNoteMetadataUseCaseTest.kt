package com.quovex.domain.usecase

import com.quovex.domain.model.SubjectInference
import com.quovex.domain.repository.AIRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class InferNoteMetadataUseCaseTest {

    private val aiRepository = mockk<AIRepository>()
    private lateinit var useCase: InferNoteMetadataUseCase

    @Before
    fun setup() {
        useCase = InferNoteMetadataUseCase(aiRepository)
    }

    @Test
    fun inferMetadata_success_returnsSubjectInference() = runTest {
        val content = "The mitochondria is the powerhouse of the cell."
        val mockInference = SubjectInference(
            subject = "Biology",
            topic = "Cell Structure",
            subtopic = "Mitochondria",
            confidence = 0.96f,
            examRelevance = listOf("NEET-UG", "CBSE Class 12")
        )

        coEvery { aiRepository.classifyMaterial(any(), "Cell Biology") } returns Result.success(mockInference)

        val result = useCase(content, "Cell Biology")
        assertTrue(result.isSuccess)
        val inference = result.getOrNull()
        assertEquals("Biology", inference?.subject)
        assertEquals("Cell Structure", inference?.topic)
        assertEquals("Mitochondria", inference?.subtopic)
        assertEquals(0.96f, inference?.confidence ?: 0f, 0.01f)
    }

    @Test
    fun inferMetadata_blankContent_returnsFailure() = runTest {
        val result = useCase("   ", "")
        assertTrue(result.isFailure)
        assertEquals("Text content cannot be blank for inference", result.exceptionOrNull()?.message)
    }
}

