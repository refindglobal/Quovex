package com.quovex.domain.usecase

import com.quovex.data.local.dao.CommunityDao
import com.quovex.data.local.entity.LeaderboardCacheEntity
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.LeaderboardEntry
import com.quovex.domain.model.LeaderboardType
import com.quovex.domain.model.RankTrend
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLeaderboardUseCaseTest {

    private lateinit var firestoreService: FirebaseFirestoreService
    private lateinit var communityDao: CommunityDao
    private lateinit var useCase: GetLeaderboardUseCase

    @Before
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        communityDao = mockk(relaxed = true)
        useCase = GetLeaderboardUseCase(firestoreService, communityDao)
    }

    /** Returns Firestore data when online — entries are ranked and persisted to cache. */
    @Test
    fun `execute returns remote entries when Firestore succeeds`() = runTest {
        val remoteEntries = listOf(
            LeaderboardEntry(
                userId = "u1", userName = "Alice", avatarId = 1, scholarRank = "Scholar I",
                studyMinutes = 420, xp = 1200, rank = 1, isCurrentUser = false, trend = RankTrend.UP
            ),
            LeaderboardEntry(
                userId = "u2", userName = "Bob", avatarId = 2, scholarRank = "Scholar II",
                studyMinutes = 300, xp = 900, rank = 2, isCurrentUser = true, trend = RankTrend.SAME
            )
        )
        coEvery { firestoreService.getWeeklyLeaderboard(any(), any(), any()) } returns remoteEntries
        coEvery { communityDao.evictStaleLeaderboard(any()) } returns Unit
        coEvery { communityDao.upsertLeaderboard(any()) } returns Unit

        val result = useCase.execute(LeaderboardType.GLOBAL, "ALL", "u2")

        assertEquals(2, result.size)
        assertEquals("Alice", result[0].userName)
        assertEquals(1, result[0].rank)
        // Verify cache was written
        val slot = slot<List<LeaderboardCacheEntity>>()
        coVerify { communityDao.upsertLeaderboard(capture(slot)) }
        assertEquals(2, slot.captured.size)
        assertEquals("GLOBAL", slot.captured[0].leaderboardType)
    }

    /** Returns cached data when Firestore returns empty (offline scenario). */
    @Test
    fun `execute falls back to cached entries when Firestore returns empty`() = runTest {
        val cachedEntities = listOf(
            LeaderboardCacheEntity(
                leaderboardType = "GLOBAL", subjectFilter = "ALL", userId = "u3",
                userName = "Carol", avatarId = 3, scholarRank = "Novice Scholar",
                studyMinutes = 150, xp = 400, rank = 1, isCurrentUser = false,
                trend = "SAME", weekKey = "2026-W35"
            )
        )
        coEvery { firestoreService.getWeeklyLeaderboard(any(), any(), any()) } returns emptyList()
        coEvery { communityDao.getLeaderboardFlow(any(), any(), any()) } returns flowOf(cachedEntities)
        coEvery { communityDao.evictStaleLeaderboard(any()) } returns Unit

        val result = useCase.execute(LeaderboardType.GLOBAL)

        assertEquals(1, result.size)
        assertEquals("Carol", result[0].userName)
        assertEquals(150, result[0].studyMinutes)
    }

    /** Stale cache eviction is always called before returning data. */
    @Test
    fun `execute always evicts stale cache before fetching`() = runTest {
        coEvery { firestoreService.getWeeklyLeaderboard(any(), any(), any()) } returns emptyList()
        coEvery { communityDao.getLeaderboardFlow(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { communityDao.evictStaleLeaderboard(any()) } returns Unit

        useCase.execute(LeaderboardType.FRIENDS)

        coVerify(exactly = 1) { communityDao.evictStaleLeaderboard(any()) }
    }

    /** Subject filter is forwarded to both Firestore and cache correctly. */
    @Test
    fun `execute passes subject filter to firestore`() = runTest {
        coEvery { firestoreService.getWeeklyLeaderboard(any(), any(), any()) } returns emptyList()
        coEvery { communityDao.getLeaderboardFlow(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { communityDao.evictStaleLeaderboard(any()) } returns Unit

        useCase.execute(LeaderboardType.SUBJECT, subjectFilter = "Physics")

        coVerify { firestoreService.getWeeklyLeaderboard(LeaderboardType.SUBJECT, "Physics", any()) }
    }

    /** Returns empty list gracefully when both Firestore and cache are empty. */
    @Test
    fun `execute returns empty list when both remote and cache are empty`() = runTest {
        coEvery { firestoreService.getWeeklyLeaderboard(any(), any(), any()) } returns emptyList()
        coEvery { communityDao.getLeaderboardFlow(any(), any(), any()) } returns flowOf(emptyList())
        coEvery { communityDao.evictStaleLeaderboard(any()) } returns Unit

        val result = useCase.execute(LeaderboardType.GLOBAL)

        assertTrue(result.isEmpty())
    }
}
