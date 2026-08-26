package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.entity.SessionEntity
import com.quovex.domain.model.UserProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class StudyAnalyticsUseCaseTest {

    private lateinit var fakeSessionDao: FakeSessionDao
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var useCase: StudyAnalyticsUseCase
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        fakeSessionDao = FakeSessionDao()
        fakeUserPrefs = UserPreferencesManager(null)
        fakeUserPrefs.saveUserProfile(UserProfile(targetExam = "JEE Advanced"))
        useCase = StudyAnalyticsUseCase(fakeSessionDao, fakeUserPrefs)
    }

    @Test
    fun `heatmap grid generates exactly 28 days for 4 weeks`() = runBlocking {
        val today = LocalDate.now(zoneId)
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        fakeSessionDao.insertSession(
            SessionEntity(
                id = 1,
                startTime = todayMillis + 1000L,
                endTime = todayMillis + 3600000L,
                durationMinutes = 60,
                focusScore = 90,
                appBlockViolations = 0,
                subject = "Physics"
            )
        )

        val heatmap = useCase.getHeatmapGrid(zoneId = zoneId, weeksCount = 4)

        assertEquals(28, heatmap.size)
        val todayCell = heatmap.find { it.isToday }
        assertTrue(todayCell != null)
        assertEquals(60, todayCell?.minutesStudied)
        assertEquals(3, todayCell?.intensityLevel)
    }

    @Test
    fun `subject breakdown correctly aggregates study time`() = runBlocking {
        val today = LocalDate.now(zoneId)
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        fakeSessionDao.insertSession(
            SessionEntity(id = 1, startTime = todayMillis, endTime = todayMillis + 3600000L, durationMinutes = 60, focusScore = 0, appBlockViolations = 0, subject = "Physics")
        )
        fakeSessionDao.insertSession(
            SessionEntity(id = 2, startTime = todayMillis + 1000L, endTime = todayMillis + 3600000L, durationMinutes = 40, focusScore = 0, appBlockViolations = 0, subject = "Chemistry")
        )

        val breakdown = useCase.getSubjectBreakdown(zoneId = zoneId, days = 30)

        assertEquals(2, breakdown.size)
        assertEquals("Physics", breakdown[0].subject)
        assertEquals(60, breakdown[0].totalMinutes)
        assertEquals(0.6f, breakdown[0].percentage, 0.01f)

        assertEquals("Chemistry", breakdown[1].subject)
        assertEquals(40, breakdown[1].totalMinutes)
        assertEquals(0.4f, breakdown[1].percentage, 0.01f)
    }

    @Test
    fun `exam countdown returns valid days remaining`() {
        val countdown = useCase.getExamCountdown(zoneId = zoneId)

        assertEquals("JEE Advanced", countdown.targetExam)
        assertTrue(countdown.daysRemaining > 0)
        assertTrue(countdown.motivationalQuote.isNotEmpty())
    }

    @Test
    fun `hourly productivity aggregates 24 hours correctly`() = runBlocking {
        val today = LocalDate.now(zoneId)
        val today10am = today.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        fakeSessionDao.insertSession(
            SessionEntity(
                id = 1,
                startTime = today10am,
                endTime = today10am + 3600000L,
                durationMinutes = 60,
                focusScore = 85,
                appBlockViolations = 0,
                subject = "Mathematics"
            )
        )

        val hourly = useCase.getHourlyProductivity(zoneId = zoneId, days = 30)

        assertEquals(24, hourly.size)
        val hour10 = hourly[10]
        assertEquals(10, hour10.hourOfDay)
        assertEquals(60, hour10.totalMinutes)
        assertEquals(1, hour10.sessionCount)
        assertEquals(85, hour10.averageFocusScore)
    }

    @Test
    fun `performance insights calculates weekly total and dynamic insight`() = runBlocking {
        val today = LocalDate.now(zoneId)
        val today10am = today.atTime(10, 0).atZone(zoneId).toInstant().toEpochMilli()

        fakeSessionDao.insertSession(
            SessionEntity(
                id = 1,
                startTime = today10am,
                endTime = today10am + 3600000L,
                durationMinutes = 90,
                focusScore = 95,
                appBlockViolations = 0,
                subject = "Physics"
            )
        )

        val insights = useCase.getPerformanceInsights(zoneId = zoneId)

        assertEquals(90, insights.weeklyTotalMinutes)
        assertEquals(95, insights.focusScoreAvg)
        assertEquals(1, insights.totalSessionsCompleted)
        assertTrue(insights.aiInsight.contains("Outstanding cognitive stamina"))
    }
}
