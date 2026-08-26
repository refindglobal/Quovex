package com.quovex.domain.usecase

import com.quovex.domain.model.AppCategory
import com.quovex.domain.repository.DistractionBlockerRepository
import javax.inject.Inject

class ToggleBlockedAppUseCase @Inject constructor(
    private val distractionBlockerRepository: DistractionBlockerRepository
) {
    suspend fun toggleApp(packageName: String, isBlocked: Boolean) {
        distractionBlockerRepository.toggleAppBlocked(packageName, isBlocked)
    }

    suspend fun setCategoryBlocked(category: AppCategory, isBlocked: Boolean) {
        distractionBlockerRepository.setCategoryBlocked(category, isBlocked)
    }

    suspend fun setShieldEnabled(enabled: Boolean) {
        distractionBlockerRepository.setShieldEnabled(enabled)
    }
}
