package com.quovex.data.analytics

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.quovex.domain.manager.PdfReportGenerator
import com.quovex.domain.model.StudyReportData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGeneratorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PdfReportGenerator {

    override suspend fun generateWeeklyReportPdf(reportData: StudyReportData): Result<File> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width (pt)
            val pageHeight = 842 // A4 standard height (pt)

            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Paints
            val bgPaint = Paint().apply {
                color = Color.parseColor("#0A0F0D") // Quovex Dark Charcoal
                style = Paint.Style.FILL
            }
            val cardPaint = Paint().apply {
                color = Color.parseColor("#141D1A")
                style = Paint.Style.FILL
            }
            val accentPaint = Paint().apply {
                color = Color.parseColor("#00C896") // Brand Emerald
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val strokeAccentPaint = Paint().apply {
                color = Color.parseColor("#00C896")
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            val textTitlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val textSubtitlePaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 10f
                isAntiAlias = true
            }
            val textHeadingPaint = Paint().apply {
                color = Color.parseColor("#00C896")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val textBodyPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                isAntiAlias = true
            }
            val textValuePaint = Paint().apply {
                color = Color.WHITE
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val textLabelPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
                isAntiAlias = true
            }
            val barTrackPaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }

            // 1. Draw Full Background
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

            // 2. Header Banner
            val headerHeight = 90f
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), headerHeight, cardPaint)
            canvas.drawRect(0f, headerHeight - 3f, pageWidth.toFloat(), headerHeight, accentPaint)

            canvas.drawText("QUOVEX — EXECUTIVE STUDY REPORT", 32f, 38f, textTitlePaint)
            canvas.drawText("Weekly Cognitive Performance & Study Telemetry", 32f, 54f, textSubtitlePaint)
            canvas.drawText("Student: ${reportData.studentName}  •  Target: ${reportData.targetExam}  •  Date: ${reportData.generatedDateFormatted}", 32f, 74f, textSubtitlePaint)

            // 3. KPI Grid (4 Cards)
            val gridTop = 110f
            val cardWidth = (pageWidth - 64f - 24f) / 2f
            val cardHeight = 65f

            val kpis = listOf(
                Pair("WEEKLY FOCUS TIME", formatHoursAndMinutes(reportData.weeklyTotalMinutes)),
                Pair("DAILY AVERAGE", "${reportData.dailyAverageMinutes} mins / day"),
                Pair("AVG FOCUS SCORE", "${reportData.focusScoreAvg}%"),
                Pair("PEAK COGNITIVE WINDOW", reportData.bestFocusWindow)
            )

            for (i in kpis.indices) {
                val row = i / 2
                val col = i % 2
                val x = 32f + col * (cardWidth + 24f)
                val y = gridTop + row * (cardHeight + 16f)

                val rect = RectF(x, y, x + cardWidth, y + cardHeight)
                canvas.drawRoundRect(rect, 8f, 8f, cardPaint)
                canvas.drawText(kpis[i].first, x + 16f, y + 24f, textLabelPaint)
                canvas.drawText(kpis[i].second, x + 16f, y + 48f, textValuePaint)
            }

            // 4. Subject Distribution Section
            var currentY = gridTop + 2 * (cardHeight + 16f) + 20f
            canvas.drawText("SUBJECT TIME & MASTERY DISTRIBUTION", 32f, currentY, textHeadingPaint)
            currentY += 16f

            if (reportData.subjects.isEmpty()) {
                val emptyRect = RectF(32f, currentY, pageWidth - 32f, currentY + 50f)
                canvas.drawRoundRect(emptyRect, 8f, 8f, cardPaint)
                canvas.drawText("No subject focus sessions recorded for this period.", 48f, currentY + 30f, textSubtitlePaint)
                currentY += 65f
            } else {
                for (subj in reportData.subjects.take(5)) {
                    val subRect = RectF(32f, currentY, pageWidth - 32f, currentY + 44f)
                    canvas.drawRoundRect(subRect, 8f, 8f, cardPaint)

                    val subjName = subj.subject
                    val timeStr = "${formatHoursAndMinutes(subj.totalMinutes)} (${(subj.percentage * 100).toInt()}%)"
                    canvas.drawText(subjName, 48f, currentY + 20f, textBodyPaint)
                    canvas.drawText(timeStr, pageWidth - 48f - textSubtitlePaint.measureText(timeStr), currentY + 20f, textSubtitlePaint)

                    // Progress Bar
                    val barWidth = pageWidth - 96f
                    val progressWidth = barWidth * subj.percentage
                    val barY = currentY + 28f
                    val trackRect = RectF(48f, barY, 48f + barWidth, barY + 6f)
                    val progressRect = RectF(48f, barY, 48f + progressWidth, barY + 6f)

                    canvas.drawRoundRect(trackRect, 3f, 3f, barTrackPaint)
                    canvas.drawRoundRect(progressRect, 3f, 3f, accentPaint)

                    currentY += 52f
                }
            }

            // 5. AI Study Coach Telemetry & Recommendation
            currentY += 10f
            canvas.drawText("AI COGNITIVE COACH INSIGHTS & RECOMMENDATIONS", 32f, currentY, textHeadingPaint)
            currentY += 16f

            val aiBoxHeight = 110f
            val aiBoxRect = RectF(32f, currentY, pageWidth - 32f, currentY + aiBoxHeight)
            canvas.drawRoundRect(aiBoxRect, 8f, 8f, cardPaint)
            canvas.drawRoundRect(aiBoxRect, 8f, 8f, strokeAccentPaint)

            val lines = wrapText(reportData.aiRecommendation, 70)
            var textY = currentY + 28f
            for (line in lines.take(4)) {
                canvas.drawText(line, 48f, textY, textBodyPaint)
                textY += 18f
            }

            // 6. Footer
            val footerY = pageHeight - 30f
            canvas.drawLine(32f, footerY - 15f, pageWidth - 32f, footerY - 15f, cardPaint)
            canvas.drawText("Generated by Quovex AI Study Engine • End-to-End Encrypted • quovex.app", 32f, footerY, textSubtitlePaint)
            val streakNotice = "🔥 ${reportData.currentStreak} Day Study Streak"
            canvas.drawText(streakNotice, pageWidth - 32f - textHeadingPaint.measureText(streakNotice), footerY, textHeadingPaint)

            pdfDocument.finishPage(page)

            // Save PDF to cache directory
            val reportsDir = File(context.cacheDir, "quovex_reports").apply { if (!exists()) mkdirs() }
            val outputFile = File(reportsDir, "quovex_weekly_report_${System.currentTimeMillis()}.pdf")
            FileOutputStream(outputFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatHoursAndMinutes(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > maxCharsPerLine) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
