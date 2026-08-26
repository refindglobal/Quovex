package com.quovex.domain.usecase

import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSessionDao : SessionDao {
    val sessionsList = mutableListOf<SessionEntity>()
    private var nextId = 1

    override suspend fun insertSession(session: SessionEntity): Long {
        val id = nextId++
        sessionsList.add(session.copy(id = id))
        return id.toLong()
    }

    override fun getAllSessions(): Flow<List<SessionEntity>> {
        return flowOf(sessionsList.sortedByDescending { it.startTime })
    }

    override suspend fun getRecentSessionsList(limit: Int): List<SessionEntity> {
        return sessionsList.sortedByDescending { it.startTime }.take(limit)
    }

    override suspend fun getSessionsBetween(startTime: Long, endTime: Long): List<SessionEntity> {
        return sessionsList.filter { it.startTime in startTime..endTime }
    }

    override suspend fun getTotalStudyMinutesSince(startTime: Long): Int? {
        val sum = sessionsList.filter { it.startTime >= startTime }.sumOf { it.durationMinutes }
        return if (sum > 0) sum else null
    }

    override suspend fun getTotalSessionsCount(): Int {
        return sessionsList.size
    }

    override fun getSessionsBySubject(subject: String): Flow<List<SessionEntity>> {
        return flowOf(sessionsList.filter { it.subject.equals(subject, ignoreCase = true) })
    }
}
