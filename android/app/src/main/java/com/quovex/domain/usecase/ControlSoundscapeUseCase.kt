package com.quovex.domain.usecase

import com.quovex.domain.model.SoundscapePreset
import com.quovex.domain.repository.SoundscapeRepository
import javax.inject.Inject

/**
 * Executes control actions for soundscape selection, volume adjustments, and playback triggers.
 */
class ControlSoundscapeUseCase @Inject constructor(
    private val soundscapeRepository: SoundscapeRepository
) {

    suspend fun selectPreset(preset: SoundscapePreset) {
        soundscapeRepository.selectPreset(preset)
    }

    suspend fun setVolume(volume: Float) {
        soundscapeRepository.setVolume(volume.coerceIn(0.0f, 1.0f))
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        soundscapeRepository.setAutoPlay(enabled)
    }

    fun play() {
        soundscapeRepository.play()
    }

    fun pause() {
        soundscapeRepository.pause()
    }

    fun togglePlay() {
        soundscapeRepository.togglePlay()
    }

    fun stop() {
        soundscapeRepository.stop()
    }
}
