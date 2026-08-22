package com.quovex.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.local.SessionStateManager
import com.quovex.data.service.TimerForegroundService
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.FocusMode
import com.quovex.domain.model.SessionStatus
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.StartFocusSessionUseCase
import com.quovex.domain.util.FocusTimerEngine
import com.quovex.domain.util.TimerFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimerScreenState {
    SETUP,
    ACTIVE,
    SUMMARY
}

data class TimerUiState(
    val availableSubjects: List<String> = emptyList(),
    val selectedSubject: String = "Physics",
    val selectedMode: FocusMode = FocusMode.Pomodoro,
    val strictFocusEnabled: Boolean = true,
    val isCustomDurationDialogOpen: Boolean = false,
    val isEndEarlyDialogOpen: Boolean = false,
    val activeSession: ActiveSessionState = ActiveSessionState(),
    val latestSummary: SessionSummary? = null,
    val errorMessage: String? = null
) {
    val screenState: TimerScreenState
        get() = when {
            latestSummary != null -> TimerScreenState.SUMMARY
            activeSession.isActive && activeSession.status == SessionStatus.RUNNING -> TimerScreenState.ACTIVE
            else -> TimerScreenState.SETUP
        }

    val formattedRemainingTime: String
        get() = TimerFormatter.formatRemainingTime(activeSession.remainingSeconds)

    val progress: Float
        get() = FocusTimerEngine.calculateProgress(
            remainingSeconds = activeSession.remainingSeconds,
            totalSeconds = activeSession.totalSeconds
        )
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase,
    private val startFocusSessionUseCase: StartFocusSessionUseCase,
    private val sessionStateManager: SessionStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        observeConfiguredSubjects()
        observeActiveSession()
        observeSessionSummary()
    }

    private fun observeConfiguredSubjects() {
        viewModelScope.launch {
            getConfiguredSubjectsUseCase().collect { subjects ->
                _uiState.update { state ->
                    val defaultSubject = state.selectedSubject.ifBlank {
                        subjects.firstOrNull() ?: "Physics"
                    }
                    state.copy(
                        availableSubjects = subjects,
                        selectedSubject = if (subjects.contains(state.selectedSubject)) state.selectedSubject else defaultSubject
                    )
                }
            }
        }
    }

    private fun observeActiveSession() {
        viewModelScope.launch {
            sessionStateManager.activeSession.collect { active ->
                _uiState.update { it.copy(activeSession = active) }
            }
        }
    }

    private fun observeSessionSummary() {
        viewModelScope.launch {
            sessionStateManager.latestSummary.collect { summary ->
                _uiState.update { it.copy(latestSummary = summary) }
            }
        }
    }

    fun selectSubject(subject: String) {
        if (_uiState.value.activeSession.isActive) return
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun selectMode(mode: FocusMode) {
        if (_uiState.value.activeSession.isActive) return
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun setCustomDuration(focusMinutes: Int, breakMinutes: Int) {
        val safeFocus = focusMinutes.coerceIn(1, 240)
        val safeBreak = breakMinutes.coerceIn(1, 60)
        val customMode = FocusMode.Custom(
            customFocusMinutes = safeFocus,
            customBreakMinutes = safeBreak
        )
        _uiState.update {
            it.copy(
                selectedMode = customMode,
                isCustomDurationDialogOpen = false
            )
        }
    }

    fun openCustomDurationDialog() {
        _uiState.update { it.copy(isCustomDurationDialogOpen = true) }
    }

    fun closeCustomDurationDialog() {
        _uiState.update { it.copy(isCustomDurationDialogOpen = false) }
    }

    fun toggleStrictFocus() {
        _uiState.update { it.copy(strictFocusEnabled = !it.strictFocusEnabled) }
    }

    fun startSession(context: Context) {
        val state = _uiState.value
        val result = startFocusSessionUseCase(
            subject = state.selectedSubject,
            mode = state.selectedMode,
            strictFocusEnabled = state.strictFocusEnabled
        )

        result.onSuccess {
            TimerForegroundService.startService(context)
        }.onFailure { error ->
            _uiState.update { it.copy(errorMessage = error.message ?: "Failed to start session") }
        }
    }

    fun requestEndEarly() {
        _uiState.update { it.copy(isEndEarlyDialogOpen = true) }
    }

    fun dismissEndEarlyDialog() {
        _uiState.update { it.copy(isEndEarlyDialogOpen = false) }
    }

    fun confirmEndEarly(context: Context) {
        _uiState.update { it.copy(isEndEarlyDialogOpen = false) }
        TimerForegroundService.stopService(context)
    }

    fun dismissSummary() {
        sessionStateManager.clearSummary()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
