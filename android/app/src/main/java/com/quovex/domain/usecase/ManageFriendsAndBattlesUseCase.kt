package com.quovex.domain.usecase

import com.quovex.data.local.dao.CommunityDao
import com.quovex.data.local.entity.FriendEntity
import com.quovex.data.local.entity.StudyBattleEntity
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.BattleStatus
import com.quovex.domain.model.FriendProfile
import com.quovex.domain.model.StudyBattle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Manages the Friends list and 1v1 Study Battle lifecycle.
 *
 * - Friends are fetched from Firestore and cached locally in the [friends] Room table.
 * - Battles are fetched from Firestore and cached locally in the [study_battles] Room table.
 * - All reads are backed by local cache for offline resilience.
 */
class ManageFriendsAndBattlesUseCase @Inject constructor(
    private val firestoreService: FirebaseFirestoreService,
    private val communityDao: CommunityDao
) {

    // ---- Friends ----

    /**
     * Observe the locally-cached friends list as a reactive [Flow].
     * Also triggers a background Firestore refresh to keep the cache fresh.
     */
    fun observeFriends(): Flow<List<FriendProfile>> =
        communityDao.getFriendsFlow().map { entities ->
            entities.map { e ->
                FriendProfile(
                    friendId = e.friendId,
                    username = e.username,
                    displayName = e.displayName,
                    avatarId = e.avatarId,
                    scholarRank = e.scholarRank,
                    streakDays = e.streakDays,
                    totalStudyHours = e.totalStudyHours,
                    topSubject = e.topSubject,
                    isStudyingNow = e.isStudyingNow
                )
            }
        }

    /**
     * Refresh friend cache for [userId] from Firestore and persist to Room.
     * Called once on ViewModel init and on manual pull-to-refresh.
     */
    suspend fun refreshFriends(userId: String) {
        firestoreService.getFriendsFlow(userId).collect { profiles ->
            val entities = profiles.map { p ->
                FriendEntity(
                    friendId = p.friendId,
                    username = p.username,
                    displayName = p.displayName,
                    avatarId = p.avatarId,
                    scholarRank = p.scholarRank,
                    streakDays = p.streakDays,
                    totalStudyHours = p.totalStudyHours,
                    topSubject = p.topSubject,
                    isStudyingNow = p.isStudyingNow
                )
            }
            communityDao.clearFriends()
            communityDao.upsertFriends(entities)
        }
    }

    // ---- Study Battles ----

    /**
     * Observe the locally-cached battles for [userId] as a reactive [Flow].
     */
    fun observeBattles(userId: String): Flow<List<StudyBattle>> =
        communityDao.getBattlesForUserFlow(userId).map { entities ->
            entities.map { e ->
                StudyBattle(
                    battleId = e.battleId,
                    challengerId = e.challengerId,
                    challengerName = e.challengerName,
                    challengerAvatarId = e.challengerAvatarId,
                    opponentId = e.opponentId,
                    opponentName = e.opponentName,
                    opponentAvatarId = e.opponentAvatarId,
                    targetExam = e.targetExam,
                    challengerMinutes = e.challengerMinutes,
                    opponentMinutes = e.opponentMinutes,
                    startDateMillis = e.startDateMillis,
                    endDateMillis = e.endDateMillis,
                    status = try { BattleStatus.valueOf(e.status) } catch (_: Exception) { BattleStatus.PENDING }
                )
            }
        }

    /**
     * Refresh battle cache for [userId] from Firestore and persist to Room.
     */
    suspend fun refreshBattles(userId: String) {
        firestoreService.getStudyBattlesFlow(userId).collect { battles ->
            val entities = battles.map { b ->
                StudyBattleEntity(
                    battleId = b.battleId,
                    challengerId = b.challengerId,
                    challengerName = b.challengerName,
                    challengerAvatarId = b.challengerAvatarId,
                    opponentId = b.opponentId,
                    opponentName = b.opponentName,
                    opponentAvatarId = b.opponentAvatarId,
                    targetExam = b.targetExam,
                    challengerMinutes = b.challengerMinutes,
                    opponentMinutes = b.opponentMinutes,
                    startDateMillis = b.startDateMillis,
                    endDateMillis = b.endDateMillis,
                    status = b.status.name
                )
            }
            communityDao.clearBattles()
            communityDao.upsertBattles(entities)
        }
    }

    /**
     * Create a new 1v1 battle and persist locally on success.
     */
    suspend fun createBattle(battle: StudyBattle): Result<String> {
        val result = firestoreService.createStudyBattle(battle)
        if (result.isSuccess) {
            val id = result.getOrThrow()
            communityDao.upsertBattles(listOf(
                StudyBattleEntity(
                    battleId = id,
                    challengerId = battle.challengerId,
                    challengerName = battle.challengerName,
                    challengerAvatarId = battle.challengerAvatarId,
                    opponentId = battle.opponentId,
                    opponentName = battle.opponentName,
                    opponentAvatarId = battle.opponentAvatarId,
                    targetExam = battle.targetExam,
                    challengerMinutes = battle.challengerMinutes,
                    opponentMinutes = battle.opponentMinutes,
                    startDateMillis = battle.startDateMillis,
                    endDateMillis = battle.endDateMillis,
                    status = battle.status.name
                )
            ))
        }
        return result
    }

    /**
     * Calculates the lead margin in minutes for the challenger.
     * Positive = challenger leading, Negative = opponent leading.
     */
    fun calculateLeadMargin(battle: StudyBattle): Int =
        battle.challengerMinutes - battle.opponentMinutes

    /**
     * Returns the completion percentage (0–100) for a side of the battle,
     * normalised against a 600-minute (10h) weekly goal.
     */
    fun progressPercent(minutes: Int): Float =
        (minutes.toFloat() / 600f).coerceIn(0f, 1f)
}
