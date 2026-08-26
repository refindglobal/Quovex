package com.quovex.domain.usecase

import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.repository.FocusDetectionRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ControlFocusDetectionUseCaseTest {

    private lateinit var repository: FocusDetectionRepository
    private lateinit var useCase: ControlFocusDetectionUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ControlFocusDetectionUseCase(repository)
    }

    @Test
    fun `setTrackingEnabled delegates to repository`() = runTest {
        useCase.setTrackingEnabled(true)
        coVerify { repository.setTrackingEnabled(true) }
    }

    @Test
    fun `updateCameraPermission and setCameraActive delegate to repository`() {
        useCase.updateCameraPermission(true)
        verify { repository.updateCameraPermission(true) }

        useCase.setCameraActive(true)
        verify { repository.setCameraActive(true) }
    }

    @Test
    fun `processFrame delegates to repository`() {
        val frame = FocusFrameResult(isFaceDetected = true, isDrowsy = false, isLookingAway = false)
        useCase.processFrame(frame)
        verify { repository.processFrame(frame) }
    }

    @Test
    fun `resetSession delegates to repository`() {
        useCase.resetSession()
        verify { repository.resetSession() }
    }
}
