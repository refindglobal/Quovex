package com.quovex.ui.knowledge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.QuizResult
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.GenerateQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaterialDetailUiState(
    val material: LearningMaterial? = null,
    val quizQuestions: List<QuizQuestion> = emptyList(),
    val quizResults: List<QuizResult> = emptyList(),
    val isGeneratingQuiz: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class MaterialDetailViewModel @Inject constructor(
    private val repository: QuovexRepository,
    private val generateQuizUseCase: GenerateQuizUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val materialId: Long = checkNotNull(savedStateHandle["materialId"])

    private val _uiState = MutableStateFlow(MaterialDetailUiState())
    val uiState: StateFlow<MaterialDetailUiState> = _uiState.asStateFlow()

    init {
        loadMaterial()
    }

    fun loadMaterial() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val item = repository.getMaterialById(materialId)
            val questions = repository.getQuizQuestionsList(materialId)

            _uiState.update {
                it.copy(
                    material = item,
                    quizQuestions = questions,
                    isLoading = false
                )
            }
        }
    }

    fun generateQuiz() {
        val mat = _uiState.value.material ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingQuiz = true, error = null) }
            val result = generateQuizUseCase(
                materialId = mat.id,
                subject = mat.subject,
                topic = mat.topic.ifBlank { mat.title },
                difficulty = "Medium",
                keyPoints = mat.keyPoints
            )

            result.onSuccess { questions ->
                _uiState.update {
                    it.copy(
                        quizQuestions = questions,
                        isGeneratingQuiz = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGeneratingQuiz = false,
                        error = "Quovex AI couldn't generate quiz: ${error.message ?: "Please retry."}"
                    )
                }
            }
        }
    }
}
