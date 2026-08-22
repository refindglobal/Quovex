package com.quovex.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.local.SessionStateManager
import com.quovex.data.local.UserPreferencesManager
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DueFlashcardsSummary
import com.quovex.domain.model.JumpBackInItem
import com.quovex.domain.model.RecentActivityItem
import com.quovex.domain.model.UserProfile
import com.quovex.domain.model.WeeklyDayProgress
import com.quovex.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

sealed interface DashboardUiStatus {
    object Loading : DashboardUiStatus
    object Success : DashboardUiStatus
    data class Error(val message: String) : DashboardUiStatus
}

data class DashboardUiState(
    val status: DashboardUiStatus = DashboardUiStatus.Loading,
    val greeting: String = "Good Day",
    val userProfile: UserProfile = UserProfile(),
    val todayFocusMinutes: Int = 0,
    val targetMinutes: Int = 240,
    val progressPercent: Float = 0f,
    val isGoalCompleted: Boolean = false,
    val isGoalExceeded: Boolean = false,
    val hasGoal: Boolean = true,
    val streakDays: Int = 0,
    val totalXp: Long = 0L,
    val weeklyProgress: List<WeeklyDayProgress> = emptyList(),
    val dueFlashcards: DueFlashcardsSummary = DueFlashcardsSummary(0),
    val jumpBackInItem: JumpBackInItem? = null,
    val recentActivities: List<RecentActivityItem> = emptyList(),
    val activeSession: ActiveSessionState = ActiveSessionState()
) {
    val isLoading: Boolean get() = status is DashboardUiStatus.Loading
    val isError: Boolean get() = status is DashboardUiStatus.Error
    val errorMessage: String? get() = (status as? DashboardUiStatus.Error)?.message
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val userPreferencesManager: UserPreferencesManager,
    private val sessionStateManager: SessionStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDataSources()
        loadDashboardData()
    }

    private fun observeDataSources() {
        viewModelScope.launch {
            userPreferencesManager.userProfile.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
                loadDashboardData()
            }
        }
        viewModelScope.launch {
            sessionStateManager.activeSession.collect { session ->
                _uiState.update { it.copy(activeSession = session) }
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val hour = LocalTime.now().hour
                val greeting = calculateGreeting(hour)
                val data = getDashboardStatsUseCase()

                _uiState.update {
                    it.copy(
                        status = DashboardUiStatus.Success,
                        greeting = greeting,
                        userProfile = data.userProfile,
                        todayFocusMinutes = data.todayFocusMinutes,
                        targetMinutes = data.targetMinutes,
                        progressPercent = data.progressPercent,
                        isGoalCompleted = data.isGoalCompleted,
                        isGoalExceeded = data.isGoalExceeded,
                        hasGoal = data.hasGoal,
                        streakDays = data.streakDays,
                        totalXp = data.totalXp,
                        weeklyProgress = data.weeklyProgress,
                        dueFlashcards = data.dueFlashcards,
                        jumpBackInItem = data.jumpBackInItem,
                        recentActivities = data.recentActivities,
                        activeSession = data.activeSession
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        status = DashboardUiStatus.Error(e.message ?: "Failed to load dashboard data")
                    )
                }
            }
        }
    }

    fun calculateGreeting(hourOfDay: Int): String {
        return when (hourOfDay) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
