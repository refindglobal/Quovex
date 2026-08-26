package com.quovex.ui.material

import com.quovex.domain.model.ExtractedContent
import com.quovex.domain.model.FormulaItem
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.SubjectInference
import com.quovex.domain.usecase.ExtractUrlContentUseCase
import com.quovex.domain.usecase.InferNoteMetadataUseCase
import com.quovex.domain.usecase.SynthesizeLearningMaterialUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MaterialViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val inferNoteMetadataUseCase = mockk<InferNoteMetadataUseCase>()
    private val synthesizeLearningMaterialUseCase = mockk<SynthesizeLearningMaterialUseCase>()
    private val extractUrlContentUseCase = mockk<ExtractUrlContentUseCase>()

    private lateinit var viewModel: MaterialViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MaterialViewModel(
            inferNoteMetadataUseCase = inferNoteMetadataUseCase,
            synthesizeLearningMaterialUseCase = synthesizeLearningMaterialUseCase,
            extractUrlContentUseCase = extractUrlContentUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdle() {
        assertEquals(MaterialUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun importUrlContent_success_transitionsToInferred() = runTest {
        val url = "https://en.wikipedia.org/wiki/Photosynthesis"
        val extracted = ExtractedContent(
            title = "Photosynthesis",
            content = "Photosynthesis is a biological process...",
            inputType = NoteInputType.URL,
            sourceUrl = url
        )

        coEvery { extractUrlContentUseCase(url, NoteInputType.URL) } returns Result.success(extracted)
        coEvery { inferNoteMetadataUseCase(extracted.content, "Photosynthesis") } returns Result.success(
            SubjectInference(
                subject = "Biology",
                topic = "Photosynthesis",
                subtopic = "Light Reaction",
                confidence = 0.95f
            )
        )

        viewModel.importUrlContent(url, NoteInputType.URL)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MaterialUiState.Inferred)
        val inferred = state as MaterialUiState.Inferred
        assertEquals("Biology", inferred.inference.subject)
        assertEquals("Photosynthesis", inferred.inference.topic)
        assertEquals("Photosynthesis", inferred.initialTitle)
    }

    @Test
    fun importUrlContent_failure_transitionsToError() = runTest {
        val url = "https://unreachable.org/broken"
        coEvery { extractUrlContentUseCase(url, NoteInputType.URL) } returns Result.failure(
            Exception("Network timeout (HTTP 404)")
        )

        viewModel.importUrlContent(url, NoteInputType.URL)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MaterialUiState.Error)
        val error = state as MaterialUiState.Error
        assertEquals("Network timeout (HTTP 404)", error.message)
    }

    @Test
    fun confirmAndSynthesize_success_transitionsToSuccess() = runTest {
        val material = LearningMaterial(
            id = 42L,
            title = "Newtonian Mechanics",
            subject = "Physics",
            topic = "Laws of Motion",
            subtopic = "Inertia",
            summary = "Newton's three laws govern classical movement.",
            keyPoints = listOf("F = ma", "Action-Reaction"),
            formulas = listOf(FormulaItem(name = "Force equation", latex = "F = ma", description = "Force equation")),
            flashcardDeckId = 10,
            inferredConfidence = 0.95f
        )

        coEvery {
            synthesizeLearningMaterialUseCase(
                title = "Newtonian Mechanics",
                subject = "Physics",
                topic = "Laws of Motion",
                rawText = "Force equals mass times acceleration.",
                inputType = NoteInputType.TEXT,
                sourceUrl = null,
                inferredConfidence = 0.95f
            )
        } returns Result.success(material)

        viewModel.confirmAndSynthesize(
            confirmedSubject = "Physics",
            confirmedTopic = "Laws of Motion",
            confirmedTitle = "Newtonian Mechanics",
            rawText = "Force equals mass times acceleration.",
            inputType = NoteInputType.TEXT,
            confidence = 0.95f
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MaterialUiState.Success)
        val success = state as MaterialUiState.Success
        assertEquals(42L, success.material.id)
        assertEquals("Physics", success.material.subject)
    }
}
