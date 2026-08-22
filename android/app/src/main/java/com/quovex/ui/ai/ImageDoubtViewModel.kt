package com.quovex.ui.ai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.ocr.MlKitOcrHelper
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import com.quovex.domain.usecase.SolveImageDoubtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class ImageDoubtUiState(
    val capturedBitmap: Bitmap? = null,
    val selectedSubject: String = "Physics",
    val availableSubjects: List<String> = listOf("Physics", "Chemistry", "Mathematics", "Biology", "General"),
    val questionText: String = "",
    val isSolving: Boolean = false,
    val isSavingAsNote: Boolean = false,
    val isCreatingFlashcards: Boolean = false,
    val solutionText: String? = null,
    val solutionProvider: String? = null,
    val savedNoteId: Long? = null,
    val flashcardsCreatedMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ImageDoubtViewModel @Inject constructor(
    private val solveImageDoubtUseCase: SolveImageDoubtUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val repository: QuovexRepository,
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase,
    private val mlKitOcrHelper: MlKitOcrHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageDoubtUiState())
    val uiState: StateFlow<ImageDoubtUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            getConfiguredSubjectsUseCase().collect { subjects ->
                if (subjects.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            availableSubjects = subjects + "General",
                            selectedSubject = subjects.first()
                        )
                    }
                }
            }
        }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                capturedBitmap = bitmap,
                solutionText = null,
                error = null,
                savedNoteId = null,
                flashcardsCreatedMessage = null
            )
        }
    }

    fun onQuestionTextChanged(text: String) {
        _uiState.update { it.copy(questionText = text) }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun clearImage() {
        _uiState.update {
            it.copy(
                capturedBitmap = null,
                solutionText = null,
                error = null,
                savedNoteId = null
            )
        }
    }

    fun solveDoubt() {
        val bitmap = _uiState.value.capturedBitmap ?: run {
            _uiState.update { it.copy(error = "Please capture or choose a problem photo first.") }
            return
        }

        if (_uiState.value.isSolving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSolving = true, error = null) }

            // Extract text from image on-device via ML Kit
            val ocrText = mlKitOcrHelper.extractTextFromBitmap(bitmap).getOrDefault("").trim()
            val userText = _uiState.value.questionText.trim()
            val queryText = buildString {
                if (userText.isNotBlank()) {
                    append(userText)
                }
                if (ocrText.isNotBlank()) {
                    if (isNotEmpty()) append("\n\n")
                    append("Extracted from image:\n$ocrText")
                }
            }.ifBlank { "Please explain and solve the concepts associated with this study material." }

            // Compress image to JPEG <= 512KB
            val bytes = compressBitmapToBytes(bitmap, maxKb = 512)
            val domainImage = DomainImageInput(bytes = bytes, mimeType = "image/jpeg")

            val result = solveImageDoubtUseCase(
                imageInput = domainImage,
                subject = _uiState.value.selectedSubject,
                questionText = queryText
            )

            result.onSuccess { solution ->
                _uiState.update {
                    it.copy(
                        isSolving = false,
                        solutionText = solution.solution,
                        solutionProvider = solution.provider
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSolving = false,
                        error = error.message ?: "Failed to solve problem image. Please try again."
                    )
                }
            }
        }
    }

    fun saveSolutionAsNote(onSuccess: (noteId: Long) -> Unit) {
        val solution = _uiState.value.solutionText ?: return
        val subject = _uiState.value.selectedSubject
        val question = _uiState.value.questionText.ifBlank { "Problem Solution" }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAsNote = true, error = null) }
            try {
                val note = NoteItem(
                    title = "Doubt Solved: $question",
                    subject = subject,
                    content = solution,
                    status = NoteProcessingStatus.READY,
                    inputType = NoteInputType.SCAN
                )
                val noteId = saveNoteUseCase(note)
                _uiState.update { it.copy(isSavingAsNote = false, savedNoteId = noteId) }
                onSuccess(noteId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingAsNote = false, error = e.message ?: "Failed to save note.") }
            }
        }
    }

    fun createFlashcardDeck() {
        val solution = _uiState.value.solutionText ?: return
        val subject = _uiState.value.selectedSubject

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingFlashcards = true, error = null) }
            try {
                val deckId = repository.insertDeck(
                    title = "AI Doubt - $subject",
                    subject = subject
                )
                repository.insertFlashcard(
                    deckId = deckId.toInt(),
                    frontContent = "Problem Concept ($subject)",
                    backContent = solution.take(800)
                )
                _uiState.update {
                    it.copy(
                        isCreatingFlashcards = false,
                        flashcardsCreatedMessage = "Created 1 card in Flashcards Library!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingFlashcards = false,
                        error = e.message ?: "Failed to create flashcard deck."
                    )
                }
            }
        }
    }

    private fun compressBitmapToBytes(bitmap: Bitmap, maxKb: Int = 512): ByteArray {
        var quality = 90
        var stream: ByteArrayOutputStream
        do {
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            quality -= 10
        } while (stream.size() > maxKb * 1024 && quality > 20)
        return stream.toByteArray()
    }
}
