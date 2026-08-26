package com.quovex.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.DiagnosticQuestion
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.QuizResult
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.GenerateDailyDiagnosticQuizUseCase
import com.quovex.domain.usecase.SynthesizeRemedialFlashcardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyDiagnosticQuizUiState(
    val isLoading: Boolean = true,
    val questions: List<DiagnosticQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val userAnswers: Map<Long, Int> = emptyMap(),
    val isFinished: Boolean = false,
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val mistakes: List<QuizMistake> = emptyList(),
    val isSynthesizingRemedial: Boolean = false,
    val remedialCardsCreated: Int = 0,
    val errorMessage: String? = null
)

@HiltViewModel
class DailyDiagnosticQuizViewModel @Inject constructor(
    private val generateDailyDiagnosticQuizUseCase: GenerateDailyDiagnosticQuizUseCase,
    private val synthesizeRemedialFlashcardsUseCase: SynthesizeRemedialFlashcardsUseCase,
    private val quovexRepository: QuovexRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyDiagnosticQuizUiState())
    val uiState: StateFlow<DailyDiagnosticQuizUiState> = _uiState.asStateFlow()

    init {
        loadDailyQuiz()
    }

    fun loadDailyQuiz() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = generateDailyDiagnosticQuizUseCase()
            result.onSuccess { questions ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        questions = questions,
                        totalQuestions = questions.size,
                        currentIndex = 0,
                        selectedOptionIndex = null,
                        isSubmitted = false,
                        isFinished = false,
                        userAnswers = emptyMap(),
                        mistakes = emptyList(),
                        remedialCardsCreated = 0
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load today's diagnostic quiz"
                    )
                }
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
            val selected = state.userAnswers[q.id]
            if (selected == q.correctOptionIndex) {
                correctCount++
            } else {
                val studentAns = if (selected != null) q.options.getOrElse(selected) { "No option" } else "Skipped"
                val correctAns = q.options.getOrElse(q.correctOptionIndex) { "" }
                mistakesList.add(
                    QuizMistake(
                        id = 0,
                        resultId = 0,
                        questionText = q.questionText,
                        studentAnswer = studentAns,
                        correctAnswer = correctAns,
                        explanation = q.explanation,
                        concept = q.concept
                    )
                )
            }
        }

        val finalScorePct = if (state.totalQuestions > 0) {
            ((correctCount.toFloat() / state.totalQuestions.toFloat()) * 100).toInt()
        } else {
            0
        }

        _uiState.update {
            it.copy(
                isFinished = true,
                score = finalScorePct,
                mistakes = mistakesList
            )
        }

        // Record quiz result into Room DB
        viewModelScope.launch {
            quovexRepository.recordQuizResult(
                QuizResult(
                    materialId = 0L,
                    score = finalScorePct,
                    totalQuestions = state.totalQuestions,
                    accuracyPercent = finalScorePct.toFloat(),
                    mistakes = mistakesList
                )
            )

            // Auto-queue remedial flashcards if there were mistakes
            if (mistakesList.isNotEmpty()) {
                autoQueueRemedialFlashcards(mistakesList)
            }
        }
    }

    private suspend fun autoQueueRemedialFlashcards(mistakes: List<QuizMistake>) {
        _uiState.update { it.copy(isSynthesizingRemedial = true) }
        val result = synthesizeRemedialFlashcardsUseCase(mistakes)
        result.onSuccess { insertedIds ->
            _uiState.update {
                it.copy(
                    isSynthesizingRemedial = false,
                    remedialCardsCreated = insertedIds.size
                )
            }
        }.onFailure {
            _uiState.update { it.copy(isSynthesizingRemedial = false) }
        }
    }
}
