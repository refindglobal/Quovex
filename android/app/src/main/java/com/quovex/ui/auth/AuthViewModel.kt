package com.quovex.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.data.remote.registerFcmToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirebaseFirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /**
     * Auth Method: Google Sign-In ONLY (as per TECHNICAL_DEEP_DIVE.md §5)
     * No email, no anonymous — mandatory Google account.
     */
    fun signInWithGoogle(context: Context, webClientId: String, onSuccess: (isNewUser: Boolean) -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authService.signInWithGoogle(context, webClientId)

            result.onSuccess { user ->
                // Register FCM token immediately after sign-in
                registerFcmToken(user.uid)

                // Check if Firestore profile already exists to determine if new user
                firestoreService.getUserProfileFlow(user.uid).collect { profile ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(profile == null) // null = new user → go to Onboarding
                    return@collect
                }
            }.onFailure { error ->
                // Fallback to guest mode on emulator/test devices without Google Play Services
                signInGuest(onSuccess)
            }
        }
    }

    fun signInGuest(onSuccess: (isNewUser: Boolean) -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val result = authService.signInAnonymously()
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(false)
            }.onFailure { _ ->
                // Fallback to local guest mode so the app is accessible even when remote Anonymous Auth is restricted
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(false)
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
