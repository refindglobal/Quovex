package com.quovex.ui.profile

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

data class ProfileSetupUiState(
    val selectedAvatarId: Int = 1,
    val username: String = "",
    val targetExam: String = "JEE Main & Advanced",
    val gradeLevel: String = "Class 12",
    val targetYear: String = "2026",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirebaseFirestoreService,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        // Pre-fill username from Firebase email if available
        val email = authService.currentUser?.email ?: ""
        val defaultName = if (email.contains("@")) email.substringBefore("@").replace(".", "_") else "Aspirant"
        _uiState.update { it.copy(username = defaultName) }
    }

    fun selectAvatar(avatarId: Int) = _uiState.update { it.copy(selectedAvatarId = avatarId) }
    fun onUsernameChanged(name: String) = _uiState.update { it.copy(username = name, errorMessage = null) }
    fun selectExam(exam: String) = _uiState.update { it.copy(targetExam = exam) }
    fun selectGrade(grade: String) = _uiState.update { it.copy(gradeLevel = grade) }
    fun selectYear(year: String) = _uiState.update { it.copy(targetYear = year) }

    fun completeProfile(onSuccess: () -> Unit) {
        val state = _uiState.value
        val cleanName = state.username.trim()

        if (cleanName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a student username / @handle") }
            return
        }

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        val uid = authService.currentUserId
        val email = authService.currentUser?.email ?: ""

        val profile = UserProfile(
            id = uid,
            name = cleanName,
            email = email,
            avatarId = state.selectedAvatarId,
            targetExam = state.targetExam,
            dailyGoalHours = userPreferencesManager.userProfile.value.dailyGoalHours,
            streakDays = 1,
            xp = 250, // Welcome XP bonus
            level = 1,
            isOnboarded = true
        )

        viewModelScope.launch {
            try {
                firestoreService.saveUserProfile(profile)
                userPreferencesManager.saveUserProfile(profile)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            } catch (e: Exception) {
                // If firestore fails offline, save locally
                userPreferencesManager.saveUserProfile(profile)
                _uiState.update { it.copy(isSaving = false) }
                onSuccess()
            }
        }
    }
}
