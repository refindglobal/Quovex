package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quovex.data.local.entity.FriendEntity
import com.quovex.data.local.entity.LeaderboardCacheEntity
import com.quovex.data.local.entity.StudyBattleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for locally cached Community data:
 * - Friends list
 * - Study battles (1v1 challenges)
 * - Weekly leaderboard snapshots
 */
@Dao
interface CommunityDao {

    // ---- Friends ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFriends(friends: List<FriendEntity>)

    @Query("SELECT * FROM friends ORDER BY isStudyingNow DESC, displayName ASC")
    fun getFriendsFlow(): Flow<List<FriendEntity>>

    @Query("DELETE FROM friends")
    suspend fun clearFriends()

    // ---- Study Battles ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBattles(battles: List<StudyBattleEntity>)

    @Query("SELECT * FROM study_battles WHERE challengerId = :userId OR opponentId = :userId ORDER BY startDateMillis DESC")
    fun getBattlesForUserFlow(userId: String): Flow<List<StudyBattleEntity>>

    @Query("SELECT * FROM study_battles WHERE battleId = :battleId LIMIT 1")
    suspend fun getBattle(battleId: String): StudyBattleEntity?

    @Query("DELETE FROM study_battles")
    suspend fun clearBattles()

    // ---- Leaderboard Cache ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLeaderboard(entries: List<LeaderboardCacheEntity>)

    @Query("""
        SELECT * FROM leaderboard_cache
        WHERE leaderboardType = :type AND subjectFilter = :subject AND weekKey = :weekKey
        ORDER BY rank ASC
    """)
    fun getLeaderboardFlow(
        type: String,
        subject: String,
        weekKey: String
    ): Flow<List<LeaderboardCacheEntity>>

    @Query("DELETE FROM leaderboard_cache WHERE weekKey != :currentWeekKey")
    suspend fun evictStaleLeaderboard(currentWeekKey: String)
}
