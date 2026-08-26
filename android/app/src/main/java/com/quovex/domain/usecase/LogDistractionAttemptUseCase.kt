package com.quovex.domain.usecase

import com.quovex.domain.repository.DistractionBlockerRepository
import javax.inject.Inject

class LogDistractionAttemptUseCase @Inject constructor(
    private val distractionBlockerRepository: DistractionBlockerRepository
) {
    suspend operator fun invoke(packageName: String): Int {
        return distractionBlockerRepository.recordDistractionAttempt(packageName)
    }
}
