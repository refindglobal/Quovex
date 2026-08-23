package com.quovex.ui.ncert

import androidx.lifecycle.SavedStateHandle
import com.quovex.domain.usecase.FakeAIRepository
import com.quovex.domain.usecase.FakeNcertRepository
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.SaveNoteUseCase
import com.quovex.domain.usecase.StudyNcertChapterWithAiUseCase
import com.quovex.domain.usecase.SummarizeNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NcertViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var ncertRepository: FakeNcertRepository
    private lateinit var quovexRepository: FakeQuovexRepository
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var studyUseCase: StudyNcertChapterWithAiUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ncertRepository = FakeNcertRepository()
        quovexRepository = FakeQuovexRepository()
        aiRepository = FakeAIRepository()

        val saveNoteUseCase = SaveNoteUseCase(quovexRepository)
        val summarizeNoteUseCase = SummarizeNoteUseCase(aiRepository)
        studyUseCase = StudyNcertChapterWithAiUseCase(quovexRepository, saveNoteUseCase, summarizeNoteUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testNcertBrowserViewModelClassAndSubjectSelection() = runTest {
        val viewModel = NcertBrowserViewModel(ncertRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(12, state.selectedClass)
        assertEquals(listOf(10, 12), state.availableClasses)

        viewModel.selectClass(10)
        advanceUntilIdle()
        assertEquals(10, viewModel.uiState.value.selectedClass)
        assertEquals("All", viewModel.uiState.value.selectedSubject)
    }

    @Test
    fun testNcertBookDetailViewModelLoadsChapters() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("bookId" to "ncert-12-phy-1"))
        val viewModel = NcertBookDetailViewModel(savedStateHandle, ncertRepository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.book)
        assertEquals("Physics Part I", state.book?.title)
        assertEquals(2, state.chapters.size)
        assertEquals("Electric Charges and Fields", state.chapters[0].chapterTitle)
    }

    @Test
    fun testNcertChapterDetailViewModelStudyWithAi() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "ncert-12-phy-1-ch6"))
        val viewModel = NcertChapterDetailViewModel(savedStateHandle, ncertRepository, studyUseCase)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.chapter)
        assertEquals("Electromagnetic Induction", state.chapter?.chapterTitle)

        var navigatedMaterialId: Long? = null
        viewModel.studyWithQuovexAi { matId ->
            navigatedMaterialId = matId
        }
        advanceUntilIdle()

        assertNotNull(navigatedMaterialId)
        assertTrue(navigatedMaterialId!! > 0)
        assertEquals(navigatedMaterialId, viewModel.uiState.value.createdMaterialId)
    }
}
