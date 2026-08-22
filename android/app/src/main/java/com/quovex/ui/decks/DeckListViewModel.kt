package com.quovex.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.R
import com.quovex.domain.model.DeckStats
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.GetDeckStatsForAllDecksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** UI model for one deck card in the Library list. */
data class DeckUiModel(
    val id: Int,
    val title: String,
    val subject: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val masteryPercent: Int,
    val xpValue: Int,
    val bgResId: Int
)

data class DeckListUiState(
    val decks: List<DeckUiModel> = emptyList(),
    val selectedCategory: String = "All",
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredDecks: List<DeckUiModel>
        get() = if (selectedCategory == "All") decks
        else decks.filter { it.subject.equals(selectedCategory, ignoreCase = true) }

    /** True when all decks have zero due cards (but at least one deck exists). */
    val isAllCaughtUp: Boolean
        get() = decks.isNotEmpty() && decks.all { it.dueCards == 0 }
}

@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val getDeckStatsForAllDecksUseCase: GetDeckStatsForAllDecksUseCase,
    private val repository: QuovexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckListUiState())
    val uiState: StateFlow<DeckListUiState> = _uiState.asStateFlow()

    init {
        loadDecks()
    }

    /**
     * Subscribes to aggregated deck stats from a single SQL query.
     * No N+1 per-deck queries — the DAO uses LEFT JOIN + conditional aggregation.
     */
    private fun loadDecks() {
        viewModelScope.launch {
            try {
                getDeckStatsForAllDecksUseCase().collect { deckStatsList ->
                    val uiModels = deckStatsList.map { stats -> stats.toUiModel() }
                    _uiState.update {
                        it.copy(decks = uiModels, isLoading = false, error = null)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load decks") }
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun createDeck(title: String, subject: String) {
        viewModelScope.launch {
            try {
                repository.insertDeck(title, subject)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to create deck") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun DeckStats.toUiModel(): DeckUiModel {
        val bgRes = when (subject.lowercase()) {
            "physics" -> R.drawable.deck_physics_bg
            "chemistry" -> R.drawable.deck_chemistry_bg
            "maths", "mathematics" -> R.drawable.deck_maths_bg
            "biology" -> R.drawable.deck_biology_bg
            "history" -> R.drawable.deck_history_bg
            else -> R.drawable.deck_physics_bg
        }
        return DeckUiModel(
            id = deckId,
            title = title,
            subject = subject,
            totalCards = totalCards,
            dueCards = dueCards,
            masteredCards = masteredCards,
            masteryPercent = masteryPercent,
            xpValue = xpValue,
            bgResId = bgRes
        )
    }
}
