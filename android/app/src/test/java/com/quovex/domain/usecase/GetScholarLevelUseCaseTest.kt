package com.quovex.domain.usecase

import com.quovex.domain.model.ScholarRank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetScholarLevelUseCaseTest {

    private lateinit var useCase: GetScholarLevelUseCase

    @Before
    fun setUp() {
        useCase = GetScholarLevelUseCase()
    }

    @Test
    fun `when XP is 0, returns Novice rank with 0 percent progress`() {
        val result = useCase(0L)
        assertEquals(ScholarRank.NOVICE, result.rank)
        assertEquals(1, result.rank.level)
        assertEquals(0L, result.currentXp)
        assertEquals(500L, result.xpRequiredForNextLevel)
        assertEquals(0f, result.progressPercent, 0.01f)
        assertEquals(ScholarRank.APPRENTICE, result.nextRank)
    }

    @Test
    fun `when XP is 250, returns Novice rank with 50 percent progress`() {
        val result = useCase(250L)
        assertEquals(ScholarRank.NOVICE, result.rank)
        assertEquals(250L, result.xpInCurrentLevel)
        assertEquals(250L, result.xpRequiredForNextLevel)
        assertEquals(0.5f, result.progressPercent, 0.01f)
    }

    @Test
    fun `when XP is 500, transitions to Apprentice rank`() {
        val result = useCase(500L)
        assertEquals(ScholarRank.APPRENTICE, result.rank)
        assertEquals(2, result.rank.level)
        assertEquals(0L, result.xpInCurrentLevel)
        assertEquals(1000L, result.xpRequiredForNextLevel)
        assertEquals(ScholarRank.SCHOLAR, result.nextRank)
    }

    @Test
    fun `when XP is 1500, transitions to Scholar rank`() {
        val result = useCase(1500L)
        assertEquals(ScholarRank.SCHOLAR, result.rank)
        assertEquals(3, result.rank.level)
    }

    @Test
    fun `when XP is 3500, transitions to Expert rank`() {
        val result = useCase(3500L)
        assertEquals(ScholarRank.EXPERT, result.rank)
        assertEquals(4, result.rank.level)
    }

    @Test
    fun `when XP is 8000, transitions to Grandmaster rank with no next rank`() {
        val result = useCase(8000L)
        assertEquals(ScholarRank.MASTER, result.rank)
        assertEquals(5, result.rank.level)
        assertNull(result.nextRank)
        assertEquals(1.0f, result.progressPercent, 0.01f)
    }
}
