package com.quovex.domain.usecase

import com.quovex.domain.model.DeckStats
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDeckStatsUseCaseTest {

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var useCase: GetDeckStatsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeQuovexRepository()
        useCase = GetDeckStatsUseCase(fakeRepository)
    }

    private fun sampleStats(deckId: Int = 1, total: Int = 10, due: Int = 3, mastered: Int = 4) =
        DeckStats(
            deckId = deckId,
            title = "Test Deck",
            subject = "Physics",
            totalCards = total,
            dueCards = due,
            masteredCards = mastered,
            learningCards = (total - mastered - due).coerceAtLeast(0)
        )

    @Test
    fun `returns stats for existing deck`() = runTest {
        val stats = sampleStats()
        fakeRepository.deckStatsMap[1] = stats

        val result = useCase(deckId = 1)

        assertEquals(stats, result)
        assertEquals("Test Deck", result?.title)
    }

    @Test
    fun `returns null when deck does not exist`() = runTest {
        val result = useCase(deckId = 999)
        assertNull(result)
    }

    @Test
    fun `mastery percent is zero for empty deck`() = runTest {
        val stats = sampleStats(total = 0, due = 0, mastered = 0)
        fakeRepository.deckStatsMap[1] = stats

        val result = useCase(deckId = 1)

        assertEquals(0, result?.masteryPercent)
    }

    @Test
    fun `mastery percent calculated correctly`() = runTest {
        // 5 mastered out of 20 = 25%
        val stats = sampleStats(total = 20, due = 2, mastered = 5)
        fakeRepository.deckStatsMap[1] = stats

        val result = useCase(deckId = 1)

        assertEquals(25, result?.masteryPercent)
    }

    @Test
    fun `isAllCaughtUp when no cards due`() = runTest {
        val stats = sampleStats(due = 0, total = 10)
        fakeRepository.deckStatsMap[1] = stats

        val result = useCase(deckId = 1)

        assertTrue(result?.isAllCaughtUp == true)
    }

    @Test
    fun `isDeckEmpty when totalCards is zero`() = runTest {
        val stats = sampleStats(total = 0, due = 0, mastered = 0)
        fakeRepository.deckStatsMap[1] = stats

        val result = useCase(deckId = 1)

        assertTrue(result?.isEmpty == true)
    }
}
