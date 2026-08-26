package com.quovex.ui.blocker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.DistractionShieldState
import com.quovex.domain.usecase.GetInstalledAppsUseCase
import com.quovex.domain.usecase.ObserveBlockedAppsUseCase
import com.quovex.domain.usecase.ToggleBlockedAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DistractionBlockerViewModel @Inject constructor(
    private val observeBlockedAppsUseCase: ObserveBlockedAppsUseCase,
    private val toggleBlockedAppUseCase: ToggleBlockedAppUseCase,
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase
) : ViewModel() {

    val shieldState: StateFlow<DistractionShieldState> = observeBlockedAppsUseCase()

    init {
        refreshInstalledApps()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            getInstalledAppsUseCase()
        }
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

    fun setCategoryBlocked(category: AppCategory, isBlocked: Boolean) {
        viewModelScope.launch {
            toggleBlockedAppUseCase.setCategoryBlocked(category, isBlocked)
        }
    }
}
