package com.quovex.domain.repository

import com.quovex.domain.model.SoundscapePreset
import com.quovex.domain.model.SoundscapeState
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for managing ambient focus soundscapes, binaural brainwave synthesis,
 * and audio playback lifecycle.
 */
interface SoundscapeRepository {

    /**
     * Reactive stream of the current soundscape playback state, volume, and preferences.
     */
    val soundscapeState: StateFlow<SoundscapeState>

    /**
     * Selects and switches to a soundscape preset.
     */
    suspend fun selectPreset(preset: SoundscapePreset)

    /**
     * Updates the soundscape master volume (0.0f to 1.0f).
     */
    suspend fun setVolume(volume: Float)

    /**
     * Toggles whether the soundscape should automatically start when a focus session starts.
     */
    suspend fun setAutoPlay(enabled: Boolean)

    /**
     * Begins or resumes audio playback.
     */
    fun play()

    /**
     * Pauses audio playback.
     */
    fun pause()

    /**
     * Toggles play/pause state.
     */
    fun togglePlay()

    /**
     * Stops audio playback and releases resources if needed.
     */
    fun stop()
}
