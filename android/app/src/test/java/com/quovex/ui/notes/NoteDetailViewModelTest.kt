package com.quovex.ui.notes

import androidx.lifecycle.SavedStateHandle
import com.quovex.data.storage.FirebaseStorageHelper
import com.quovex.domain.model.NoteItem
import com.quovex.domain.usecase.DeleteNoteUseCase
import com.quovex.domain.usecase.FakeAIRepository
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GenerateFlashcardsFromNoteUseCase
import com.quovex.domain.usecase.GetNoteByIdUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeQuovexRepository
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var storageHelper: FirebaseStorageHelper
    private lateinit var getNoteByIdUseCase: GetNoteByIdUseCase
    private lateinit var saveNoteUseCase: SaveNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase
    private lateinit var generateFlashcardsUseCase: GenerateFlashcardsFromNoteUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeQuovexRepository()
        aiRepository = FakeAIRepository()
        storageHelper = FirebaseStorageHelper()

        getNoteByIdUseCase = GetNoteByIdUseCase(repository)
        saveNoteUseCase = SaveNoteUseCase(repository)
        deleteNoteUseCase = DeleteNoteUseCase(repository, storageHelper)
        generateFlashcardsUseCase = GenerateFlashcardsFromNoteUseCase(aiRepository, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNote loads note from repository correctly`() = runTest {
        val sampleNote = NoteItem(
            id = 42L,
            title = "Quantum Physics Intro",
            subject = "Physics",
            content = "Wave-particle duality and Schrödinger equation."
        )
        repository.insertNote(sampleNote)

        val savedStateHandle = SavedStateHandle(mapOf("noteId" to 42L))
        val viewModel = NoteDetailViewModel(
            getNoteByIdUseCase = getNoteByIdUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            generateFlashcardsFromNoteUseCase = generateFlashcardsUseCase,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.note)
        assertEquals("Quantum Physics Intro", state.note?.title)
        assertEquals("Physics", state.note?.subject)
    }

    @Test
    fun `saveEdits updates note title and content`() = runTest {
        val sampleNote = NoteItem(
            id = 10L,
            title = "Organic Chem",
            subject = "Chemistry",
            content = "Aldehydes"
        )
        repository.insertNote(sampleNote)

        val savedStateHandle = SavedStateHandle(mapOf("noteId" to 10L))
        val viewModel = NoteDetailViewModel(
            getNoteByIdUseCase = getNoteByIdUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            generateFlashcardsFromNoteUseCase = generateFlashcardsUseCase,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.onEditTitleChange("Organic Chemistry - Aldehydes & Ketones")
        viewModel.onEditContentChange("Nucleophilic addition reactions.")
        viewModel.saveEdits()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEditing)
        assertEquals("Organic Chemistry - Aldehydes & Ketones", state.note?.title)
        assertEquals("Nucleophilic addition reactions.", state.note?.content)
    }

    @Test
    fun `deleteNote marks isDeleted as true`() = runTest {
        val sampleNote = NoteItem(id = 5L, title = "To Delete", subject = "Maths", content = "Test")
        repository.insertNote(sampleNote)

        val savedStateHandle = SavedStateHandle(mapOf("noteId" to 5L))
        val viewModel = NoteDetailViewModel(
            getNoteByIdUseCase = getNoteByIdUseCase,
            saveNoteUseCase = saveNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            generateFlashcardsFromNoteUseCase = generateFlashcardsUseCase,
            savedStateHandle = savedStateHandle
        )
        advanceUntilIdle()

        viewModel.deleteNote()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDeleted)
    }
}
