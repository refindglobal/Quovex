package com.quovex.ui.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.CemeteryTombstone
import com.quovex.domain.model.StreakInfo
import com.quovex.domain.model.StreakMilestone
import com.quovex.domain.model.StreakStatus
import com.quovex.domain.usecase.CheckStreakMilestoneUseCase
import com.quovex.domain.usecase.LogStreakReflectionUseCase
import com.quovex.domain.usecase.ManageStreakUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreakUiState(
    val streakInfo: StreakInfo = StreakInfo(),
    val tombstones: List<CemeteryTombstone> = emptyList(),
    val milestones: List<StreakMilestone> = emptyList(),
    val isRedeemingToken: Boolean = false,
    val actionMessage: String? = null,
    val selectedTombstoneForReflection: CemeteryTombstone? = null
)

@HiltViewModel
class StreakViewModel @Inject constructor(
    private val manageStreakUseCase: ManageStreakUseCase,
    private val logStreakReflectionUseCase: LogStreakReflectionUseCase,
    private val checkStreakMilestoneUseCase: CheckStreakMilestoneUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StreakUiState())
    val uiState: StateFlow<StreakUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            manageStreakUseCase.getStreakInfo().collect { info ->
                val milestones = checkStreakMilestoneUseCase(info.currentStreak)
                _uiState.update {
                    it.copy(
                        streakInfo = info,
                        milestones = milestones
                    )
                }
            }
        }

        viewModelScope.launch {
            manageStreakUseCase.getCemeteryTombstones().collect { tombstones ->
                _uiState.update {
                    it.copy(tombstones = tombstones)
                }
            }
        }
    }

    fun spendRescueToken() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRedeemingToken = true) }
            val result = manageStreakUseCase.spendRescueToken()
            _uiState.update {
                it.copy(
                    isRedeemingToken = false,
                    actionMessage = result.message
                )
            }
        }
    }

    fun openReflectionDialog(tombstone: CemeteryTombstone) {
        _uiState.update { it.copy(selectedTombstoneForReflection = tombstone) }
    }

    fun dismissReflectionDialog() {
        _uiState.update { it.copy(selectedTombstoneForReflection = null) }
    }

    fun saveReflection(note: String) {
        val selected = _uiState.value.selectedTombstoneForReflection ?: return
        viewModelScope.launch {
            val success = logStreakReflectionUseCase(selected.id, note)
            if (success) {
                _uiState.update {
                    it.copy(
                        selectedTombstoneForReflection = null,
                        actionMessage = "Reflection saved to Streak Cemetery."
                    )
                }
            }
        }
    }

    fun dismissActionMessage() {
        _uiState.update { it.copy(actionMessage = null) }
    }
}
