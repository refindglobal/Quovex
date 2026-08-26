package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.quovex.domain.model.AttentivenessState
import com.quovex.domain.model.FocusFrameResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FocusDetectionRepositoryTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: FocusDetectionRepositoryImpl

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences(any(), any()) } returns prefs
        every { prefs.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { prefs.getBoolean("is_camera_focus_enabled", any()) } returns false

        repository = FocusDetectionRepositoryImpl(context)
    }

    @Test
    fun `initial state loads from preferences`() {
        val state = repository.focusTrackingState.value
        assertFalse(state.isEnabled)
        assertFalse(state.isCameraActive)
        assertEquals(AttentivenessState.CAMERA_OFF, state.attentivenessState)
        assertEquals(100, state.focusScore)
    }

    @Test
    fun `setTrackingEnabled updates state and persists`() = runTest {
        repository.setTrackingEnabled(true)

        assertTrue(repository.focusTrackingState.value.isEnabled)
        verify { editor.putBoolean("is_camera_focus_enabled", true) }
    }

    @Test
    fun `updateCameraPermission and setCameraActive update state`() {
        repository.updateCameraPermission(true)
        assertTrue(repository.focusTrackingState.value.hasCameraPermission)

        repository.setCameraActive(true)
        assertTrue(repository.focusTrackingState.value.isCameraActive)
        assertEquals(AttentivenessState.ATTENTIVE, repository.focusTrackingState.value.attentivenessState)
    }

    @Test
    fun `processFrame calculates attentiveness score accurately`() = runTest {
        repository.setTrackingEnabled(true)
        repository.setCameraActive(true)

        // 3 attentive frames
        repeat(3) {
            repository.processFrame(
                FocusFrameResult(
                    isFaceDetected = true,
                    isDrowsy = false,
                    isLookingAway = false
                )
            )
        }

        assertEquals(100, repository.focusTrackingState.value.focusScore)
        assertEquals(AttentivenessState.ATTENTIVE, repository.focusTrackingState.value.attentivenessState)
        assertFalse(repository.focusTrackingState.value.isWarningActive)
    }

    @Test
    fun `drowsiness triggers warning after 3 consecutive frames with eyes closed`() = runTest {
        repository.setTrackingEnabled(true)
        repository.setCameraActive(true)

        // 2 frames drowsy -> not yet triggered
        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = true, isLookingAway = false))
        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = true, isLookingAway = false))
        assertFalse(repository.focusTrackingState.value.isWarningActive)

        // 3rd frame drowsy -> triggers drowsiness alert
        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = true, isLookingAway = false))
        assertTrue(repository.focusTrackingState.value.isWarningActive)
        assertEquals(AttentivenessState.DROWSY_EYES_CLOSED, repository.focusTrackingState.value.attentivenessState)
        assertEquals(1, repository.focusTrackingState.value.drowsinessCount)
    }

    @Test
    fun `looking away triggers warning after 2 consecutive frames`() = runTest {
        repository.setTrackingEnabled(true)
        repository.setCameraActive(true)

        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = false, isLookingAway = true))
        assertFalse(repository.focusTrackingState.value.isWarningActive)

        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = false, isLookingAway = true))
        assertTrue(repository.focusTrackingState.value.isWarningActive)
        assertEquals(AttentivenessState.LOOKING_AWAY, repository.focusTrackingState.value.attentivenessState)
        assertEquals(1, repository.focusTrackingState.value.distractionsCount)
    }

    @Test
    fun `resetSession resets metrics to initial baseline`() = runTest {
        repository.setTrackingEnabled(true)
        repository.setCameraActive(true)

        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = true, isLookingAway = false))
        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = true, isLookingAway = false))
        repository.processFrame(FocusFrameResult(isFaceDetected = true, isDrowsy = true, isLookingAway = false))

        assertEquals(1, repository.focusTrackingState.value.drowsinessCount)

        repository.resetSession()
        assertEquals(0, repository.focusTrackingState.value.drowsinessCount)
        assertEquals(0, repository.focusTrackingState.value.distractionsCount)
        assertEquals(100, repository.focusTrackingState.value.focusScore)
        assertFalse(repository.focusTrackingState.value.isWarningActive)
    }
}
