package com.quovex.ui.ncert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.NcertBook
import com.quovex.domain.repository.NcertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NcertBrowserUiState(
    val availableClasses: List<Int> = listOf(9, 10, 11, 12),
    val selectedClass: Int = 12,
    val availableSubjects: List<String> = emptyList(),
    val selectedSubject: String = "All",
    val searchQuery: String = "",
    val books: List<NcertBook> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class NcertBrowserViewModel @Inject constructor(
    private val ncertRepository: NcertRepository
) : ViewModel() {

    private val _selectedClass = MutableStateFlow(12)
    private val _selectedSubject = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)

    val uiState: StateFlow<NcertBrowserUiState> = combine(
        _selectedClass,
        _selectedSubject,
        _searchQuery,
        _isRefreshing,
        ncertRepository.getAvailableClasses()
    ) { selClass, selSubj, query, refreshing, classes ->
        Tuple5(selClass, selSubj, query, refreshing, classes)
    }.flatMapLatest { tuple ->
        val (selClass, selSubj, query, refreshing, classes) = tuple
        combine(
            ncertRepository.getSubjectsForClass(selClass),
            ncertRepository.getBooks(selClass, if (selSubj == "All") null else selSubj)
        ) { subjects, books ->
            val allSubjects = listOf("All") + subjects
            val filteredBooks = if (query.isBlank()) {
                books
            } else {
                books.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.subject.contains(query, ignoreCase = true)
                }
            }

            NcertBrowserUiState(
                availableClasses = if (classes.isNotEmpty()) classes else listOf(9, 10, 11, 12),
                selectedClass = selClass,
                availableSubjects = allSubjects,
                selectedSubject = if (allSubjects.contains(selSubj)) selSubj else "All",
                searchQuery = query,
                books = filteredBooks,
                isRefreshing = refreshing
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NcertBrowserUiState(isLoading = true)
    )

    fun selectClass(classLevel: Int) {
        _selectedClass.value = classLevel
        _selectedSubject.value = "All"
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            ncertRepository.refreshCatalog()
            _isRefreshing.value = false
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
