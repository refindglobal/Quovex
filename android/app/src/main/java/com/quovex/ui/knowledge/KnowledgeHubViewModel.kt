package com.quovex.ui.knowledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.repository.QuovexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KnowledgeHubUiState(
    val selectedSubject: String? = null,
    val subjects: List<String> = emptyList(),
    val materials: List<LearningMaterial> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class KnowledgeHubViewModel @Inject constructor(
    private val repository: QuovexRepository
) : ViewModel() {

    private val _selectedSubject = MutableStateFlow<String?>(null)

    val uiState: StateFlow<KnowledgeHubUiState> = combine(
        repository.getMaterials(),
        repository.getDistinctMaterialSubjects(),
        _selectedSubject
    ) { materials, subjects, selected ->
        val filtered = if (selected.isNullOrBlank()) {
            materials
        } else {
            materials.filter { it.subject.equals(selected, ignoreCase = true) }
        }
        KnowledgeHubUiState(
            selectedSubject = selected,
            subjects = subjects,
            materials = filtered,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = KnowledgeHubUiState(isLoading = true)
    )

    fun selectSubject(subject: String?) {
        _selectedSubject.value = if (_selectedSubject.value == subject) null else subject
    }

    fun deleteMaterial(id: Long) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
        }
    }
}
