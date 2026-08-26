package com.quovex.domain.usecase

import com.quovex.domain.model.FocusTrackingState
import com.quovex.domain.repository.FocusDetectionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveFocusDetectionUseCase @Inject constructor(
    private val focusDetectionRepository: FocusDetectionRepository
) {
    operator fun invoke(): StateFlow<FocusTrackingState> {
        return focusDetectionRepository.focusTrackingState
    }
}
