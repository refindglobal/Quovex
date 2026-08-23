package com.quovex.ui.originals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.originals.OriginalChapter
import com.quovex.domain.model.originals.QuovexOriginalBook
import com.quovex.domain.repository.QuovexOriginalsRepository
import com.quovex.domain.usecase.PrepareOriginalChapterStudyAidsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OriginalsBrowserUiState(
    val isLoading: Boolean = true,
    val books: List<QuovexOriginalBook> = emptyList(),
    val filteredBooks: List<QuovexOriginalBook> = emptyList(),
    val searchQuery: String = "",
    val selectedRegion: String = "All",
    val selectedCurriculum: String = "All",
    val selectedClass: String = "All",
    val selectedSubject: String = "All",
    val availableSubjects: List<String> = listOf("All", "Physics", "Chemistry", "Mathematics", "Biology"),
    val availableCurriculums: List<String> = listOf("All", "CBSE", "JEE", "NEET", "AP", "IB", "A-Level"),
    val availableClasses: List<String> = listOf("All", "Class 9", "Class 10", "Class 11", "Class 12"),
    val errorMessage: String? = null
)

data class OriginalBookDetailUiState(
    val isLoading: Boolean = true,
    val book: QuovexOriginalBook? = null,
    val errorMessage: String? = null
)

data class OriginalChapterReaderUiState(
    val isLoading: Boolean = true,
    val book: QuovexOriginalBook? = null,
    val currentChapter: OriginalChapter? = null,
    val selectedSectionIndex: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class OriginalsViewModel @Inject constructor(
    private val repository: QuovexOriginalsRepository,
    private val prepareOriginalChapterStudyAidsUseCase: PrepareOriginalChapterStudyAidsUseCase
) : ViewModel() {

    private val _browserState = MutableStateFlow(OriginalsBrowserUiState())
    val browserState: StateFlow<OriginalsBrowserUiState> = _browserState.asStateFlow()

    private val _bookDetailState = MutableStateFlow(OriginalBookDetailUiState())
    val bookDetailState: StateFlow<OriginalBookDetailUiState> = _bookDetailState.asStateFlow()

    private val _readerState = MutableStateFlow(OriginalChapterReaderUiState())
    val readerState: StateFlow<OriginalChapterReaderUiState> = _readerState.asStateFlow()

    init {
        loadPublishedOriginals()
    }

    fun loadPublishedOriginals() {
        _browserState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getPublishedOriginals()
                .catch { e ->
                    _browserState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Failed to load Quovex Originals"
                        )
                    }
                }
                .collect { books ->
                    _browserState.update { state ->
                        state.copy(
                            isLoading = false,
                            books = books,
                            filteredBooks = applyFilters(
                                books,
                                state.searchQuery,
                                state.selectedCurriculum,
                                state.selectedClass,
                                state.selectedSubject
                            )
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _browserState.update { state ->
            state.copy(
                searchQuery = query,
                filteredBooks = applyFilters(
                    state.books,
                    query,
                    state.selectedCurriculum,
                    state.selectedClass,
                    state.selectedSubject
                )
            )
        }
    }

    fun onSelectSubject(subject: String) {
        _browserState.update { state ->
            state.copy(
                selectedSubject = subject,
                filteredBooks = applyFilters(
                    state.books,
                    state.searchQuery,
                    state.selectedCurriculum,
                    state.selectedClass,
                    subject
                )
            )
        }
    }

    fun onSelectCurriculum(curriculum: String) {
        _browserState.update { state ->
            state.copy(
                selectedCurriculum = curriculum,
                filteredBooks = applyFilters(
                    state.books,
                    state.searchQuery,
                    curriculum,
                    state.selectedClass,
                    state.selectedSubject
                )
            )
        }
    }

    fun onSelectClass(gradeClass: String) {
        _browserState.update { state ->
            state.copy(
                selectedClass = gradeClass,
                filteredBooks = applyFilters(
                    state.books,
                    state.searchQuery,
                    state.selectedCurriculum,
                    gradeClass,
                    state.selectedSubject
                )
            )
        }
    }

    fun loadBookDetails(bookId: String) {
        _bookDetailState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getOriginalBookDetails(bookId)
                .catch { e ->
                    _bookDetailState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Failed to load book details"
                        )
                    }
                }
                .collect { book ->
                    _bookDetailState.update {
                        it.copy(
                            isLoading = false,
                            book = book,
                            errorMessage = if (book == null) "Book not found or not published" else null
                        )
                    }
                }
        }
    }

    fun loadChapterForReading(bookId: String, chapterNumber: Int) {
        _readerState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getOriginalBookDetails(bookId)
                .catch { e ->
                    _readerState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "Failed to load chapter content"
                        )
                    }
                }
                .collect { book ->
                    if (book != null) {
                        val chapter = book.chapters.find { it.chapterNumber == chapterNumber }
                            ?: book.chapters.firstOrNull()
                        _readerState.update {
                            it.copy(
                                isLoading = false,
                                book = book,
                                currentChapter = chapter,
                                selectedSectionIndex = 0,
                                errorMessage = if (chapter == null) "Chapter not found" else null
                            )
                        }
                    } else {
                        _readerState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Book not published or accessible"
                            )
                        }
                    }
                }
        }
    }

    fun selectSection(index: Int) {
        _readerState.update { it.copy(selectedSectionIndex = index) }
    }

    fun prepareChapterQuiz(onReady: (materialId: Long) -> Unit) {
        val book = _readerState.value.book ?: return
        val chapter = _readerState.value.currentChapter ?: return
        viewModelScope.launch {
            prepareOriginalChapterStudyAidsUseCase(book, chapter).onSuccess { result ->
                onReady(result.materialId)
            }
        }
    }

    fun prepareChapterFlashcards(onReady: (deckId: Long) -> Unit) {
        val book = _readerState.value.book ?: return
        val chapter = _readerState.value.currentChapter ?: return
        viewModelScope.launch {
            prepareOriginalChapterStudyAidsUseCase(book, chapter).onSuccess { result ->
                onReady(result.deckId)
            }
        }
    }

    private fun applyFilters(
        books: List<QuovexOriginalBook>,
        query: String,
        curriculum: String,
        gradeClass: String,
        subject: String
    ): List<QuovexOriginalBook> {
        return books.filter { book ->
            val matchesQuery = query.isBlank() ||
                    book.title.contains(query, ignoreCase = true) ||
                    book.topic.contains(query, ignoreCase = true) ||
                    book.description.contains(query, ignoreCase = true)

            val matchesCurriculum = curriculum == "All" ||
                    book.curriculum.contains(curriculum, ignoreCase = true)

            val matchesClass = gradeClass == "All" ||
                    book.gradeClass.contains(gradeClass, ignoreCase = true)

            val matchesSubject = subject == "All" ||
                    book.subject.equals(subject, ignoreCase = true)

            matchesQuery && matchesCurriculum && matchesClass && matchesSubject
        }
    }
}
