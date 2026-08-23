package com.quovex.ui.ncert

import androidx.lifecycle.SavedStateHandle
import com.quovex.data.repository.PdfTextExtractor
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.DeckStats
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertCatalog
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.PdfLineBlock
import com.quovex.domain.model.PdfPageText
import com.quovex.domain.model.PdfRect
import com.quovex.domain.model.PdfTextBlock
import com.quovex.domain.model.PdfWordBlock
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.QuizResult
import com.quovex.domain.model.RecentActivityItem
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.NcertPdfCacheRepository
import com.quovex.domain.repository.NcertRepository
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class NcertPdfReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val sampleChapter = NcertChapter(
        id = "leph101",
        bookId = "leph1",
        bookCode = "leph1",
        bookTitle = "Physics Part I",
        chapterNumber = 1,
        chapterTitle = "Electric Charges and Fields",
        classLevel = 12,
        subject = "Physics",
        officialSourceUrl = "https://ncert.nic.in/textbook/pdf/leph101.pdf"
    )

    private val fakeNcertRepository = object : NcertRepository {
        override fun getCatalog(): Flow<NcertCatalog> = flowOf(com.quovex.domain.model.NcertCatalog())
        override fun getAvailableClasses(): Flow<List<Int>> = flowOf(listOf(12))
        override fun getSubjectsForClass(classLevel: Int): Flow<List<String>> = flowOf(listOf("Physics"))
        override fun getBooks(classLevel: Int?, subject: String?): Flow<List<NcertBook>> = flowOf(emptyList())
        override fun getBookById(bookId: String): Flow<NcertBook?> = flowOf(null)
        override fun getChaptersForBook(bookId: String): Flow<List<NcertChapter>> = flowOf(listOf(sampleChapter))
        override fun getChapterById(chapterId: String): Flow<NcertChapter?> = flowOf(sampleChapter)
        override suspend fun refreshCatalog(): Result<com.quovex.domain.model.NcertCatalog> = Result.success(com.quovex.domain.model.NcertCatalog())
    }

    private val fakePdfCacheRepository = object : NcertPdfCacheRepository {
        override fun getCachedPdfPath(chapterId: String): String? = "/fake/path/leph101.pdf"
        override suspend fun downloadPdfToCache(chapterId: String, url: String): Result<String> = Result.success("/fake/path/leph101.pdf")
        override suspend fun clearPdfCache(): Result<Unit> = Result.success(Unit)
    }

    private val fakeTextExtractor = object : PdfTextExtractor() {
        override suspend fun extractPageText(pdfFile: File, pageIndex: Int): Result<PdfPageText> {
            val word = PdfWordBlock("Electrostatics", 1, PdfRect(10f, 20f, 60f, 40f), 0, 0, 0)
            val line = PdfLineBlock("Electrostatics", 1, PdfRect(10f, 20f, 60f, 40f), 0, listOf(word))
            val block = PdfTextBlock("Electrostatics", 1, PdfRect(10f, 20f, 60f, 40f), listOf(line))
            return Result.success(PdfPageText(pageIndex + 1, 595f, 842f, listOf(block)))
        }
    }

    private val fakeQuovexRepository = object : QuovexRepository {
        val insertedNotes = mutableListOf<NoteItem>()
        val insertedDecks = mutableListOf<Pair<String, String>>()
        val insertedMaterials = mutableListOf<LearningMaterial>()

        override fun getMaterials(): Flow<List<LearningMaterial>> = flowOf(emptyList())
        override fun getMaterialsBySubject(subject: String): Flow<List<LearningMaterial>> = flowOf(emptyList())
        override suspend fun getMaterialById(id: Long): LearningMaterial? = null
        override suspend fun insertMaterial(material: LearningMaterial): Long {
            insertedMaterials.add(material)
            return 101L
        }
        override suspend fun updateMaterial(material: LearningMaterial): Int = 1
        override suspend fun deleteMaterial(id: Long): Int = 1
        override fun getDistinctMaterialSubjects(): Flow<List<String>> = flowOf(emptyList())

        override fun getNotes(): Flow<List<NoteItem>> = flowOf(emptyList())
        override fun getNotesBySubject(subject: String): Flow<List<NoteItem>> = flowOf(emptyList())
        override suspend fun getNoteById(id: Long): NoteItem? = null
        override suspend fun insertNote(note: NoteItem): Long {
            insertedNotes.add(note)
            return 42L
        }
        override suspend fun updateNote(note: NoteItem): Int = 1
        override suspend fun deleteNote(id: Long): Int = 1

        override fun getDecks(): Flow<List<DeckItem>> = flowOf(emptyList())
        override suspend fun getDeckById(deckId: Long): DeckItem? = null
        override suspend fun getDeckByMaterialId(materialId: Long): DeckItem? = null
        override suspend fun insertDeck(title: String, subject: String, sourceMaterialId: Long?): Long {
            insertedDecks.add(title to subject)
            return 77L
        }
        override suspend fun getMostRecentDeck(): DeckItem? = null
        override suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int = 0
        override fun getDeckStatsForAllDecks(currentTimeMillis: Long): Flow<List<DeckStats>> = flowOf(emptyList())
        override suspend fun getDeckStats(deckId: Int, currentTimeMillis: Long): DeckStats? = null

        override fun getDueFlashcards(deckId: Long, currentTimeMillis: Long): Flow<List<FlashcardItem>> = flowOf(emptyList())
        override fun getAllFlashcardsForDeck(deckId: Long): Flow<List<FlashcardItem>> = flowOf(emptyList())
        override suspend fun processCardReview(cardId: Long, quality: Int): FlashcardItem? = null
        override suspend fun insertFlashcard(deckId: Int, frontContent: String, backContent: String): Long = 1L
        override suspend fun insertFlashcards(deckId: Int, cards: List<Pair<String, String>>): List<Long> = listOf(1L)
        override suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int = 0
        override suspend fun createRemedialFlashcard(mistake: QuizMistake, deckId: Int): Long = 1L

        override suspend fun saveQuizQuestions(questions: List<QuizQuestion>): List<Long> = listOf(1L)
        override fun getQuizQuestionsForMaterial(materialId: Long): Flow<List<QuizQuestion>> = flowOf(emptyList())
        override suspend fun getQuizQuestionsList(materialId: Long): List<QuizQuestion> = emptyList()
        override suspend fun recordQuizResult(result: QuizResult): Long = 1L
        override fun getQuizResultsForMaterial(materialId: Long): Flow<List<QuizResult>> = flowOf(emptyList())
        override suspend fun getRecentMistakes(limit: Int): List<QuizMistake> = emptyList()

        override fun getRecentSessions(limit: Int): Flow<List<RecentActivityItem>> = flowOf(emptyList())
        override suspend fun getRecentSessionsList(limit: Int): List<RecentActivityItem> = emptyList()
        override suspend fun recordSession(startTime: Long, endTime: Long, durationMinutes: Int, focusScore: Int, appBlockViolations: Int, subject: String): Long = 1L
        override suspend fun getTodayFocusSeconds(): Long = 0L
        override suspend fun getTotalXp(): Long = 0L
        override suspend fun getWeeklySessionMinutes(startOfWeekMillis: Long, endOfWeekMillis: Long): Map<Int, Int> = emptyMap()

        override fun getActiveSessionState(): Flow<ActiveSessionState> = flowOf(ActiveSessionState(isActive = false, subject = ""))
        override fun updateActiveSessionState(isActive: Boolean, remainingSeconds: Int, totalSeconds: Int, subject: String) {}
    }

    private val fakeAiRepository = object : AIRepository {
        override suspend fun sendChatMessage(message: String, subject: String, history: List<com.quovex.data.remote.dto.ChatMessageDto>) = Result.success("AI response")
        override suspend fun sendTutorMessage(message: String, subject: String, topic: String, materialSummary: String?, recentMistakes: List<String>, history: List<com.quovex.data.remote.dto.ChatMessageDto>) = Result.success("Tutor response")
        override suspend fun sendMessageWithImage(imageInput: com.quovex.domain.model.DomainImageInput, message: String, subject: String, history: List<com.quovex.data.remote.dto.ChatMessageDto>) = Result.success("Image response")
        override suspend fun classifyMaterial(textSample: String, filename: String?) = Result.success(com.quovex.domain.model.SubjectInference(subject = "Physics", topic = "Mechanics", confidence = 0.9f))
        override suspend fun summarizeNote(rawText: String, subject: String) = Result.success(com.quovex.data.remote.dto.AiSummaryResult("Summary", listOf("Key1"), emptyList()))
        override suspend fun generateQuiz(subject: String, topic: String, difficulty: String, keyPoints: List<String>): Result<List<QuizQuestion>> = Result.success(emptyList())
        override suspend fun solveImageDoubt(imageInput: com.quovex.domain.model.DomainImageInput, subject: String, questionText: String) = Result.success(com.quovex.domain.model.ImageDoubtSolution("Solution text"))
        override suspend fun analyzeDocumentImages(pageImages: List<com.quovex.domain.model.DomainImageInput>, subjectHint: String) = Result.success(com.quovex.domain.model.ScannedDocumentOrganization(detectedSubject = "Physics", detectedStream = "Science", documentTitle = "Ch 1", chapters = emptyList()))
        override suspend fun extractUrlContent(url: String) = Result.success("Title" to "Content")
        override suspend fun generateStudyPlan(examName: String, targetHours: Int, subjects: List<String>, days: Int) = Result.success("Plan")
        override suspend fun getDailyQuote(streak: Int) = Result.success("Quote" to "Author")
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_loadsCachedChapter_andSetsLoadingState() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("leph101", state.chapter?.id)
        assertEquals(PdfLoadPhase.LOADING, state.loadPhase)
        assertEquals("/fake/path/leph101.pdf", state.localPdfPath)
        assertNotNull(state.nativePageText)
    }

    @Test
    fun onWordLongPressed_createsActiveSelection() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        viewModel.onWordLongPressed(0)

        val sel = viewModel.uiState.value.currentSelection
        assertNotNull(sel)
        assertEquals("Electrostatics", sel?.selectedText)
    }

    @Test
    fun addSelectionToNotes_savesNoteWithCitation() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        viewModel.onWordLongPressed(0)

        var createdNoteId: Long? = null
        viewModel.addSelectionToNotes { id -> createdNoteId = id }
        advanceUntilIdle()

        assertEquals(42L, createdNoteId)
        assertEquals(1, fakeQuovexRepository.insertedNotes.size)
        val note = fakeQuovexRepository.insertedNotes.first()
        assertTrue(note.content.contains("Electrostatics"))
        assertTrue(note.content.contains("Official NCERT Textbook Reference"))
    }

    @Test
    fun makeFlashcardsFromSelection_createsDeckAndCards() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()
        viewModel.onWordLongPressed(0)

        var createdDeckId: Long? = null
        viewModel.makeFlashcardsFromSelection { id -> createdDeckId = id }
        advanceUntilIdle()

        assertEquals(77L, createdDeckId)
        assertEquals(1, fakeQuovexRepository.insertedDecks.size)
    }

    @Test
    fun makeQuizFromSelection_createsMaterialAndQuizQuestions() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()
        viewModel.onWordLongPressed(0)

        var createdMaterialId: Long? = null
        viewModel.makeQuizFromSelection { id -> createdMaterialId = id }
        advanceUntilIdle()

        assertEquals(101L, createdMaterialId)
        assertEquals(1, fakeQuovexRepository.insertedMaterials.size)
    }

    @Test
    fun askAiAboutSelection_triggersTutorQuery_andPopulatesAiResultSheet() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()
        viewModel.onWordLongPressed(0)

        viewModel.askAiAboutSelection("explain")
        assertTrue(viewModel.uiState.value.isAiResultSheetVisible)
        assertTrue(viewModel.uiState.value.isAiResponseLoading)

        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isAiResponseLoading)
        assertEquals("Tutor response", viewModel.uiState.value.aiResponseText)
        assertEquals("Explain Concept", viewModel.uiState.value.aiPromptTitle)
    }

    @Test
    fun saveAiExplanationAsNote_persistsNoteWithCitation() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("chapterId" to "leph101"))

        val viewModel = NcertPdfReaderViewModel(
            ncertRepository = fakeNcertRepository,
            pdfCacheRepository = fakePdfCacheRepository,
            pdfTextExtractor = fakeTextExtractor,
            quovexRepository = fakeQuovexRepository,
            aiRepository = fakeAiRepository,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()
        viewModel.onWordLongPressed(0)
        viewModel.askAiAboutSelection("explain")
        advanceUntilIdle()

        fakeQuovexRepository.insertedNotes.clear()
        viewModel.saveAiExplanationAsNote()
        advanceUntilIdle()

        assertEquals(1, fakeQuovexRepository.insertedNotes.size)
        val note = fakeQuovexRepository.insertedNotes.first()
        assertTrue(note.content.contains("Tutor response"))
        assertTrue(note.content.contains("Official NCERT Reference"))
    }
}
