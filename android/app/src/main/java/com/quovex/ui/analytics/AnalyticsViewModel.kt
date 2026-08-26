package com.quovex.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.HourlyProductivity
import com.quovex.domain.model.PerformanceInsights
import com.quovex.domain.model.SubjectStudyTime
import com.quovex.domain.repository.BillingRepository
import com.quovex.domain.usecase.GenerateWeeklyPdfReportUseCase
import com.quovex.domain.usecase.StudyAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface PdfExportState {
    data object Idle : PdfExportState
    data object Generating : PdfExportState
    data class Success(val file: File) : PdfExportState
    data class Error(val message: String) : PdfExportState
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val studyAnalyticsUseCase: StudyAnalyticsUseCase,
    private val generateWeeklyPdfReportUseCase: GenerateWeeklyPdfReportUseCase,
    private val billingRepository: BillingRepository
) : ViewModel() {

    private val _hourlyProductivity = MutableStateFlow<List<HourlyProductivity>>(emptyList())
    val hourlyProductivity: StateFlow<List<HourlyProductivity>> = _hourlyProductivity.asStateFlow()

    private val _subjectBreakdown = MutableStateFlow<List<SubjectStudyTime>>(emptyList())
    val subjectBreakdown: StateFlow<List<SubjectStudyTime>> = _subjectBreakdown.asStateFlow()

    private val _performanceInsights = MutableStateFlow<PerformanceInsights?>(null)
    val performanceInsights: StateFlow<PerformanceInsights?> = _performanceInsights.asStateFlow()

    private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
    val pdfExportState: StateFlow<PdfExportState> = _pdfExportState.asStateFlow()

    val userEntitlement = billingRepository.userEntitlement

    init {
        loadAnalyticsData()
    }

    fun loadAnalyticsData() {
        viewModelScope.launch {
            _hourlyProductivity.value = studyAnalyticsUseCase.getHourlyProductivity(days = 30)
            _subjectBreakdown.value = studyAnalyticsUseCase.getSubjectBreakdown(days = 30)
            _performanceInsights.value = studyAnalyticsUseCase.getPerformanceInsights()
        }
    }

    fun exportWeeklyPdfReport() {
        viewModelScope.launch {
            _pdfExportState.value = PdfExportState.Generating
            val result = generateWeeklyPdfReportUseCase()
            result.onSuccess { file ->
                _pdfExportState.value = PdfExportState.Success(file)
            }.onFailure { error ->
                _pdfExportState.value = PdfExportState.Error(
                    error.message ?: "Failed to generate study report PDF."
                )
            }
        }
    }

    fun clearPdfExportState() {
        _pdfExportState.value = PdfExportState.Idle
    }
}
