package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quovex.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun observeUserStats(): Flow<UserStatsEntity?>

    @Query("UPDATE user_stats SET currentStreak = :streak, longestStreak = :longest, lastStudyDateMillis = :lastDate WHERE id = 1")
    suspend fun updateStreak(streak: Int, longest: Int, lastDate: Long)

    @Query("UPDATE user_stats SET rescueTokens = :tokens WHERE id = 1")
    suspend fun updateRescueTokens(tokens: Int)

    @Query("UPDATE user_stats SET totalXp = totalXp + :xpToAdd, scholarLevel = :newLevel WHERE id = 1")
    suspend fun addXp(xpToAdd: Long, newLevel: Int)
}
