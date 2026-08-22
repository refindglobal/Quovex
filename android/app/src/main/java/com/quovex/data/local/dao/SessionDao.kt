package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quovex.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessionsList(limit: Int = 10): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime ASC")
    suspend fun getSessionsBetween(startTime: Long, endTime: Long): List<SessionEntity>

    @Query("SELECT SUM(durationMinutes) FROM sessions WHERE startTime >= :startTime")
    suspend fun getTotalStudyMinutesSince(startTime: Long): Int?

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalSessionsCount(): Int

    @Query("SELECT * FROM sessions WHERE subject = :subject ORDER BY startTime DESC")
    fun getSessionsBySubject(subject: String): Flow<List<SessionEntity>>
}
