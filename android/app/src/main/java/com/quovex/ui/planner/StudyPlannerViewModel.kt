package com.quovex.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.DailyStudyTask
import com.quovex.domain.model.StudyPlan
import com.quovex.domain.usecase.GenerateStudyPlanUseCase
import com.quovex.domain.usecase.ObserveDailyScheduleUseCase
import com.quovex.domain.usecase.UpdateTaskProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class StudyPlannerUiState(
    val activePlan: StudyPlan? = null,
    val selectedDay: Int = 1,
    val tasksForSelectedDay: List<DailyStudyTask> = emptyList(),
    val allPlanTasks: List<DailyStudyTask> = emptyList(),
    val isGenerating: Boolean = false,
    val generationError: String? = null,
    val showWizard: Boolean = false,
    val wizardStep: Int = 1, // 1: Exam & Date, 2: Subjects & Hours, 3: Weak Areas, 4: Review
    val selectedExam: String = "JEE Advanced",
    val customExamName: String = "",
    val examDateMillis: Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(60),
    val dailyHours: Float = 4.0f,
    val selectedSubjects: Set<String> = setOf("Physics", "Chemistry", "Mathematics"),
    val weakTopicsInput: String = ""
) {
    val totalPlanProgress: Float
        get() {
            if (allPlanTasks.isEmpty()) return 0f
            val completedCount = allPlanTasks.count { it.isCompleted }
            return completedCount.toFloat() / allPlanTasks.size.toFloat()
        }

    val totalHoursPlanned: Float
        get() {
            val totalMinutes = allPlanTasks.sumOf { it.estimatedMinutes }
            return totalMinutes / 60f
        }

    val totalHoursCompleted: Float
        get() {
            val completedMinutes = allPlanTasks.filter { it.isCompleted }.sumOf { it.completedMinutes }
            return completedMinutes / 60f
        }
}

@HiltViewModel
class StudyPlannerViewModel @Inject constructor(
    private val generateStudyPlanUseCase: GenerateStudyPlanUseCase,
    private val observeDailyScheduleUseCase: ObserveDailyScheduleUseCase,
    private val updateTaskProgressUseCase: UpdateTaskProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudyPlannerUiState())
    val uiState: StateFlow<StudyPlannerUiState> = _uiState.asStateFlow()

    init {
        observeActivePlan()
    }

    private fun observeActivePlan() {
        viewModelScope.launch {
            observeDailyScheduleUseCase.observeActivePlan().collect { plan ->
                _uiState.update { it.copy(activePlan = plan, showWizard = plan == null) }
                if (plan != null) {
                    observePlanTasks(plan.id)
                }
            }
        }
    }

    private fun observePlanTasks(planId: Long) {
        viewModelScope.launch {
            observeDailyScheduleUseCase.observeTasksForPlan(planId).collect { tasks ->
                _uiState.update { state ->
                    val dayTasks = tasks.filter { it.dayNumber == state.selectedDay }
                    state.copy(
                        allPlanTasks = tasks,
                        tasksForSelectedDay = dayTasks
                    )
                }
            }
        }
    }

    fun selectDay(dayNumber: Int) {
        _uiState.update { state ->
            val dayTasks = state.allPlanTasks.filter { it.dayNumber == dayNumber }
            state.copy(
                selectedDay = dayNumber,
                tasksForSelectedDay = dayTasks
            )
        }
    }

    fun toggleTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            updateTaskProgressUseCase.execute(taskId, isCompleted)
        }
    }

    fun openWizard() {
        _uiState.update { it.copy(showWizard = true, wizardStep = 1, generationError = null) }
    }

    fun closeWizard() {
        if (_uiState.value.activePlan != null) {
            _uiState.update { it.copy(showWizard = false) }
        }
    }

    fun setWizardStep(step: Int) {
        _uiState.update { it.copy(wizardStep = step.coerceIn(1, 4)) }
    }

    fun selectExam(exam: String) {
        _uiState.update { it.copy(selectedExam = exam) }
    }

    fun setCustomExamName(name: String) {
        _uiState.update { it.copy(customExamName = name) }
    }

    fun setExamDate(dateMillis: Long) {
        _uiState.update { it.copy(examDateMillis = dateMillis) }
    }

    fun setDailyHours(hours: Float) {
        _uiState.update { it.copy(dailyHours = hours) }
    }

    fun toggleSubject(subject: String) {
        _uiState.update { state ->
            val updated = if (state.selectedSubjects.contains(subject)) {
                if (state.selectedSubjects.size > 1) state.selectedSubjects - subject else state.selectedSubjects
            } else {
                state.selectedSubjects + subject
            }
            state.copy(selectedSubjects = updated)
        }
    }

    fun setWeakTopics(text: String) {
        _uiState.update { it.copy(weakTopicsInput = text) }
    }

    fun generatePlan() {
        val state = _uiState.value
        val examName = if (state.selectedExam == "Other") {
            state.customExamName.ifBlank { "Custom Exam" }
        } else {
            state.selectedExam
        }

        val weakTopics = state.weakTopicsInput
            .split(",", "\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        _uiState.update { it.copy(isGenerating = true, generationError = null) }

        viewModelScope.launch {
            val result = generateStudyPlanUseCase.execute(
                examName = examName,
                examDateMillis = state.examDateMillis,
                dailyStudyHours = state.dailyHours,
                subjects = state.selectedSubjects.toList(),
                weakTopics = weakTopics
            )

            result.fold(
                onSuccess = { plan ->
                    _uiState.update {
                        it.copy(
                            activePlan = plan,
                            isGenerating = false,
                            showWizard = false,
                            selectedDay = 1
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            generationError = error.message ?: "Failed to generate study plan"
                        )
                    }
                }
            )
        }
    }
}
