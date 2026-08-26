package com.quovex.domain.manager

import com.quovex.domain.model.StudyReportData
import java.io.File

/**
 * Domain interface for generating structured PDF study analytics documents.
 */
interface PdfReportGenerator {

    /**
     * Renders a vector-sharp, printable A4 PDF report from real student study telemetry.
     */
    suspend fun generateWeeklyReportPdf(reportData: StudyReportData): Result<File>
}
