package com.quovex.domain.usecase

import com.quovex.data.local.dao.CommunityDao
import com.quovex.data.local.entity.FriendEntity
import com.quovex.data.local.entity.StudyBattleEntity
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.BattleStatus
import com.quovex.domain.model.FriendProfile
import com.quovex.domain.model.StudyBattle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ManageFriendsAndBattlesUseCaseTest {

    private lateinit var firestoreService: FirebaseFirestoreService
    private lateinit var communityDao: CommunityDao
    private lateinit var useCase: ManageFriendsAndBattlesUseCase

    private val userId = "user_abc"

    @Before
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        communityDao = mockk(relaxed = true)
        useCase = ManageFriendsAndBattlesUseCase(firestoreService, communityDao)
    }

    /** observeFriends maps FriendEntity → FriendProfile correctly. */
    @Test
    fun `observeFriends maps cached entities to domain models`() = runTest {
        val entity = FriendEntity(
            friendId = "f1", username = "bob99", displayName = "Bob",
            avatarId = 2, scholarRank = "Scholar II", streakDays = 14,
            totalStudyHours = 52.5f, topSubject = "Physics", isStudyingNow = true
        )
        every { communityDao.getFriendsFlow() } returns flowOf(listOf(entity))

        val emitted = mutableListOf<List<FriendProfile>>()
        useCase.observeFriends().collect { emitted.add(it) }

        assertEquals(1, emitted.size)
        val friend = emitted[0][0]
        assertEquals("f1", friend.friendId)
        assertEquals("Bob", friend.displayName)
        assertEquals(14, friend.streakDays)
        assertTrue(friend.isStudyingNow)
    }

    /** observeBattles maps StudyBattleEntity → StudyBattle correctly. */
    @Test
    fun `observeBattles maps cached entities to domain models`() = runTest {
        val now = System.currentTimeMillis()
        val entity = StudyBattleEntity(
            battleId = "b1", challengerId = userId, challengerName = "Alice",
            challengerAvatarId = 1, opponentId = "opp1", opponentName = "Charlie",
            opponentAvatarId = 3, targetExam = "JEE Advanced",
            challengerMinutes = 240, opponentMinutes = 180,
            startDateMillis = now, endDateMillis = now + 604800000L,
            status = "ACTIVE"
        )
        every { communityDao.getBattlesForUserFlow(userId) } returns flowOf(listOf(entity))

        val emitted = mutableListOf<List<StudyBattle>>()
        useCase.observeBattles(userId).collect { emitted.add(it) }

        assertEquals(1, emitted.size)
        val battle = emitted[0][0]
        assertEquals("b1", battle.battleId)
        assertEquals(BattleStatus.ACTIVE, battle.status)
        assertEquals(240, battle.challengerMinutes)
    }

    /** createBattle calls Firestore and persists result locally on success. */
    @Test
    fun `createBattle persists to cache on Firestore success`() = runTest {
        val battle = StudyBattle(
            challengerId = userId, challengerName = "Alice", challengerAvatarId = 1,
            opponentId = "opp1", opponentName = "Charlie", opponentAvatarId = 3,
            targetExam = "NEET UG", challengerMinutes = 0, opponentMinutes = 0,
            status = BattleStatus.PENDING
        )
        coEvery { firestoreService.createStudyBattle(battle) } returns Result.success("new_battle_id")
        coEvery { communityDao.upsertBattles(any()) } returns Unit

        val result = useCase.createBattle(battle)

        assertTrue(result.isSuccess)
        assertEquals("new_battle_id", result.getOrThrow())
        coVerify { communityDao.upsertBattles(match { it[0].battleId == "new_battle_id" }) }
    }

    /** createBattle does NOT persist when Firestore fails. */
    @Test
    fun `createBattle does not write to cache on Firestore failure`() = runTest {
        val battle = StudyBattle(challengerId = userId, challengerName = "Alice")
        coEvery { firestoreService.createStudyBattle(battle) } returns Result.failure(RuntimeException("Quota exceeded"))

        val result = useCase.createBattle(battle)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { communityDao.upsertBattles(any()) }
    }

    /** calculateLeadMargin returns positive when challenger leads. */
    @Test
    fun `calculateLeadMargin returns positive value when challenger leads`() {
        val battle = StudyBattle(challengerMinutes = 300, opponentMinutes = 200)
        assertEquals(100, useCase.calculateLeadMargin(battle))
    }

    /** calculateLeadMargin returns negative when opponent leads. */
    @Test
    fun `calculateLeadMargin returns negative value when opponent leads`() {
        val battle = StudyBattle(challengerMinutes = 100, opponentMinutes = 250)
        assertEquals(-150, useCase.calculateLeadMargin(battle))
    }

    /** progressPercent clamps at 1.0 for minutes > 600. */
    @Test
    fun `progressPercent clamps to 1f for values exceeding weekly goal`() {
        assertEquals(1f, useCase.progressPercent(700))
    }

    /** progressPercent returns proportional value within range. */
    @Test
    fun `progressPercent returns correct proportion for 300 minutes`() {
        assertEquals(0.5f, useCase.progressPercent(300))
    }
}
