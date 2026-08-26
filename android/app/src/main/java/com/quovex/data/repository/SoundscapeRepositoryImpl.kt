package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.quovex.data.audio.FocusAudioEngine
import com.quovex.domain.model.SoundscapePreset
import com.quovex.domain.model.SoundscapePresets
import com.quovex.domain.model.SoundscapeState
import com.quovex.domain.repository.SoundscapeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundscapeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioEngine: FocusAudioEngine
) : SoundscapeRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("quovex_soundscape_prefs", Context.MODE_PRIVATE)
    }

    private val _soundscapeState = MutableStateFlow(loadInitialState())
    override val soundscapeState: StateFlow<SoundscapeState> = _soundscapeState.asStateFlow()

    private fun loadInitialState(): SoundscapeState {
        val presetId = try {
            prefs.getString("selected_preset_id", SoundscapePresets.BINAURAL_ALPHA_10HZ.id)
                ?: SoundscapePresets.BINAURAL_ALPHA_10HZ.id
        } catch (_: Exception) {
            SoundscapePresets.BINAURAL_ALPHA_10HZ.id
        }
        val volume = try {
            prefs.getFloat("soundscape_volume", 0.75f)
        } catch (_: Exception) {
            0.75f
        }
        val autoPlay = try {
            prefs.getBoolean("auto_play_enabled", true)
        } catch (_: Exception) {
            true
        }

        return SoundscapeState(
            selectedPreset = SoundscapePresets.findById(presetId),
            isPlaying = false,
            volume = volume.coerceIn(0.0f, 1.0f),
            isAutoPlayWithTimerEnabled = autoPlay
        )
    }

    override suspend fun selectPreset(preset: SoundscapePreset) {
        try {
            prefs.edit().putString("selected_preset_id", preset.id).apply()
        } catch (_: Exception) {}

        val current = _soundscapeState.value
        val shouldPlay = current.isPlaying && preset != SoundscapePresets.NONE

        _soundscapeState.value = current.copy(
            selectedPreset = preset,
            isPlaying = shouldPlay
        )

        if (shouldPlay) {
            audioEngine.startPlayback(preset, current.volume)
        } else if (preset == SoundscapePresets.NONE) {
            audioEngine.stopPlayback()
        }
    }

    override suspend fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0.0f, 1.0f)
        try {
            prefs.edit().putFloat("soundscape_volume", clamped).apply()
        } catch (_: Exception) {}

        _soundscapeState.value = _soundscapeState.value.copy(volume = clamped)
        audioEngine.updateVolume(clamped)
    }

    override suspend fun setAutoPlay(enabled: Boolean) {
        try {
            prefs.edit().putBoolean("auto_play_enabled", enabled).apply()
        } catch (_: Exception) {}

        _soundscapeState.value = _soundscapeState.value.copy(isAutoPlayWithTimerEnabled = enabled)
    }

    override fun play() {
        val current = _soundscapeState.value
        if (current.selectedPreset == SoundscapePresets.NONE) return

        _soundscapeState.value = current.copy(isPlaying = true)
        audioEngine.startPlayback(current.selectedPreset, current.volume)
    }

    override fun pause() {
        _soundscapeState.value = _soundscapeState.value.copy(isPlaying = false)
        audioEngine.pausePlayback()
    }

    override fun togglePlay() {
        val current = _soundscapeState.value
        if (current.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    override fun stop() {
        _soundscapeState.value = _soundscapeState.value.copy(isPlaying = false)
        audioEngine.stopPlayback()
    }
}
