package com.quovex.domain.usecase

import com.quovex.data.remote.dto.GeneratedFlashcardDto
import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteUseCasesTest {

    private lateinit var repository: FakeQuovexRepository
    private lateinit var aiRepository: FakeAIRepository

    @Before
    fun setup() {
        repository = FakeQuovexRepository()
        aiRepository = FakeAIRepository()
    }

    @Test
    fun `SaveNoteUseCase inserts new note and assigns id`() = runTest {
        val saveUseCase = SaveNoteUseCase(repository)
        val note = NoteItem(
            title = "Newton's Laws",
            subject = "Physics",
            content = "Three physical laws that form the foundation of classical mechanics.",
            keyPoints = listOf("Inertia", "F=ma", "Action-Reaction")
        )

        val generatedId = saveUseCase(note)
        assertTrue(generatedId > 0L)

        val retrieved = repository.getNoteById(generatedId)
        assertNotNull(retrieved)
        assertEquals("Newton's Laws", retrieved?.title)
        assertEquals("Physics", retrieved?.subject)
        assertEquals(3, retrieved?.keyPoints?.size)
    }

    @Test
    fun `SaveNoteUseCase updates existing note`() = runTest {
        val saveUseCase = SaveNoteUseCase(repository)
        val note = NoteItem(
            id = 5L,
            title = "Thermodynamics",
            subject = "Physics",
            content = "First law: delta U = Q - W"
        )
        repository.insertNote(note)

        val updatedNote = note.copy(content = "First and Second Laws of Thermodynamics")
        saveUseCase(updatedNote)

        val retrieved = repository.getNoteById(5L)
        assertEquals("First and Second Laws of Thermodynamics", retrieved?.content)
    }

    @Test
    fun `GetNotesUseCase filters by subject and search query`() = runTest {
        val note1 = NoteItem(id = 1L, title = "Wave Optics", subject = "Physics", content = "Interference and diffraction")
        val note2 = NoteItem(id = 2L, title = "Electrochemistry", subject = "Chemistry", content = "Nernst equation and galvanic cells")
        val note3 = NoteItem(id = 3L, title = "Rotational Motion", subject = "Physics", content = "Moment of inertia and torque")
        repository.insertNote(note1)
        repository.insertNote(note2)
        repository.insertNote(note3)

        val getNotesUseCase = GetNotesUseCase(repository)

        // All subjects
        val allNotes = getNotesUseCase(selectedSubject = "All").first()
        assertEquals(3, allNotes.size)

        // Filter Physics
        val physicsNotes = getNotesUseCase(selectedSubject = "Physics").first()
        assertEquals(2, physicsNotes.size)

        // Search query "Nernst"
        val searchResults = getNotesUseCase(selectedSubject = "All", searchQuery = "Nernst").first()
        assertEquals(1, searchResults.size)
        assertEquals("Electrochemistry", searchResults[0].title)
    }

    @Test
    fun `GenerateFlashcardsFromNoteUseCase generates deck and updates note flashcardCount`() = runTest {
        val note = NoteItem(
            id = 10L,
            title = "Cell Division",
            subject = "Biology",
            content = "Mitosis and Meiosis phases.",
            flashcardCount = 0
        )
        repository.insertNote(note)

        aiRepository.summarizeResult = Result.success(
            AiSummaryResult(
                summary = "Cell division summary",
                keyPoints = listOf("Prophase", "Metaphase", "Anaphase", "Telophase"),
                flashcards = listOf(
                    GeneratedFlashcardDto(question = "What is Mitosis?", answer = "Equational division"),
                    GeneratedFlashcardDto(question = "What is Meiosis?", answer = "Reduction division")
                )
            )
        )

        val generateUseCase = GenerateFlashcardsFromNoteUseCase(aiRepository, repository)
        val result = generateUseCase(note)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())

        // Verify note flashcardCount was incremented in repository
        val updatedNote = repository.getNoteById(10L)
        assertEquals(2, updatedNote?.flashcardCount)
    }

    @Test
    fun `ExtractUrlAndSummarizeUseCase rejects invalid url`() = runTest {
        val extractUseCase = ExtractUrlAndSummarizeUseCase(aiRepository)
        val result = extractUseCase("ftp://invalid-url.com")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("http") == true)
    }

    @Test
    fun `ExtractUrlAndSummarizeUseCase handles YouTube and Web URLs correctly`() = runTest {
        val extractUseCase = ExtractUrlAndSummarizeUseCase(aiRepository)

        // Regular URL
        val webResult = extractUseCase("https://en.wikipedia.org/wiki/Photosynthesis", "Biology")
        assertTrue(webResult.isSuccess)
        assertEquals(NoteInputType.URL, webResult.getOrNull()?.inputType)

        // YouTube URL
        val ytResult = extractUseCase("https://youtube.com/watch?v=12345", "Physics")
        assertTrue(ytResult.isSuccess)
        assertEquals(NoteInputType.YOUTUBE, ytResult.getOrNull()?.inputType)
    }
}
