package com.quovex.domain.usecase

import com.quovex.domain.model.DistractionShieldState
import com.quovex.domain.repository.DistractionBlockerRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveBlockedAppsUseCase @Inject constructor(
    private val distractionBlockerRepository: DistractionBlockerRepository
) {
    operator fun invoke(): StateFlow<DistractionShieldState> = distractionBlockerRepository.shieldState
}
