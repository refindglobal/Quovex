package com.quovex.ui.flashcards

import androidx.lifecycle.SavedStateHandle
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetDueFlashcardsUseCase
import com.quovex.domain.usecase.GetFlashcardsForDeckUseCase
import com.quovex.domain.usecase.ReviewCardUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlashcardPlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var getDueUseCase: GetDueFlashcardsUseCase
    private lateinit var getAllCardsUseCase: GetFlashcardsForDeckUseCase
    private lateinit var reviewCardUseCase: ReviewCardUseCase

    private val sampleCards = listOf(
        FlashcardItem(id = 1, deckId = 10, frontContent = "Q1", backContent = "A1", nextReviewDate = 0L),
        FlashcardItem(id = 2, deckId = 10, frontContent = "Q2", backContent = "A2", nextReviewDate = 0L),
        FlashcardItem(id = 3, deckId = 10, frontContent = "Q3", backContent = "A3", nextReviewDate = 0L)
    )

    private val sampleDeck = DeckItem(id = 10, title = "Physics", subject = "Physics", totalCards = 3)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        getDueUseCase = GetDueFlashcardsUseCase(fakeRepository)
        getAllCardsUseCase = GetFlashcardsForDeckUseCase(fakeRepository)
        reviewCardUseCase = ReviewCardUseCase(fakeRepository)

        fakeRepository.decksList = listOf(sampleDeck)
        fakeRepository.mostRecentDeck = sampleDeck
        fakeRepository.dueCardsMap[10L] = sampleCards
        fakeRepository.allCardsMap[10L] = sampleCards
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(reviewAll: Boolean = false, deckId: Int = 10): FlashcardPlayerViewModel {
        val handle = SavedStateHandle(mapOf("deckId" to deckId, "reviewAll" to reviewAll))
        return FlashcardPlayerViewModel(handle, getDueUseCase, getAllCardsUseCase, reviewCardUseCase, fakeRepository)
    }

    @Test
    fun `loads due cards in DUE_ONLY mode on init`() = runTest {
        val vm = buildViewModel(reviewAll = false)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.cards.size)
        assertEquals(StudyMode.DUE_ONLY, state.studyMode)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loads all cards in REVIEW_ALL mode on init`() = runTest {
        val vm = buildViewModel(reviewAll = true)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(3, state.cards.size)
        assertEquals(StudyMode.REVIEW_ALL, state.studyMode)
    }

    @Test
    fun `deck title is loaded from repository`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Physics", vm.uiState.value.deckTitle)
    }

    @Test
    fun `flipCard toggles isFlipped`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isFlipped)
        vm.flipCard()
        assertTrue(vm.uiState.value.isFlipped)
        vm.flipCard()
        assertFalse(vm.uiState.value.isFlipped)
    }

    @Test
    fun `first card is at index 0`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, vm.uiState.value.currentCardIndex)
        assertEquals(sampleCards[0], vm.uiState.value.currentCard)
    }

    @Test
    fun `submitReviewRating advances to next card and records quality`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.submitReviewRating(4)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.currentCardIndex)
        assertFalse(state.isFlipped)
        assertEquals(1, state.session.goodCount)
        assertEquals(1, state.session.reviewedCount)
        assertEquals(4, fakeRepository.lastReviewedQuality)
    }

    @Test
    fun `again quality increments againCount`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.submitReviewRating(0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.session.againCount)
        assertEquals(0, fakeRepository.lastReviewedQuality)
    }

    @Test
    fun `hard quality increments hardCount`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.submitReviewRating(3)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.session.hardCount)
        assertEquals(3, fakeRepository.lastReviewedQuality)
    }

    @Test
    fun `easy quality increments easyCount`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.submitReviewRating(5)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.session.easyCount)
        assertEquals(5, fakeRepository.lastReviewedQuality)
    }

    @Test
    fun `reviewing last card triggers isDeckComplete`() = runTest {
        fakeRepository.dueCardsMap[10L] = listOf(sampleCards[0])
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isDeckComplete)
        vm.submitReviewRating(4)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isDeckComplete)
    }

    @Test
    fun `restartSession resets index and session counts`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.submitReviewRating(4)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.restartSession()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(0, state.currentCardIndex)
        assertFalse(state.isDeckComplete)
        assertEquals(0, state.session.reviewedCount)
        assertEquals(0, state.session.goodCount)
    }

    @Test
    fun `zero due cards results in isNoDueCards state`() = runTest {
        fakeRepository.dueCardsMap[10L] = emptyList()

        val vm = buildViewModel(reviewAll = false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isNoDueCards)
        assertFalse(vm.uiState.value.isDeckComplete)
    }

    @Test
    fun `progress text is correct at start`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("1 / 3", vm.uiState.value.progressText)
    }
}
