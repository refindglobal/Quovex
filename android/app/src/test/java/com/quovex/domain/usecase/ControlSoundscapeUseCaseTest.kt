package com.quovex.domain.usecase

import com.quovex.domain.model.SoundscapePresets
import com.quovex.domain.repository.SoundscapeRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ControlSoundscapeUseCaseTest {

    private lateinit var soundscapeRepository: SoundscapeRepository
    private lateinit var useCase: ControlSoundscapeUseCase

    @Before
    fun setUp() {
        soundscapeRepository = mockk(relaxed = true)
        useCase = ControlSoundscapeUseCase(soundscapeRepository)
    }

    @Test
    fun `selectPreset delegates to repository`() = runTest {
        useCase.selectPreset(SoundscapePresets.BROWN_NOISE)
        coVerify { soundscapeRepository.selectPreset(SoundscapePresets.BROWN_NOISE) }
    }

    @Test
    fun `setVolume delegates to repository with clamping`() = runTest {
        useCase.setVolume(0.85f)
        coVerify { soundscapeRepository.setVolume(0.85f) }
    }

    @Test
    fun `setAutoPlay delegates to repository`() = runTest {
        useCase.setAutoPlay(true)
        coVerify { soundscapeRepository.setAutoPlay(true) }
    }

    @Test
    fun `play and pause delegate to repository`() {
        useCase.play()
        verify { soundscapeRepository.play() }

        useCase.pause()
        verify { soundscapeRepository.pause() }
    }

    @Test
    fun `togglePlay and stop delegate to repository`() {
        useCase.togglePlay()
        verify { soundscapeRepository.togglePlay() }

        useCase.stop()
        verify { soundscapeRepository.stop() }
    }
}
