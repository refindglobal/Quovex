package com.quovex.ui.scanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.ocr.MlKitOcrHelper
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import com.quovex.domain.usecase.SummarizeNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DocumentScannerUiState(
    val extractedText: String = "",
    val noteTitle: String = "",
    val selectedSubject: String = "Physics",
    val availableSubjects: List<String> = listOf("Physics", "Chemistry", "Mathematics", "Biology"),
    val isScanning: Boolean = false,
    val isSaving: Boolean = false,
    val isSummarizing: Boolean = false,
    val error: String? = null,
    val savedNoteId: Long? = null
)

@HiltViewModel
class DocumentScannerViewModel @Inject constructor(
    private val ocrHelper: MlKitOcrHelper,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentScannerUiState())
    val uiState: StateFlow<DocumentScannerUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            getConfiguredSubjectsUseCase().collect { subjects ->
                if (subjects.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            availableSubjects = subjects,
                            selectedSubject = subjects.first()
                        )
                    }
                }
            }
        }
    }

    fun processCapturedImage(bitmap: Bitmap, rotationDegrees: Int = 0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, error = null) }
            val result = ocrHelper.extractTextFromBitmap(bitmap, rotationDegrees)
            result.onSuccess { text ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        extractedText = text,
                        noteTitle = if (it.noteTitle.isBlank()) "Scanned Note - ${it.selectedSubject}" else it.noteTitle
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        error = error.message ?: "Failed to extract text from document."
                    )
                }
            }
        }
    }

    fun onTextChanged(text: String) {
        _uiState.update { it.copy(extractedText = text) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(noteTitle = title) }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun saveAsNote(onSuccess: (noteId: Long) -> Unit) {
        val state = _uiState.value
        val text = state.extractedText.trim()
        val title = state.noteTitle.trim().ifBlank { "Scanned Note - ${state.selectedSubject}" }

        if (text.isBlank()) {
            _uiState.update { it.copy(error = "Extracted text cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val note = NoteItem(
                    title = title,
                    subject = state.selectedSubject,
                    content = text,
                    status = NoteProcessingStatus.READY,
                    inputType = NoteInputType.SCAN
                )
                val noteId = saveNoteUseCase(note)
                _uiState.update { it.copy(isSaving = false, savedNoteId = noteId) }
                onSuccess(noteId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save note.") }
            }
        }
    }

    fun summarizeAndSave(onSuccess: (noteId: Long) -> Unit) {
        val state = _uiState.value
        val text = state.extractedText.trim()
        val title = state.noteTitle.trim().ifBlank { "AI Scanned Note - ${state.selectedSubject}" }

        if (text.isBlank()) {
            _uiState.update { it.copy(error = "Extracted text cannot be empty.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true, error = null) }
            val result = summarizeNoteUseCase(text, state.selectedSubject)
            result.onSuccess { aiSummary ->
                val note = NoteItem(
                    title = title,
                    subject = state.selectedSubject,
                    content = aiSummary.summary.ifBlank { text },
                    keyPoints = aiSummary.keyPoints,
                    status = NoteProcessingStatus.READY,
                    inputType = NoteInputType.SCAN
                )
                val noteId = saveNoteUseCase(note)
                _uiState.update { it.copy(isSummarizing = false, savedNoteId = noteId) }
                onSuccess(noteId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSummarizing = false,
                        error = error.message ?: "AI summarization failed. You can still save raw text."
                    )
                }
            }
        }
    }
}
