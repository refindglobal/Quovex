package com.quovex.domain.usecase

import com.quovex.data.local.dao.CommunityDao
import com.quovex.data.local.entity.LeaderboardCacheEntity
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.LeaderboardEntry
import com.quovex.domain.model.LeaderboardType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import javax.inject.Inject

/**
 * Fetches and ranks the weekly leaderboard.
 *
 * Strategy:
 * 1. Attempt to load from Firestore (online).
 * 2. On success, persist to local [LeaderboardCacheEntity] for offline access.
 * 3. On failure, fall back to cached Room entries.
 */
class GetLeaderboardUseCase @Inject constructor(
    private val firestoreService: FirebaseFirestoreService,
    private val communityDao: CommunityDao
) {

    /**
     * Returns ranked leaderboard entries for the given [type] and optional [subjectFilter].
     * The current user is identified by [currentUserId] so their row can be highlighted.
     */
    suspend fun execute(
        type: LeaderboardType,
        subjectFilter: String = "ALL",
        currentUserId: String = ""
    ): List<LeaderboardEntry> {
        val weekKey = currentWeekKey()

        // 1. Evict stale cache rows from previous weeks
        communityDao.evictStaleLeaderboard(weekKey)

        // 2. Fetch from Firestore
        val remoteEntries = firestoreService.getWeeklyLeaderboard(
            type = type,
            subjectFilter = subjectFilter,
            currentUserId = currentUserId
        )

        return if (remoteEntries.isNotEmpty()) {
            // 3a. Persist fetched entries to local cache
            val cacheEntities = remoteEntries.map { entry ->
                LeaderboardCacheEntity(
                    leaderboardType = type.name,
                    subjectFilter = subjectFilter,
                    userId = entry.userId,
                    userName = entry.userName,
                    avatarId = entry.avatarId,
                    scholarRank = entry.scholarRank,
                    studyMinutes = entry.studyMinutes,
                    xp = entry.xp,
                    rank = entry.rank,
                    isCurrentUser = entry.isCurrentUser,
                    trend = entry.trend.name,
                    weekKey = weekKey
                )
            }
            communityDao.upsertLeaderboard(cacheEntities)
            remoteEntries
        } else {
            // 3b. Return cached rows if Firestore is unreachable (offline)
            val cachedEntities = try {
                communityDao.getLeaderboardFlow(type.name, subjectFilter, weekKey).first()
            } catch (_: Exception) {
                emptyList()
            }
            cachedEntities.map { e ->
                LeaderboardEntry(
                    userId = e.userId,
                    userName = e.userName,
                    avatarId = e.avatarId,
                    scholarRank = e.scholarRank,
                    studyMinutes = e.studyMinutes,
                    xp = e.xp,
                    rank = e.rank,
                    isCurrentUser = e.isCurrentUser,
                    trend = try {
                        com.quovex.domain.model.RankTrend.valueOf(e.trend)
                    } catch (_: Exception) { com.quovex.domain.model.RankTrend.SAME }
                )
            }
        }
    }

    /** Returns the ISO week key for the current date, e.g. "2026-W35". */
    private fun currentWeekKey(): String {
        val now = LocalDate.now()
        val week = now.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val year = now.get(IsoFields.WEEK_BASED_YEAR)
        return "$year-W${week.toString().padStart(2, '0')}"
    }
}
