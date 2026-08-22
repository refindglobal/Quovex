package com.quovex.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.UserProfile
import com.quovex.domain.repository.QuovexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userProfile: UserProfile = UserProfile(),
    val totalFocusHours: Double = 0.0,
    val totalCardsReviewed: Int = 0,
    val focusScoreAverage: Int = 94,
    val isSignedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val repository: QuovexRepository,
    private val authService: FirebaseAuthService,
    private val firestoreService: FirebaseFirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            userPreferencesManager.userProfile.collect { profile ->
                val focusSeconds = repository.getTodayFocusSeconds()
                val totalHours = focusSeconds / 3600.0
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        totalFocusHours = totalHours
                    )
                }
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        authService.signOut()
        userPreferencesManager.resetProfileForNewUser()
        _uiState.update { it.copy(isSignedOut = true) }
        onSignedOut()
    }
}
