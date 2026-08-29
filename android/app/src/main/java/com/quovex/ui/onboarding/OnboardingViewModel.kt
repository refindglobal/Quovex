package com.quovex.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0, // 0 = Exam, 1 = Obstacle, 2 = Commitment
    val selectedExam: String = "JEE Main & Advanced",
    val selectedObstacle: String = "Social Media Doomscrolling",
    val dailyGoalHours: Float = 2.5f,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirebaseFirestoreService,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun selectExam(exam: String) = _uiState.update { it.copy(selectedExam = exam) }
    fun selectObstacle(obstacle: String) = _uiState.update { it.copy(selectedObstacle = obstacle) }
    fun setDailyGoal(hours: Float) = _uiState.update { it.copy(dailyGoalHours = hours) }

    fun nextStep() {
        if (_uiState.value.currentStep < 2) {
            _uiState.update { it.copy(currentStep = it.currentStep + 1, errorMessage = null) }
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1, errorMessage = null) }
        }
    }

    fun finishOnboarding(onSuccess: () -> Unit) {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            userPreferencesManager.setOnboardingCompleted(true)
            userPreferencesManager.setDailyStudyTarget(state.dailyGoalHours)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
