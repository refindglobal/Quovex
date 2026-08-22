package com.quovex.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.usecase.DeleteNoteUseCase
import com.quovex.domain.usecase.ExtractUrlAndSummarizeUseCase
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.GetNotesUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import com.quovex.domain.usecase.SummarizeNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val notes: List<NoteItem> = emptyList(),
    val availableSubjects: List<String> = listOf("Physics", "Chemistry", "Mathematics", "Biology"),
    val selectedSubject: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isCreatingNote: Boolean = false,
    val creationError: String? = null,
    val showCreateSheet: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val extractUrlAndSummarizeUseCase: ExtractUrlAndSummarizeUseCase,
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase
) : ViewModel() {

    private val _selectedSubject = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")
    private val _isCreatingNote = MutableStateFlow(false)
    private val _creationError = MutableStateFlow<String?>(null)
    private val _showCreateSheet = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _availableSubjects = MutableStateFlow(listOf("Physics", "Chemistry", "Mathematics", "Biology"))

    init {
        loadConfiguredSubjects()
    }

    private fun loadConfiguredSubjects() {
        viewModelScope.launch {
            getConfiguredSubjectsUseCase().collect { subjects ->
                if (subjects.isNotEmpty()) {
                    _availableSubjects.value = subjects
                }
            }
        }
    }

    val uiState: StateFlow<NotesUiState> = combine(
        _selectedSubject,
        _searchQuery,
        _isCreatingNote,
        _creationError,
        _showCreateSheet,
        _error,
        _availableSubjects
    ) { params ->
        val selectedSubject = params[0] as String
        val searchQuery = params[1] as String
        val isCreating = params[2] as Boolean
        val creationError = params[3] as String?
        val showSheet = params[4] as Boolean
        val error = params[5] as String?
        @Suppress("UNCHECKED_CAST")
        val availableSubjects = params[6] as List<String>

        NotesUiState(
            selectedSubject = selectedSubject,
            searchQuery = searchQuery,
            isCreatingNote = isCreating,
            creationError = creationError,
            showCreateSheet = showSheet,
            error = error,
            availableSubjects = availableSubjects
        )
    }.combine(getNotesUseCase()) { state, allNotes ->
        val filtered = allNotes.filter { note ->
            val matchesSubject = state.selectedSubject.equals("All", ignoreCase = true) ||
                    note.subject.equals(state.selectedSubject, ignoreCase = true)
            val matchesSearch = state.searchQuery.isBlank() ||
                    note.title.contains(state.searchQuery, ignoreCase = true) ||
                    note.content.contains(state.searchQuery, ignoreCase = true) ||
                    note.keyPoints.any { it.contains(state.searchQuery, ignoreCase = true) }
            matchesSubject && matchesSearch
        }
        state.copy(notes = filtered, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotesUiState(isLoading = true)
    )

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun openCreateSheet() {
        _creationError.value = null
        _showCreateSheet.value = true
    }

    fun closeCreateSheet() {
        _showCreateSheet.value = false
        _creationError.value = null
    }

    fun createQuickNote(
        title: String,
        subject: String,
        content: String,
        summarizeWithAi: Boolean = false,
        onSuccess: (noteId: Long) -> Unit
    ) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isBlank() || trimmedContent.isBlank()) {
            _creationError.value = "Title and content cannot be blank."
            return
        }

        viewModelScope.launch {
            _isCreatingNote.value = true
            _creationError.value = null

            try {
                var finalContent = trimmedContent
                var keyPoints = emptyList<String>()

                if (summarizeWithAi) {
                    val summaryResult = summarizeNoteUseCase(trimmedContent, subject)
                    summaryResult.onSuccess { aiData ->
                        if (aiData.summary.isNotBlank()) {
                            finalContent = aiData.summary
                        }
                        keyPoints = aiData.keyPoints
                    }
                }

                val newNote = NoteItem(
                    title = trimmedTitle,
                    subject = subject,
                    content = finalContent,
                    status = NoteProcessingStatus.READY,
                    inputType = NoteInputType.TEXT,
                    keyPoints = keyPoints
                )

                val noteId = saveNoteUseCase(newNote)
                _isCreatingNote.value = false
                _showCreateSheet.value = false
                onSuccess(noteId)
            } catch (e: Exception) {
                _isCreatingNote.value = false
                _creationError.value = e.message ?: "Failed to save note."
            }
        }
    }

    fun importUrlNote(
        url: String,
        subject: String,
        onSuccess: (noteId: Long) -> Unit
    ) {
        viewModelScope.launch {
            _isCreatingNote.value = true
            _creationError.value = null

            val result = extractUrlAndSummarizeUseCase(url, subject)
            result.onSuccess { urlData ->
                val newNote = NoteItem(
                    title = urlData.title,
                    subject = subject,
                    content = urlData.summaryResult.summary.ifBlank { urlData.extractedText.take(1500) },
                    status = NoteProcessingStatus.READY,
                    inputType = urlData.inputType,
                    sourceUrl = url.trim(),
                    keyPoints = urlData.summaryResult.keyPoints
                )

                val noteId = saveNoteUseCase(newNote)
                _isCreatingNote.value = false
                _showCreateSheet.value = false
                onSuccess(noteId)
            }.onFailure { error ->
                _isCreatingNote.value = false
                _creationError.value = error.message ?: "Failed to import content from URL."
            }
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            try {
                deleteNoteUseCase(note.id, note.storageRef)
            } catch (e: Exception) {
                _error.value = "Failed to delete note: ${e.message}"
            }
        }
    }
}
