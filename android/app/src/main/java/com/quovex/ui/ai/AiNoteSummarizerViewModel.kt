package com.quovex.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.SummarizeNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiNoteSummarizerUiState(
    val inputText: String = "",
    val selectedSubject: String = "Physics",
    val isProcessing: Boolean = false,
    val summaryResult: AiSummaryResult? = null,
    val selectedTab: Int = 0, // 0 = Summary, 1 = Key Points, 2 = Flashcards
    val savedFlashcardsMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AiNoteSummarizerViewModel @Inject constructor(
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val repository: QuovexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiNoteSummarizerUiState())
    val uiState: StateFlow<AiNoteSummarizerUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text, savedFlashcardsMessage = null) }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun summarizeNote() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isProcessing) return

        _uiState.update { it.copy(isProcessing = true, error = null, savedFlashcardsMessage = null) }

        viewModelScope.launch {
            val result = summarizeNoteUseCase(
                rawText = text,
                subject = _uiState.value.selectedSubject
            )

            result.onSuccess { summaryResult ->
                _uiState.update {
                    it.copy(
                        summaryResult = summaryResult,
                        isProcessing = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = error.message ?: "Failed to generate AI notes."
                    )
                }
            }
        }
    }

    fun saveFlashcardsToDeck(subject: String = _uiState.value.selectedSubject) {
        val flashcards = _uiState.value.summaryResult?.flashcards ?: return
        if (flashcards.isEmpty()) return

        viewModelScope.launch {
            try {
                val deckId = repository.insertDeck(
                    title = "AI Notes - $subject",
                    subject = subject
                )
                val pairs = flashcards.map { card ->
                    card.question to "${card.answer}${if (!card.formula.isNullOrBlank()) "\n\nFormula: ${card.formula}" else ""}"
                }
                var count = 0
                pairs.forEach { (q, a) ->
                    repository.insertFlashcard(deckId = deckId.toInt(), frontContent = q, backContent = a)
                    count++
                }
                _uiState.update {
                    it.copy(savedFlashcardsMessage = "Successfully saved $count flashcards to your deck!")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(savedFlashcardsMessage = "Failed to save flashcards: ${e.message}")
                }
            }
        }
    }
}
