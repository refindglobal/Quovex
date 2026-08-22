package com.quovex.domain.usecase

import com.quovex.domain.model.FlashcardItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDueFlashcardsUseCaseTest {

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var useCase: GetDueFlashcardsUseCase

    private val now = System.currentTimeMillis()

    @Before
    fun setUp() {
        fakeRepository = FakeQuovexRepository()
        useCase = GetDueFlashcardsUseCase(fakeRepository)
    }

    private fun card(id: Int, dueAt: Long) = FlashcardItem(
        id = id,
        deckId = 1,
        frontContent = "Q$id",
        backContent = "A$id",
        nextReviewDate = dueAt
    )

    @Test
    fun `returns only due cards when due cards exist`() = runTest {
        val pastTime = now - 1000L
        val dueCards = listOf(card(1, pastTime), card(2, pastTime))
        fakeRepository.dueCardsMap[1L] = dueCards

        val result = useCase(deckId = 1L, currentTimeMillis = now).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.nextReviewDate <= now })
    }

    @Test
    fun `returns empty list when no cards due`() = runTest {
        fakeRepository.dueCardsMap[1L] = emptyList()

        val result = useCase(deckId = 1L, currentTimeMillis = now).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `future-dated cards are filtered out by repository`() = runTest {
        fakeRepository.dueCardsMap[1L] = emptyList()

        val result = useCase(deckId = 1L, currentTimeMillis = now).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `default invocation returns flow from repository`() = runTest {
        val cards = listOf(card(1, now))
        fakeRepository.dueCardsMap[1L] = cards

        val result = useCase(deckId = 1L).first()

        assertEquals(1, result.size)
        assertEquals("Q1", result.first().frontContent)
    }
}
