package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.quovex.data.audio.FocusAudioEngine
import com.quovex.domain.model.SoundscapePresets
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SoundscapeRepositoryTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var audioEngine: FocusAudioEngine
    private lateinit var repository: SoundscapeRepositoryImpl

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)
        audioEngine = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putFloat(any(), any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor

        every { prefs.getString("selected_preset_id", any()) } returns SoundscapePresets.BINAURAL_ALPHA_10HZ.id
        every { prefs.getFloat("soundscape_volume", any()) } returns 0.75f
        every { prefs.getBoolean("auto_play_enabled", any()) } returns true

        repository = SoundscapeRepositoryImpl(context, audioEngine)
    }

    @Test
    fun `initial state loads preferences correctly`() {
        val state = repository.soundscapeState.value

        assertEquals(SoundscapePresets.BINAURAL_ALPHA_10HZ.id, state.selectedPreset.id)
        assertEquals(0.75f, state.volume, 0.01f)
        assertTrue(state.isAutoPlayWithTimerEnabled)
        assertFalse(state.isPlaying)
    }

    @Test
    fun `selectPreset updates selected preset and persists to preferences`() = runTest {
        repository.selectPreset(SoundscapePresets.RAIN_MONSOON)

        val state = repository.soundscapeState.value
        assertEquals(SoundscapePresets.RAIN_MONSOON.id, state.selectedPreset.id)
        verify { editor.putString("selected_preset_id", SoundscapePresets.RAIN_MONSOON.id) }
    }

    @Test
    fun `setVolume clamps volume between 0 and 1 and updates audio engine`() = runTest {
        repository.setVolume(1.5f)
        assertEquals(1.0f, repository.soundscapeState.value.volume, 0.01f)
        verify { audioEngine.updateVolume(1.0f) }

        repository.setVolume(-0.5f)
        assertEquals(0.0f, repository.soundscapeState.value.volume, 0.01f)
        verify { audioEngine.updateVolume(0.0f) }
    }

    @Test
    fun `setAutoPlay updates state and persists`() = runTest {
        repository.setAutoPlay(false)

        assertFalse(repository.soundscapeState.value.isAutoPlayWithTimerEnabled)
        verify { editor.putBoolean("auto_play_enabled", false) }
    }

    @Test
    fun `play and pause update state and trigger audio engine`() {
        repository.play()
        assertTrue(repository.soundscapeState.value.isPlaying)
        verify { audioEngine.startPlayback(SoundscapePresets.BINAURAL_ALPHA_10HZ, 0.75f) }

        repository.pause()
        assertFalse(repository.soundscapeState.value.isPlaying)
        verify { audioEngine.pausePlayback() }
    }

    @Test
    fun `togglePlay flips playing state`() {
        assertFalse(repository.soundscapeState.value.isPlaying)

        repository.togglePlay()
        assertTrue(repository.soundscapeState.value.isPlaying)

        repository.togglePlay()
        assertFalse(repository.soundscapeState.value.isPlaying)
    }
}
