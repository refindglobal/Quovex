package com.quovex.ui.decks

import com.quovex.domain.model.DeckStats
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetDeckStatsForAllDecksUseCase
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
class DeckListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var getDeckStatsForAllDecksUseCase: GetDeckStatsForAllDecksUseCase

    private val sampleDeckStats = listOf(
        DeckStats(
            deckId = 1,
            title = "Thermodynamics",
            subject = "Physics",
            totalCards = 20,
            dueCards = 5,
            masteredCards = 10,
            learningCards = 5
        ),
        DeckStats(
            deckId = 2,
            title = "Organic Reactions",
            subject = "Chemistry",
            totalCards = 15,
            dueCards = 0,
            masteredCards = 15,
            learningCards = 0
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        getDeckStatsForAllDecksUseCase = GetDeckStatsForAllDecksUseCase(fakeRepository)
        fakeRepository.deckStatsList = sampleDeckStats
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): DeckListViewModel {
        return DeckListViewModel(
            getDeckStatsForAllDecksUseCase = getDeckStatsForAllDecksUseCase,
            repository = fakeRepository
        )
    }

    @Test
    fun `loads all deck statistics on init`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(2, state.decks.size)
        assertEquals("Thermodynamics", state.decks[0].title)
        assertEquals(5, state.decks[0].dueCards)
        assertEquals(50, state.decks[0].masteryPercent)
    }

    @Test
    fun `category filter filters deck list correctly`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectCategory("Chemistry")
        val state = vm.uiState.value
        assertEquals("Chemistry", state.selectedCategory)
        assertEquals(1, state.filteredDecks.size)
        assertEquals("Organic Reactions", state.filteredDecks[0].title)

        vm.selectCategory("All")
        assertEquals(2, vm.uiState.value.filteredDecks.size)
    }

    @Test
    fun `isAllCaughtUp is true only when all decks have zero due cards`() = runTest {
        val allCaughtUpList = listOf(
            DeckStats(1, "D1", "Physics", 10, 0, 10, 0),
            DeckStats(2, "D2", "Maths", 10, 0, 10, 0)
        )
        fakeRepository.deckStatsList = allCaughtUpList

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isAllCaughtUp)
    }

    @Test
    fun `error state is set when flow errors`() = runTest {
        fakeRepository.shouldThrowOnDeckStats = true

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Failed to load decks", state.error)
    }
}
