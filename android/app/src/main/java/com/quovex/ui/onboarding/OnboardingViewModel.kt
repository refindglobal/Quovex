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
    val currentStep: Int = 0, // 0 = Profile/Avatar, 1 = Exam Target, 2 = Daily Hours, 3 = Confirmation
    val selectedAvatarId: Int = 1,
    val nameInput: String = "",
    val selectedExam: String = "JEE Advanced",
    val dailyGoalHours: Float = 4.0f,
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

    fun selectAvatar(id: Int) = _uiState.update { it.copy(selectedAvatarId = id) }
    fun onNameChanged(name: String) = _uiState.update { it.copy(nameInput = name, errorMessage = null) }
    fun selectExam(exam: String) = _uiState.update { it.copy(selectedExam = exam) }
    fun setDailyGoal(hours: Float) = _uiState.update { it.copy(dailyGoalHours = hours) }

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep == 0 && state.nameInput.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your name to personalize your study space.") }
            return
        }
        if (state.currentStep < 3) {
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

        val uid = authService.currentUserId
        val userProfile = UserProfile(
            id = uid,
            name = state.nameInput.trim().ifEmpty { "Aspirant" },
            avatarId = state.selectedAvatarId,
            targetExam = state.selectedExam,
            dailyGoalHours = state.dailyGoalHours,
            streakDays = 1,
            xp = 250, // Welcome bonus XP
            level = 1,
            isOnboarded = true,
            email = authService.currentUser?.email ?: ""
        )

        viewModelScope.launch {
            // 1. Write to Firestore
            firestoreService.saveUserProfile(userProfile)

            // 2. Save locally in UserPreferencesManager
            userPreferencesManager.saveUserProfile(userProfile)

            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }
}
