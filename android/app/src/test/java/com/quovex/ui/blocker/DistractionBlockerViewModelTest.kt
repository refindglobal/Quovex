package com.quovex.ui.blocker

import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.BlockedAppInfo
import com.quovex.domain.model.DistractionShieldState
import com.quovex.domain.usecase.GetInstalledAppsUseCase
import com.quovex.domain.usecase.ObserveBlockedAppsUseCase
import com.quovex.domain.usecase.ToggleBlockedAppUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DistractionBlockerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val observeBlockedAppsUseCase = mockk<ObserveBlockedAppsUseCase>()
    private val toggleBlockedAppUseCase = mockk<ToggleBlockedAppUseCase>(relaxed = true)
    private val getInstalledAppsUseCase = mockk<GetInstalledAppsUseCase>(relaxed = true)

    private val initialShieldState = DistractionShieldState(
        isShieldEnabled = true,
        isAccessibilityServiceEnabled = true,
        installedApps = listOf(
            BlockedAppInfo(
                packageName = "com.instagram.android",
                appName = "Instagram",
                category = AppCategory.SOCIAL,
                isBlocked = true,
                attemptsResistedCount = 3
            ),
            BlockedAppInfo(
                packageName = "com.google.android.youtube",
                appName = "YouTube",
                category = AppCategory.STREAMING,
                isBlocked = false,
                attemptsResistedCount = 0
            )
        ),
        totalAttemptsResistedToday = 3
    )

    private val fakeStateFlow = MutableStateFlow(initialShieldState)
    private lateinit var viewModel: DistractionBlockerViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { observeBlockedAppsUseCase() } returns fakeStateFlow

        viewModel = DistractionBlockerViewModel(
            observeBlockedAppsUseCase = observeBlockedAppsUseCase,
            toggleBlockedAppUseCase = toggleBlockedAppUseCase,
            getInstalledAppsUseCase = getInstalledAppsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state emits correct shield state`() = runTest {
        advanceUntilIdle()
        val state = viewModel.shieldState.value
        assertTrue(state.isShieldEnabled)
        assertTrue(state.isAccessibilityServiceEnabled)
        assertEquals(2, state.installedApps.size)
        assertEquals(1, state.blockedCount)
        assertEquals(3, state.totalAttemptsResistedToday)
    }

    @Test
    fun `toggleShield delegates to ToggleBlockedAppUseCase`() = runTest {
        viewModel.toggleShield(false)
        advanceUntilIdle()
        coVerify { toggleBlockedAppUseCase.setShieldEnabled(false) }
    }

    @Test
    fun `toggleAppBlocked delegates to ToggleBlockedAppUseCase`() = runTest {
        viewModel.toggleAppBlocked("com.google.android.youtube", true)
        advanceUntilIdle()
        coVerify { toggleBlockedAppUseCase.toggleApp("com.google.android.youtube", true) }
    }

    @Test
    fun `setCategoryBlocked delegates to ToggleBlockedAppUseCase`() = runTest {
        viewModel.setCategoryBlocked(AppCategory.SOCIAL, false)
        advanceUntilIdle()
        coVerify { toggleBlockedAppUseCase.setCategoryBlocked(AppCategory.SOCIAL, false) }
    }

    @Test
    fun `refreshInstalledApps delegates to GetInstalledAppsUseCase`() = runTest {
        viewModel.refreshInstalledApps()
        advanceUntilIdle()
        coVerify(atLeast = 1) { getInstalledAppsUseCase() }
    }
}
