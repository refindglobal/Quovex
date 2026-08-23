package com.quovex.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.QuizResult
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.CreateRemedialFlashcardsUseCase
import com.quovex.domain.usecase.RecordQuizResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val materialId: Long = 0L,
    val questions: List<QuizQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val userAnswers: Map<Long, Int> = emptyMap(),
    val isFinished: Boolean = false,
    val finalScore: Int = 0,
    val totalQuestions: Int = 0,
    val mistakes: List<QuizMistake> = emptyList(),
    val remedialDeckCreated: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuovexRepository,
    private val recordQuizResultUseCase: RecordQuizResultUseCase,
    private val createRemedialFlashcardsUseCase: CreateRemedialFlashcardsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val materialId: Long = checkNotNull(savedStateHandle["materialId"])

    private val _uiState = MutableStateFlow(QuizUiState(materialId = materialId))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            val list = repository.getQuizQuestionsList(materialId)
            _uiState.update {
                it.copy(
                    questions = list,
                    totalQuestions = list.size,
                    isLoading = false
                )
            }
        }
    }

    fun selectOption(optionIndex: Int) {
        if (_uiState.value.isSubmitted) return
        _uiState.update { it.copy(selectedOptionIndex = optionIndex) }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val currentQuestion = state.questions.getOrNull(state.currentIndex) ?: return
        val selected = state.selectedOptionIndex ?: return

        _uiState.update {
            it.copy(
                isSubmitted = true,
                userAnswers = it.userAnswers + (currentQuestion.id to selected)
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex + 1 < state.questions.size) {
            _uiState.update {
                it.copy(
                    currentIndex = it.currentIndex + 1,
                    selectedOptionIndex = null,
                    isSubmitted = false
                )
            }
        } else {
            finishQuiz()
        }
    }

    private fun finishQuiz() {
        val state = _uiState.value
        var correctCount = 0
        val mistakesList = mutableListOf<QuizMistake>()

        state.questions.forEach { q ->
            val userPick = state.userAnswers[q.id] ?: -1
            if (userPick == q.correctIndex) {
                correctCount++
            } else {
                mistakesList.add(
                    QuizMistake(
                        questionId = q.id,
                        questionText = q.question,
                        studentAnswer = q.options.getOrNull(userPick) ?: "Skipped",
                        correctAnswer = q.options.getOrNull(q.correctIndex) ?: "",
                        explanation = q.explanation,
                        concept = q.relatedConcept
                    )
                )
            }
        }

        val accuracy = if (state.questions.isNotEmpty()) {
            (correctCount.toFloat() / state.questions.size) * 100f
        } else 0f

        viewModelScope.launch {
            recordQuizResultUseCase(
                result = QuizResult(
                    materialId = state.materialId,
                    score = correctCount,
                    totalQuestions = state.questions.size,
                    accuracyPercent = accuracy,
                    mistakes = mistakesList
                )
            )

            _uiState.update {
                it.copy(
                    isFinished = true,
                    finalScore = correctCount,
                    mistakes = mistakesList
                )
            }
        }
    }

    fun createRemedialFlashcards() {
        val state = _uiState.value
        if (state.mistakes.isEmpty() || state.remedialDeckCreated) return

        viewModelScope.launch {
            createRemedialFlashcardsUseCase(
                mistakes = state.mistakes,
                deckId = state.materialId.toInt()
            )
            _uiState.update { it.copy(remedialDeckCreated = true) }
        }
    }
}
