package com.quovex.ui.flashcards

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.StudySession
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.GetDueFlashcardsUseCase
import com.quovex.domain.usecase.GetFlashcardsForDeckUseCase
import com.quovex.domain.usecase.ReviewCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Represents whether the current study session loads due-only or all cards. */
enum class StudyMode { DUE_ONLY, REVIEW_ALL }

data class FlashcardPlayerUiState(
    val deckId: Int = 0,
    val deckTitle: String = "",
    val studyMode: StudyMode = StudyMode.DUE_ONLY,
    val cards: List<FlashcardItem> = emptyList(),
    val currentCardIndex: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = true,
    val isSubmittingReview: Boolean = false,
    val isDeckComplete: Boolean = false,
    val error: String? = null,
    val session: StudySession = StudySession()
) {
    val currentCard: FlashcardItem?
        get() = cards.getOrNull(currentCardIndex)

    val progress: Float
        get() = if (cards.isNotEmpty()) {
            (currentCardIndex.toFloat() / cards.size).coerceIn(0f, 1f)
        } else 0f

    val progressText: String
        get() = if (cards.isNotEmpty()) "${currentCardIndex + 1} / ${cards.size}" else "0 / 0"

    val hasCards: Boolean get() = cards.isNotEmpty()
    val isNoDueCards: Boolean get() = !isLoading && cards.isEmpty() && !isDeckComplete
}

@HiltViewModel
class FlashcardPlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDueFlashcardsUseCase: GetDueFlashcardsUseCase,
    private val getFlashcardsForDeckUseCase: GetFlashcardsForDeckUseCase,
    private val reviewCardUseCase: ReviewCardUseCase,
    private val repository: QuovexRepository
) : ViewModel() {

    private val deckId: Int = when (val raw = savedStateHandle.get<Any>("deckId")) {
        is Int -> raw
        is String -> raw.toIntOrNull() ?: 0
        is Number -> raw.toInt()
        else -> 0
    }

    /** reviewAll=true → load all cards; reviewAll=false (default) → load only due cards. */
    private val reviewAll: Boolean = when (val raw = savedStateHandle.get<Any>("reviewAll")) {
        is Boolean -> raw
        is String -> raw.toBoolean()
        else -> false
    }

    private val _uiState = MutableStateFlow(
        FlashcardPlayerUiState(
            deckId = deckId,
            studyMode = if (reviewAll) StudyMode.REVIEW_ALL else StudyMode.DUE_ONLY
        )
    )
    val uiState: StateFlow<FlashcardPlayerUiState> = _uiState.asStateFlow()

    init {
        loadDeckTitle()
        loadCards()
    }

    private fun loadDeckTitle() {
        viewModelScope.launch {
            val deck = repository.getDeckById(deckId.toLong())
            _uiState.update { it.copy(deckTitle = deck?.title ?: "Flashcard Review") }
        }
    }

    /**
     * Loads either due-only or all cards depending on [reviewAll].
     * Takes a one-shot snapshot from the flow (first emission) so the card list
     * does not auto-change mid-session as Room updates propagate.
     */
    private fun loadCards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val now = System.currentTimeMillis()
                val cards = if (reviewAll) {
                    getFlashcardsForDeckUseCase(deckId.toLong()).first()
                } else {
                    getDueFlashcardsUseCase(deckId.toLong(), now).first()
                }
                _uiState.update { state ->
                    state.copy(
                        cards = cards,
                        currentCardIndex = 0,
                        isLoading = false,
                        isDeckComplete = false,
                        session = StudySession(totalCards = cards.size)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load flashcards") }
            }
        }
    }

    /** Toggles the card flip state. */
    fun flipCard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
    }

    /**
     * Records a review rating and advances to the next card.
     *
     * @param quality SM-2 quality rating (0=Again, 3=Hard, 4=Good, 5=Easy).
     *   The SM-2 algorithm in [ReviewCardUseCase] is the single source of truth
     *   for the next scheduled interval. The UI labels are approximations only.
     */
    fun submitReviewRating(quality: Int) {
        val currentCard = _uiState.value.currentCard ?: return
        if (_uiState.value.isSubmittingReview) return  // guard against double-tap

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true) }

            try {
                // Persist SM-2 result to Room via use case
                reviewCardUseCase(currentCard.id.toLong(), quality)

                val updatedSession = _uiState.value.session.recordReview(quality)
                val nextIndex = _uiState.value.currentCardIndex + 1
                val isComplete = nextIndex >= _uiState.value.cards.size

                _uiState.update { state ->
                    state.copy(
                        isFlipped = false,
                        currentCardIndex = nextIndex,
                        isDeckComplete = isComplete,
                        isSubmittingReview = false,
                        session = updatedSession
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmittingReview = false, error = "Failed to save review") }
            }
        }
    }

    /**
     * Restarts the session by re-fetching cards from Room.
     * Due-only mode will re-query so recently-reviewed cards that are no longer
     * due today will be excluded correctly.
     */
    fun restartSession() {
        _uiState.update {
            it.copy(
                currentCardIndex = 0,
                isFlipped = false,
                isDeckComplete = false,
                session = StudySession()
            )
        }
        loadCards()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
