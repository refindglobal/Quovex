package com.quovex.ui.analytics

import com.quovex.domain.model.PerformanceInsights
import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.repository.BillingRepository
import com.quovex.domain.usecase.GenerateWeeklyPdfReportUseCase
import com.quovex.domain.usecase.StudyAnalyticsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val studyAnalyticsUseCase = mockk<StudyAnalyticsUseCase>()
    private val generateWeeklyPdfReportUseCase = mockk<GenerateWeeklyPdfReportUseCase>()
    private val billingRepository = mockk<BillingRepository>()

    private val fakeEntitlementFlow = MutableStateFlow(UserEntitlement())
    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        coEvery { billingRepository.userEntitlement } returns fakeEntitlementFlow
        coEvery { studyAnalyticsUseCase.getHourlyProductivity(days = 30) } returns emptyList()
        coEvery { studyAnalyticsUseCase.getSubjectBreakdown(days = 30) } returns emptyList()
        coEvery { studyAnalyticsUseCase.getPerformanceInsights() } returns PerformanceInsights(
            bestDayOfWeek = "Monday",
            bestHourWindow = "10:00 – 12:00",
            weeklyTotalMinutes = 300,
            monthlyTotalMinutes = 1200,
            focusScoreAvg = 88,
            distractionResistanceRate = 0.9f,
            totalSessionsCompleted = 8,
            aiInsight = "Great focus streak."
        )

        viewModel = AnalyticsViewModel(
            studyAnalyticsUseCase = studyAnalyticsUseCase,
            generateWeeklyPdfReportUseCase = generateWeeklyPdfReportUseCase,
            billingRepository = billingRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAnalyticsData_loadsTelemetrySuccessfully() = runTest {
        advanceUntilIdle()

        val insights = viewModel.performanceInsights.value
        assertEquals("Monday", insights?.bestDayOfWeek)
        assertEquals(300, insights?.weeklyTotalMinutes)
        assertEquals(88, insights?.focusScoreAvg)
    }

    @Test
    fun exportWeeklyPdfReport_success_transitionsToSuccessState() = runTest {
        val mockFile = File("fake/quovex_report.pdf")
        coEvery { generateWeeklyPdfReportUseCase() } returns Result.success(mockFile)

        viewModel.exportWeeklyPdfReport()
        advanceUntilIdle()

        val state = viewModel.pdfExportState.value
        assertTrue(state is PdfExportState.Success)
        assertEquals(mockFile, (state as PdfExportState.Success).file)
    }

    @Test
    fun exportWeeklyPdfReport_failure_transitionsToErrorState() = runTest {
        coEvery { generateWeeklyPdfReportUseCase() } returns Result.failure(Exception("IO Error"))

        viewModel.exportWeeklyPdfReport()
        advanceUntilIdle()

        val state = viewModel.pdfExportState.value
        assertTrue(state is PdfExportState.Error)
        assertEquals("IO Error", (state as PdfExportState.Error).message)
    }
}
