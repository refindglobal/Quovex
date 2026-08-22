package com.quovex.ui.notes

import com.quovex.data.storage.FirebaseStorageHelper
import com.quovex.domain.model.NoteItem
import com.quovex.domain.usecase.DeleteNoteUseCase
import com.quovex.domain.usecase.ExtractUrlAndSummarizeUseCase
import com.quovex.domain.usecase.FakeAIRepository
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.GetNotesUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import com.quovex.domain.usecase.SummarizeNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class NotesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeQuovexRepository
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var storageHelper: FirebaseStorageHelper
    private lateinit var getNotesUseCase: GetNotesUseCase
    private lateinit var saveNoteUseCase: SaveNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase
    private lateinit var summarizeNoteUseCase: SummarizeNoteUseCase
    private lateinit var extractUrlUseCase: ExtractUrlAndSummarizeUseCase
    private lateinit var getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeQuovexRepository()
        aiRepository = FakeAIRepository()
        storageHelper = FirebaseStorageHelper()

        getNotesUseCase = GetNotesUseCase(repository)
        saveNoteUseCase = SaveNoteUseCase(repository)
        deleteNoteUseCase = DeleteNoteUseCase(repository, storageHelper)
        summarizeNoteUseCase = SummarizeNoteUseCase(aiRepository)
        extractUrlUseCase = ExtractUrlAndSummarizeUseCase(aiRepository)
        getConfiguredSubjectsUseCase = GetConfiguredSubjectsUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createQuickNote creates note in repository and invokes callback`() = runTest {
        val viewModel = NotesViewModel(
            getNotesUseCase = getNotesUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            summarizeNoteUseCase = summarizeNoteUseCase,
            extractUrlAndSummarizeUseCase = extractUrlUseCase,
            getConfiguredSubjectsUseCase = getConfiguredSubjectsUseCase
        )
        advanceUntilIdle()

        var createdId: Long? = null
        viewModel.createQuickNote(
            title = "Newtonian Mechanics",
            subject = "Physics",
            content = "F=ma and conservation of momentum.",
            summarizeWithAi = false
        ) { id ->
            createdId = id
        }
        advanceUntilIdle()

        assertNotNull(createdId)
        val note = repository.getNoteById(createdId!!)
        assertEquals("Newtonian Mechanics", note?.title)
        assertEquals("Physics", note?.subject)
    }

    @Test
    fun `deleteNote removes note from repository`() = runTest {
        val note = NoteItem(id = 20L, title = "To Remove", subject = "Chemistry", content = "Test content")
        repository.insertNote(note)

        val viewModel = NotesViewModel(
            getNotesUseCase = getNotesUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            summarizeNoteUseCase = summarizeNoteUseCase,
            extractUrlAndSummarizeUseCase = extractUrlUseCase,
            getConfiguredSubjectsUseCase = getConfiguredSubjectsUseCase
        )
        advanceUntilIdle()

        viewModel.deleteNote(note)
        advanceUntilIdle()

        val retrieved = repository.getNoteById(20L)
        assertTrue(retrieved == null)
    }
}
