package com.quovex.ui.ncert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.repository.NcertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NcertBookDetailUiState(
    val book: NcertBook? = null,
    val chapters: List<NcertChapter> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NcertBookDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    ncertRepository: NcertRepository
) : ViewModel() {

    private val bookId: String = checkNotNull(savedStateHandle["bookId"])

    val uiState: StateFlow<NcertBookDetailUiState> = combine(
        ncertRepository.getBookById(bookId),
        ncertRepository.getChaptersForBook(bookId)
    ) { book, chapters ->
        if (book == null) {
            NcertBookDetailUiState(error = "Textbook not found in NCERT catalog.")
        } else {
            NcertBookDetailUiState(
                book = book,
                chapters = chapters,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NcertBookDetailUiState(isLoading = true)
    )
}
