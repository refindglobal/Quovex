package com.quovex.domain.usecase

import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for offline behavior:
 * - Notes cached in Room remain readable without network
 * - Text notes saved as DRAFT remain locally available
 * - [DeleteNoteUseCase] removes notes from local store
 * - [SummarizeNoteUseCase] is a pure AI operation (no repository writes)
 */
class NoteOfflineBehaviorTest {

    private lateinit var repository: FakeQuovexRepository
    private lateinit var aiRepository: FakeAIRepository

    @Before
    fun setup() {
        repository = FakeQuovexRepository()
        aiRepository = FakeAIRepository()
    }

    @Test
    fun `cached notes remain readable from Room cache without network`() = runTest {
        val note1 = NoteItem(id = 1L, title = "Photosynthesis", subject = "Biology", content = "Light reactions and Calvin cycle")
        val note2 = NoteItem(id = 2L, title = "Mitosis Phases", subject = "Biology", content = "Prophase, Metaphase, Anaphase, Telophase")
        repository.insertNote(note1)
        repository.insertNote(note2)

        val getNotesUseCase = GetNotesUseCase(repository)
        val notes = getNotesUseCase(selectedSubject = "All").first()
        assertEquals(2, notes.size)
    }

    @Test
    fun `text note created offline is stored as DRAFT in Room`() = runTest {
        val saveUseCase = SaveNoteUseCase(repository)
        val draftNote = NoteItem(
            title = "Offline Quick Note",
            subject = "Maths",
            content = "Integral of x^2 is x^3/3 + C",
            status = NoteProcessingStatus.DRAFT,
            inputType = NoteInputType.TEXT
        )

        val id = saveUseCase(draftNote)
        assertTrue(id > 0L)

        val saved = repository.getNoteById(id)
        assertEquals(NoteProcessingStatus.DRAFT, saved?.status)
        assertEquals("Offline Quick Note", saved?.title)
    }

    @Test
    fun `DeleteNoteUseCase removes note from local repository`() = runTest {
        val note = NoteItem(id = 7L, title = "To be deleted", subject = "History", content = "World War I causes")
        repository.insertNote(note)

        val before = repository.getNoteById(7L)
        assertEquals("To be deleted", before?.title)

        val deleteUseCase = DeleteNoteUseCase(repository, FakeFirebaseStorageHelper())
        val deletedCount = deleteUseCase(7L, null)
        assertEquals(1, deletedCount)

        val after = repository.getNoteById(7L)
        assertNull(after)
    }

    @Test
    fun `DeleteNoteUseCase returns 0 for non-existent note`() = runTest {
        val deleteUseCase = DeleteNoteUseCase(repository, FakeFirebaseStorageHelper())
        val result = deleteUseCase(999L, null)
        assertEquals(0, result)
    }

    @Test
    fun `SummarizeNoteUseCase is a pure AI call and does not touch repository`() = runTest {
        val summarizeUseCase = SummarizeNoteUseCase(aiRepository)
        val result = summarizeUseCase("Some raw note text about photosynthesis", "Biology")

        assertTrue(result.isSuccess)
        assertTrue(repository.notesMap.isEmpty())
    }

    @Test
    fun `SummarizeNoteUseCase propagates AI failure cleanly`() = runTest {
        aiRepository.shouldFailSummarize = true
        val summarizeUseCase = SummarizeNoteUseCase(aiRepository)
        val result = summarizeUseCase("Some raw note text", "Physics")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Summarization") == true)
    }

    @Test
    fun `GetNotesUseCase returns empty list when no notes cached`() = runTest {
        val getNotesUseCase = GetNotesUseCase(repository)
        val notes = getNotesUseCase(selectedSubject = "All").first()
        assertTrue(notes.isEmpty())
    }
}
