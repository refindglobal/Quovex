package com.quovex.ui.decks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.DeckStats
import com.quovex.domain.usecase.GetDeckStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DeckOverviewUiState(
    val stats: DeckStats? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /** True when stats are loaded and due cards exist. */
    val canStudyDue: Boolean
        get() = stats != null && stats.dueCards > 0

    /** True when stats are loaded and the deck has at least one card. */
    val canReviewAll: Boolean
        get() = stats != null && stats.totalCards > 0

    /** True when the deck has cards but none are due today. */
    val isAllCaughtUp: Boolean
        get() = stats != null && stats.dueCards == 0 && stats.totalCards > 0

    /** True when the deck has no cards at all. */
    val isDeckEmpty: Boolean
        get() = stats != null && stats.totalCards == 0
}

@HiltViewModel
class DeckOverviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDeckStatsUseCase: GetDeckStatsUseCase
) : ViewModel() {

    private val deckId: Int = when (val raw = savedStateHandle.get<Any>("deckId")) {
        is Int -> raw
        is String -> raw.toIntOrNull() ?: 0
        is Number -> raw.toInt()
        else -> 0
    }

    private val _uiState = MutableStateFlow(DeckOverviewUiState())
    val uiState: StateFlow<DeckOverviewUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    /**
     * Loads aggregated deck stats. Safe to call for refresh (e.g., on back navigation
     * from FlashcardPlayerScreen when cards may have been reviewed).
     */
    fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val stats = getDeckStatsUseCase(deckId)
                _uiState.update { it.copy(stats = stats, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load deck information") }
            }
        }
    }
}
