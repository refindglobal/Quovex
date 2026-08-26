package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.domain.manager.PdfReportGenerator
import com.quovex.domain.model.PerformanceInsights
import com.quovex.domain.model.StudyReportData
import com.quovex.domain.model.UserProfile
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class GenerateWeeklyPdfReportUseCaseTest {

    private val studyAnalyticsUseCase = mockk<StudyAnalyticsUseCase>()
    private val pdfReportGenerator = mockk<PdfReportGenerator>()
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var useCase: GenerateWeeklyPdfReportUseCase

    @Before
    fun setup() {
        userPreferencesManager = UserPreferencesManager(null)
        userPreferencesManager.saveUserProfile(UserProfile(name = "Arjun", targetExam = "NEET"))
        useCase = GenerateWeeklyPdfReportUseCase(
            studyAnalyticsUseCase = studyAnalyticsUseCase,
            userPreferencesManager = userPreferencesManager,
            pdfReportGenerator = pdfReportGenerator
        )
    }

    @Test
    fun invoke_assemblesStudyReportDataAndCallsGenerator() = runTest {
        val fakeInsights = PerformanceInsights(
            bestDayOfWeek = "Thursday",
            bestHourWindow = "09:00 – 11:00",
            weeklyTotalMinutes = 420,
            monthlyTotalMinutes = 1800,
            focusScoreAvg = 92,
            distractionResistanceRate = 0.95f,
            totalSessionsCompleted = 12,
            aiInsight = "Consistent performance."
        )

        coEvery { studyAnalyticsUseCase.getPerformanceInsights() } returns fakeInsights
        coEvery { studyAnalyticsUseCase.getSubjectBreakdown(days = 7) } returns emptyList()
        coEvery { studyAnalyticsUseCase.getHourlyProductivity(days = 7) } returns emptyList()

        val capturedSlot = slot<StudyReportData>()
        val mockFile = File("fake/path/report.pdf")
        coEvery { pdfReportGenerator.generateWeeklyReportPdf(capture(capturedSlot)) } returns Result.success(mockFile)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(mockFile, result.getOrNull())

        val captured = capturedSlot.captured
        assertEquals("Arjun", captured.studentName)
        assertEquals("NEET", captured.targetExam)
        assertEquals(420, captured.weeklyTotalMinutes)
        assertEquals(60, captured.dailyAverageMinutes) // 420 / 7
        assertEquals(92, captured.focusScoreAvg)
        assertEquals("09:00 – 11:00", captured.bestFocusWindow)
        coVerify(exactly = 1) { pdfReportGenerator.generateWeeklyReportPdf(any()) }
    }
}
