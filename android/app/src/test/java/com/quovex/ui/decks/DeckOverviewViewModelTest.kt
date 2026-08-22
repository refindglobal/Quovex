package com.quovex.ui.decks

import androidx.lifecycle.SavedStateHandle
import com.quovex.domain.model.DeckStats
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetDeckStatsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeckOverviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var getDeckStatsUseCase: GetDeckStatsUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        getDeckStatsUseCase = GetDeckStatsUseCase(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(deckId: Int = 5): DeckOverviewViewModel {
        return DeckOverviewViewModel(
            savedStateHandle = SavedStateHandle(mapOf("deckId" to deckId)),
            getDeckStatsUseCase = getDeckStatsUseCase
        )
    }

    private fun sampleStats(
        dueCards: Int = 3,
        totalCards: Int = 10,
        masteredCards: Int = 5
    ) = DeckStats(
        deckId = 5,
        title = "Thermodynamics",
        subject = "Physics",
        totalCards = totalCards,
        dueCards = dueCards,
        masteredCards = masteredCards,
        learningCards = (totalCards - masteredCards - dueCards).coerceAtLeast(0)
    )

    @Test
    fun `loads deck stats successfully`() = runTest {
        val stats = sampleStats()
        fakeRepository.deckStatsMap[5] = stats

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Thermodynamics", state.stats?.title)
    }

    @Test
    fun `canStudyDue is true when due cards exist`() = runTest {
        fakeRepository.deckStatsMap[5] = sampleStats(dueCards = 3)

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.canStudyDue)
    }

    @Test
    fun `canStudyDue is false when zero due cards`() = runTest {
        fakeRepository.deckStatsMap[5] = sampleStats(dueCards = 0)

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.canStudyDue)
    }

    @Test
    fun `isAllCaughtUp is true when deck has cards but none due`() = runTest {
        fakeRepository.deckStatsMap[5] = sampleStats(dueCards = 0, totalCards = 10)

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isAllCaughtUp)
    }

    @Test
    fun `isDeckEmpty is true when totalCards is zero`() = runTest {
        fakeRepository.deckStatsMap[5] = sampleStats(totalCards = 0, dueCards = 0, masteredCards = 0)

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isDeckEmpty)
    }

    @Test
    fun `canReviewAll is true when deck has cards`() = runTest {
        fakeRepository.deckStatsMap[5] = sampleStats(totalCards = 5)

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.canReviewAll)
    }

    @Test
    fun `error state is set when repository throws`() = runTest {
        fakeRepository.shouldThrowOnDeckStats = true

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.stats)
        assertEquals("Failed to load deck information", state.error)
    }

    @Test
    fun `mastery percent is correct`() = runTest {
        fakeRepository.deckStatsMap[5] = sampleStats(totalCards = 10, masteredCards = 5)

        val vm = buildViewModel(deckId = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(50, vm.uiState.value.stats?.masteryPercent)
    }
}
