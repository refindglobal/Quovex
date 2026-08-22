package com.quovex.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.NoteItem
import com.quovex.domain.usecase.DeleteNoteUseCase
import com.quovex.domain.usecase.GenerateFlashcardsFromNoteUseCase
import com.quovex.domain.usecase.GetNoteByIdUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NoteDetailUiState(
    val note: NoteItem? = null,
    val isEditing: Boolean = false,
    val editTitle: String = "",
    val editSubject: String = "",
    val editContent: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isGeneratingFlashcards: Boolean = false,
    val flashcardsGeneratedCount: Int? = null,
    val error: String? = null,
    val isDeleted: Boolean = false
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val generateFlashcardsFromNoteUseCase: GenerateFlashcardsFromNoteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = when (val raw = savedStateHandle.get<Any>("noteId")) {
        is Long -> raw
        is Int -> raw.toLong()
        is String -> raw.toLongOrNull() ?: 0L
        else -> 0L
    }

    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    init {
        loadNote()
    }

    fun loadNote() {
        if (noteId == 0L) {
            _uiState.update { it.copy(isLoading = false, error = "Invalid Note ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val note = getNoteByIdUseCase(noteId)
            if (note != null) {
                _uiState.update {
                    it.copy(
                        note = note,
                        editTitle = note.title,
                        editSubject = note.subject,
                        editContent = note.content,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Note not found.") }
            }
        }
    }

    fun startEditing() {
        val currentNote = _uiState.value.note ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editTitle = currentNote.title,
                editSubject = currentNote.subject,
                editContent = currentNote.content,
                error = null
            )
        }
    }

    fun cancelEditing() {
        val currentNote = _uiState.value.note ?: return
        _uiState.update {
            it.copy(
                isEditing = false,
                editTitle = currentNote.title,
                editSubject = currentNote.subject,
                editContent = currentNote.content,
                error = null
            )
        }
    }

    fun onEditTitleChange(title: String) {
        _uiState.update { it.copy(editTitle = title) }
    }

    fun onEditSubjectChange(subject: String) {
        _uiState.update { it.copy(editSubject = subject) }
    }

    fun onEditContentChange(content: String) {
        _uiState.update { it.copy(editContent = content) }
    }

    fun saveEdits() {
        val currentNote = _uiState.value.note ?: return
        val newTitle = _uiState.value.editTitle.trim()
        val newContent = _uiState.value.editContent.trim()
        val newSubject = _uiState.value.editSubject.trim()

        if (newTitle.isBlank() || newContent.isBlank()) {
            _uiState.update { it.copy(error = "Title and content cannot be blank.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val updated = currentNote.copy(
                    title = newTitle,
                    subject = newSubject.ifBlank { currentNote.subject },
                    content = newContent,
                    updatedAt = System.currentTimeMillis()
                )
                saveNoteUseCase(updated)
                _uiState.update {
                    it.copy(
                        note = updated,
                        isEditing = false,
                        isSaving = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save edits."
                    )
                }
            }
        }
    }

    fun generateFlashcards() {
        val currentNote = _uiState.value.note ?: return
        if (_uiState.value.isGeneratingFlashcards) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingFlashcards = true, error = null, flashcardsGeneratedCount = null) }
            val result = generateFlashcardsFromNoteUseCase(currentNote)
            result.onSuccess { count ->
                _uiState.update {
                    it.copy(
                        isGeneratingFlashcards = false,
                        flashcardsGeneratedCount = count,
                        note = it.note?.copy(flashcardCount = (it.note?.flashcardCount ?: 0) + count)
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGeneratingFlashcards = false,
                        error = error.message ?: "Failed to generate flashcards."
                    )
                }
            }
        }
    }

    fun deleteNote() {
        val currentNote = _uiState.value.note ?: return
        viewModelScope.launch {
            try {
                deleteNoteUseCase(currentNote.id, currentNote.storageRef)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete note: ${e.message}") }
            }
        }
    }
}
