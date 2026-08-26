package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quovex.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity): Long

    @Query("SELECT * FROM streaks_cemetery ORDER BY endDate DESC")
    fun getAllBrokenStreaks(): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks_cemetery WHERE id = :id LIMIT 1")
    suspend fun getStreakById(id: Long): StreakEntity?

    @Query("UPDATE streaks_cemetery SET reflectionNote = :note WHERE id = :id")
    suspend fun updateReflection(id: Long, note: String): Int

    @Query("SELECT MAX(streakDays) FROM streaks_cemetery")
    suspend fun getLongestCemeteryStreak(): Int?

    @Query("SELECT COUNT(*) FROM streaks_cemetery")
    suspend fun getTotalCemeteryCount(): Int
}
