package com.quovex.ui.originals

import com.quovex.domain.model.originals.OriginalChapter
import com.quovex.domain.model.originals.OriginalFlashcard
import com.quovex.domain.model.originals.OriginalQuizQuestion
import com.quovex.domain.model.originals.OriginalSection
import com.quovex.domain.model.originals.QuovexOriginalBook
import com.quovex.domain.repository.QuovexOriginalsRepository
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.PrepareOriginalChapterStudyAidsUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
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

class FakeQuovexOriginalsRepository : QuovexOriginalsRepository {
    private val books = mutableListOf(
        QuovexOriginalBook(
            id = "book_physics_newton",
            title = "Newton's Laws — Made Simple",
            subtitle = "Mastering Classical Dynamics for JEE",
            description = "A rigorous, intuitive guide to forces and momentum.",
            subject = "Physics",
            topic = "Classical Mechanics",
            countryRegion = "IN",
            curriculum = "CBSE",
            gradeClass = "Class 11",
            exam = "JEE Main",
            chapterCount = 2,
            introduction = "Welcome to classical mechanics.",
            learningObjectives = listOf("Master F=ma", "Understand Inertia"),
            chapters = listOf(
                OriginalChapter(
                    chapterNumber = 1,
                    title = "First Law & Inertia",
                    summary = "Inertia and reference frames.",
                    sections = listOf(
                        OriginalSection(
                            id = "sec_1_1",
                            sectionNumber = "1.1",
                            title = "Galileo to Newton",
                            conceptualExplanation = "Inertia is resistance to acceleration."
                        )
                    ),
                    flashcards = listOf(
                        OriginalFlashcard(
                            id = "fc_1",
                            frontPrompt = "What is inertia?",
                            backAnswer = "Mass dependent resistance.",
                            conceptTag = "Mechanics"
                        )
                    ),
                    quizQuestions = listOf(
                        OriginalQuizQuestion(
                            id = "q_1",
                            question = "What measures inertia?",
                            options = listOf("Mass", "Velocity", "Force", "Energy"),
                            correctIndex = 0,
                            pedagogicalExplanation = "Mass is the quantitative measure of inertia."
                        )
                    )
                ),
                OriginalChapter(
                    chapterNumber = 2,
                    title = "Second Law & Momentum",
                    summary = "F = dp/dt and force balance."
                )
            ),
            publishedAt = System.currentTimeMillis()
        ),
        QuovexOriginalBook(
            id = "book_math_calculus",
            title = "Calculus from Zero to Hero",
            subtitle = "Limits, Derivatives and Integrals",
            description = "Visual intuitive calculus for Class 12.",
            subject = "Mathematics",
            topic = "Differential Calculus",
            countryRegion = "IN",
            curriculum = "JEE",
            gradeClass = "Class 12",
            chapterCount = 3,
            introduction = "The mathematics of change.",
            chapters = emptyList()
        )
    )

    override fun getPublishedOriginals(subject: String?, curriculum: String?): Flow<List<QuovexOriginalBook>> {
        var list = books.toList()
        if (!subject.isNullOrBlank() && subject != "All") {
            list = list.filter { it.subject.equals(subject, ignoreCase = true) }
        }
        if (!curriculum.isNullOrBlank() && curriculum != "All") {
            list = list.filter { it.curriculum.contains(curriculum, ignoreCase = true) }
        }
        return flowOf(list)
    }

    override fun getOriginalBookDetails(bookId: String): Flow<QuovexOriginalBook?> {
        return flowOf(books.find { it.id == bookId })
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OriginalsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeQuovexOriginalsRepository
    private lateinit var quovexRepository: FakeQuovexRepository
    private lateinit var prepareStudyAidsUseCase: PrepareOriginalChapterStudyAidsUseCase
    private lateinit var viewModel: OriginalsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeQuovexOriginalsRepository()
        quovexRepository = FakeQuovexRepository()
        val saveNoteUseCase = SaveNoteUseCase(quovexRepository)
        prepareStudyAidsUseCase = PrepareOriginalChapterStudyAidsUseCase(quovexRepository, saveNoteUseCase)
        viewModel = OriginalsViewModel(repository, prepareStudyAidsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialLoadAndFiltering() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.browserState.collect() }
        advanceUntilIdle()

        val state = viewModel.browserState.value
        assertEquals(false, state.isLoading)
        assertEquals(2, state.books.size)
        assertEquals(2, state.filteredBooks.size)
    }

    @Test
    fun testSubjectFilterSelection() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.browserState.collect() }
        advanceUntilIdle()

        viewModel.onSelectSubject("Physics")
        advanceUntilIdle()

        val state = viewModel.browserState.value
        assertEquals(1, state.filteredBooks.size)
        assertEquals("Newton's Laws — Made Simple", state.filteredBooks.first().title)
    }

    @Test
    fun testSearchQueryFiltering() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.browserState.collect() }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("calculus")
        advanceUntilIdle()

        val state = viewModel.browserState.value
        assertEquals(1, state.filteredBooks.size)
        assertEquals("Calculus from Zero to Hero", state.filteredBooks.first().title)
    }

    @Test
    fun testBookDetailLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.bookDetailState.collect() }
        advanceUntilIdle()

        viewModel.loadBookDetails("book_physics_newton")
        advanceUntilIdle()

        val detailState = viewModel.bookDetailState.value
        assertEquals(false, detailState.isLoading)
        assertNotNull(detailState.book)
        assertEquals("Newton's Laws — Made Simple", detailState.book?.title)
        assertEquals(2, detailState.book?.chapters?.size)
    }

    @Test
    fun testChapterReaderLoading() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.readerState.collect() }
        advanceUntilIdle()

        viewModel.loadChapterForReading("book_physics_newton", 1)
        advanceUntilIdle()

        val readerState = viewModel.readerState.value
        assertEquals(false, readerState.isLoading)
        assertNotNull(readerState.currentChapter)
        assertEquals("First Law & Inertia", readerState.currentChapter?.title)
        assertEquals(1, readerState.currentChapter?.sections?.size)
        assertEquals("Galileo to Newton", readerState.currentChapter?.sections?.first()?.title)
    }

    @Test
    fun testEmptyStateOnUnmatchedFilter() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.browserState.collect() }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Nonexistent Subject Quantum String Theory")
        advanceUntilIdle()

        val state = viewModel.browserState.value
        assertEquals(0, state.filteredBooks.size)
    }

    @Test
    fun testPrepareChapterQuizIngestion() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.readerState.collect() }
        advanceUntilIdle()

        viewModel.loadChapterForReading("book_physics_newton", 1)
        advanceUntilIdle()

        var resultingMaterialId = -1L
        viewModel.prepareChapterQuiz { materialId ->
            resultingMaterialId = materialId
        }
        advanceUntilIdle()

        assertTrue(resultingMaterialId > 0)
        val questionsInDb = quovexRepository.getQuizQuestionsList(resultingMaterialId)
        assertEquals(1, questionsInDb.size)
        assertEquals("What measures inertia?", questionsInDb.first().question)
    }

    @Test
    fun testPrepareChapterFlashcardsIngestion() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.readerState.collect() }
        advanceUntilIdle()

        viewModel.loadChapterForReading("book_physics_newton", 1)
        advanceUntilIdle()

        var resultingDeckId = -1L
        viewModel.prepareChapterFlashcards { deckId ->
            resultingDeckId = deckId
        }
        advanceUntilIdle()

        assertTrue(resultingDeckId > 0)
    }
}
