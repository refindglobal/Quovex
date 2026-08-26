package com.quovex.domain.usecase

import com.quovex.domain.model.SoundscapeState
import com.quovex.domain.repository.SoundscapeRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Observes the current ambient soundscape playback state and user preferences.
 */
class ObserveSoundscapeUseCase @Inject constructor(
    private val soundscapeRepository: SoundscapeRepository
) {
    operator fun invoke(): StateFlow<SoundscapeState> = soundscapeRepository.soundscapeState
}
