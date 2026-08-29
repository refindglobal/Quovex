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
    val isSignUpMode: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: FirebaseAuthService,
    private val firestoreService: FirebaseFirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleAuthMode() = _uiState.update { it.copy(isSignUpMode = !it.isSignUpMode, errorMessage = null) }
    fun onEmailChanged(email: String) = _uiState.update { it.copy(emailInput = email, errorMessage = null) }
    fun onPasswordChanged(pass: String) = _uiState.update { it.copy(passwordInput = pass, errorMessage = null) }

    fun signInWithGoogle(context: Context, webClientId: String, onSuccess: (isNewUser: Boolean) -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = authService.signInWithGoogle(context, webClientId)

            result.onSuccess { user ->
                registerFcmToken(user.uid)
                firestoreService.getUserProfileFlow(user.uid).collect { profile ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(profile == null)
                    return@collect
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Google Sign-In failed") }
            }
        }
    }

    fun submitEmailAuth(onSuccess: (isNewUser: Boolean) -> Unit) {
        val state = _uiState.value
        val email = state.emailInput.trim()
        val password = state.passwordInput.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = if (state.isSignUpMode) {
                authService.signUpWithEmail(email, password)
            } else {
                authService.signInWithEmail(email, password)
            }

            result.onSuccess { user ->
                registerFcmToken(user.uid)
                firestoreService.getUserProfileFlow(user.uid).collect { profile ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(state.isSignUpMode || profile == null)
                    return@collect
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error.localizedMessage ?: "Authentication failed") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
}
