package com.quovex.domain.usecase

import com.quovex.data.local.dao.StreakDao
import com.quovex.data.local.entity.StreakEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeStreakDao : StreakDao {

    private val streaksList = mutableListOf<StreakEntity>()
    private val streaksFlow = MutableStateFlow<List<StreakEntity>>(emptyList())

    override fun getAllBrokenStreaks(): Flow<List<StreakEntity>> {
        return streaksFlow.asStateFlow().map { list -> list.filter { it.isBroken } }
    }

    override suspend fun getLongestCemeteryStreak(): Int? {
        return streaksList.maxOfOrNull { it.streakDays }
    }

    override suspend fun getTotalCemeteryCount(): Int {
        return streaksList.count { it.isBroken }
    }

    override suspend fun insertStreak(streak: StreakEntity): Long {
        val newId = (streaksList.size + 1).toLong()
        val item = streak.copy(id = newId)
        streaksList.add(item)
        streaksFlow.value = streaksList.toList()
        return newId
    }

    override suspend fun updateReflection(id: Long, note: String): Int {
        val idx = streaksList.indexOfFirst { it.id == id }
        return if (idx != -1) {
            val updated = streaksList[idx].copy(reflectionNote = note)
            streaksList[idx] = updated
            streaksFlow.value = streaksList.toList()
            1
        } else {
            0
        }
    }

    override suspend fun getStreakById(id: Long): StreakEntity? {
        return streaksList.find { it.id == id }
    }
}
