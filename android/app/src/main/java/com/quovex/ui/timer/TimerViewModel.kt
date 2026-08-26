package com.quovex.ui.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.local.SessionStateManager
import com.quovex.data.service.TimerForegroundService
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.DistractionShieldState
import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.model.FocusMode
import com.quovex.domain.model.FocusTrackingState
import com.quovex.domain.model.SessionStatus
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.model.SoundscapePreset
import com.quovex.domain.model.SoundscapeState
import com.quovex.domain.usecase.ControlFocusDetectionUseCase
import com.quovex.domain.usecase.ControlSoundscapeUseCase
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.GetInstalledAppsUseCase
import com.quovex.domain.usecase.ObserveBlockedAppsUseCase
import com.quovex.domain.usecase.ObserveFocusDetectionUseCase
import com.quovex.domain.usecase.ObserveSoundscapeUseCase
import com.quovex.domain.usecase.StartFocusSessionUseCase
import com.quovex.domain.usecase.ToggleBlockedAppUseCase
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
    val isSoundscapeSheetOpen: Boolean = false,
    val isDistractionShieldSheetOpen: Boolean = false,
    val soundscapeState: SoundscapeState = SoundscapeState(),
    val focusTrackingState: FocusTrackingState = FocusTrackingState(),
    val distractionShieldState: DistractionShieldState = DistractionShieldState(),
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
    private val sessionStateManager: SessionStateManager,
    private val observeSoundscapeUseCase: ObserveSoundscapeUseCase,
    private val controlSoundscapeUseCase: ControlSoundscapeUseCase,
    private val observeFocusDetectionUseCase: ObserveFocusDetectionUseCase,
    private val controlFocusDetectionUseCase: ControlFocusDetectionUseCase,
    private val observeBlockedAppsUseCase: ObserveBlockedAppsUseCase,
    private val toggleBlockedAppUseCase: ToggleBlockedAppUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        observeConfiguredSubjects()
        observeActiveSession()
        observeSessionSummary()
        observeSoundscape()
        observeFocusDetection()
        observeDistractionShield()
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

    private fun observeSoundscape() {
        viewModelScope.launch {
            observeSoundscapeUseCase().collect { soundState ->
                _uiState.update { it.copy(soundscapeState = soundState) }
            }
        }
    }

    private fun observeFocusDetection() {
        viewModelScope.launch {
            observeFocusDetectionUseCase().collect { focusState ->
                _uiState.update { it.copy(focusTrackingState = focusState) }
            }
        }
    }

    private fun observeDistractionShield() {
        viewModelScope.launch {
            observeBlockedAppsUseCase().collect { shieldState ->
                _uiState.update { it.copy(distractionShieldState = shieldState) }
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

    fun toggleCameraFocusDetection() {
        val nextState = !_uiState.value.focusTrackingState.isEnabled
        viewModelScope.launch {
            controlFocusDetectionUseCase.setTrackingEnabled(nextState)
        }
    }

    fun updateCameraPermission(granted: Boolean) {
        controlFocusDetectionUseCase.updateCameraPermission(granted)
    }

    fun onFrameAnalyzed(result: FocusFrameResult) {
        controlFocusDetectionUseCase.processFrame(result)
    }

    fun openSoundscapeSheet() {
        _uiState.update { it.copy(isSoundscapeSheetOpen = true) }
    }

    fun closeSoundscapeSheet() {
        _uiState.update { it.copy(isSoundscapeSheetOpen = false) }
    }

    fun selectSoundscapePreset(preset: SoundscapePreset) {
        viewModelScope.launch {
            controlSoundscapeUseCase.selectPreset(preset)
        }
    }

    fun setSoundscapeVolume(volume: Float) {
        viewModelScope.launch {
            controlSoundscapeUseCase.setVolume(volume)
        }
    }

    fun toggleSoundscapeAutoPlay(enabled: Boolean) {
        viewModelScope.launch {
            controlSoundscapeUseCase.setAutoPlay(enabled)
        }
    }

    fun toggleSoundscapePlay() {
        controlSoundscapeUseCase.togglePlay()
    }

    // Distraction Shield actions
    fun openDistractionShieldSheet() {
        viewModelScope.launch {
            getInstalledAppsUseCase()
            _uiState.update { it.copy(isDistractionShieldSheetOpen = true) }
        }
    }

    fun closeDistractionShieldSheet() {
        _uiState.update { it.copy(isDistractionShieldSheetOpen = false) }
    }

    fun toggleShield(enabled: Boolean) {
        viewModelScope.launch {
            toggleBlockedAppUseCase.setShieldEnabled(enabled)
        }
    }

    fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            toggleBlockedAppUseCase.toggleApp(packageName, isBlocked)
        }
    }

    fun toggleCategoryBlocked(category: AppCategory, isBlocked: Boolean) {
        viewModelScope.launch {
            toggleBlockedAppUseCase.setCategoryBlocked(category, isBlocked)
        }
    }

    fun startSession(context: Context) {
        val state = _uiState.value
        val result = startFocusSessionUseCase(
            subject = state.selectedSubject,
            mode = state.selectedMode,
            strictFocusEnabled = state.strictFocusEnabled
        )

        result.onSuccess {
            TimerForegroundService.start(context)
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
        TimerForegroundService.stop(context)
    }

    fun dismissSummary() {
        sessionStateManager.clearSummary()
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
