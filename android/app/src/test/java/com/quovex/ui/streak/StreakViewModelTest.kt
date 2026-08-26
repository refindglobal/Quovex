package com.quovex.ui.streak

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.entity.StreakEntity
import com.quovex.data.repository.StreakRepositoryImpl
import com.quovex.domain.model.UserProfile
import com.quovex.domain.usecase.CheckStreakMilestoneUseCase
import com.quovex.domain.usecase.FakeSessionDao
import com.quovex.domain.usecase.FakeStreakDao
import com.quovex.domain.usecase.FakeUserStatsDao
import com.quovex.domain.usecase.LogStreakReflectionUseCase
import com.quovex.domain.usecase.ManageStreakUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreakViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var streakDao: FakeStreakDao
    private lateinit var sessionDao: FakeSessionDao
    private lateinit var userStatsDao: FakeUserStatsDao
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var streakRepository: StreakRepositoryImpl
    private lateinit var manageStreakUseCase: ManageStreakUseCase
    private lateinit var logStreakReflectionUseCase: LogStreakReflectionUseCase
    private lateinit var checkStreakMilestoneUseCase: CheckStreakMilestoneUseCase

    private lateinit var viewModel: StreakViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        streakDao = FakeStreakDao()
        sessionDao = FakeSessionDao()
        userStatsDao = FakeUserStatsDao()
        userPreferencesManager = UserPreferencesManager(null)
        streakRepository = StreakRepositoryImpl(streakDao, sessionDao, userStatsDao, userPreferencesManager)
        manageStreakUseCase = ManageStreakUseCase(streakRepository)
        logStreakReflectionUseCase = LogStreakReflectionUseCase(streakRepository)
        checkStreakMilestoneUseCase = CheckStreakMilestoneUseCase(streakRepository)

        userPreferencesManager.saveUserProfile(
            UserProfile(
                streakDays = 14,
                rescueTokens = 2
            )
        )

        viewModel = StreakViewModel(
            manageStreakUseCase = manageStreakUseCase,
            logStreakReflectionUseCase = logStreakReflectionUseCase,
            checkStreakMilestoneUseCase = checkStreakMilestoneUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads streak info and milestones`() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(14, state.streakInfo.currentStreak)
        assertEquals(2, state.streakInfo.rescueTokens)
        assertEquals(4, state.milestones.size)
    }

    @Test
    fun `spendRescueToken triggers redemption and updates message`() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.spendRescueToken()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.streakInfo.rescueTokens)
        assertNotNull(state.actionMessage)
        assertTrue(state.actionMessage!!.contains("Protected"))
    }

    @Test
    fun `saveReflection logs note to cemetery tombstone`() = runTest(testDispatcher) {
        val id = streakDao.insertStreak(
            StreakEntity(
                streakDays = 10,
                startDate = System.currentTimeMillis() - 864000000L,
                endDate = System.currentTimeMillis(),
                isBroken = true,
                causeOfDeath = "Missed daily focus goal",
                reflectionNote = null,
                tokensUsed = 0
            )
        )
        advanceUntilIdle()

        val tombstone = viewModel.uiState.value.tombstones.find { it.id == id }
        assertNotNull(tombstone)

        viewModel.openReflectionDialog(tombstone!!)
        viewModel.saveReflection("Reflecting on study habits.")
        advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        val updatedTombstone = updatedState.tombstones.find { it.id == id }
        assertEquals("Reflecting on study habits.", updatedTombstone?.reflectionNote)
    }
}
