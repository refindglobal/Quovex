package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.SessionDao
import com.quovex.domain.model.DailyDiagnosticTopic
import com.quovex.domain.model.DiagnosticQuestion
import com.quovex.domain.model.DiagnosticQuizRequest
import com.quovex.domain.repository.DiagnosticQuizRepository
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GenerateDailyDiagnosticQuizUseCase @Inject constructor(
    private val sessionDao: SessionDao,
    private val userPreferencesManager: UserPreferencesManager,
    private val diagnosticQuizRepository: DiagnosticQuizRepository
) {

    suspend operator fun invoke(zoneId: ZoneId = ZoneId.systemDefault()): Result<List<DiagnosticQuestion>> {
        val profile = userPreferencesManager.userProfile.value
        val targetExam = profile.targetExam.ifBlank { "General Competitive" }

        val startOfTodayMillis = LocalDate.now(zoneId)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val nowMillis = System.currentTimeMillis()

        val todaySessions = sessionDao.getSessionsBetween(startOfTodayMillis, nowMillis)

        val topics = if (todaySessions.isNotEmpty()) {
            todaySessions
                .filter { it.subject.isNotBlank() }
                .groupBy { it.subject }
                .map { (subject, sessions) ->
                    val totalMinutes = sessions.sumOf { it.durationMinutes }
                    DailyDiagnosticTopic(
                        topicName = subject,
                        subject = subject,
                        minutesStudied = totalMinutes
                    )
                }
        } else {
            getDefaultTopicsForExam(targetExam)
        }

        val request = DiagnosticQuizRequest(
            targetExam = targetExam,
            topics = if (topics.isNotEmpty()) topics else getDefaultTopicsForExam(targetExam),
            questionCount = 5
        )

        return diagnosticQuizRepository.generateDailyDiagnosticQuiz(request)
    }

    private fun getDefaultTopicsForExam(exam: String): List<DailyDiagnosticTopic> {
        val lower = exam.lowercase()
        return when {
            lower.contains("neet") || lower.contains("mcat") -> listOf(
                DailyDiagnosticTopic("Human Physiology & Cell Biology", "Biology", 30),
                DailyDiagnosticTopic("Organic Chemistry & Thermodynamics", "Chemistry", 30),
                DailyDiagnosticTopic("Optics & Electromagnetism", "Physics", 30)
            )
            lower.contains("jee") -> listOf(
                DailyDiagnosticTopic("Mechanics & Modern Physics", "Physics", 30),
                DailyDiagnosticTopic("Chemical Bonding & Kinetics", "Chemistry", 30),
                DailyDiagnosticTopic("Calculus & Coordinate Geometry", "Mathematics", 30)
            )
            lower.contains("upsc") || lower.contains("civil") -> listOf(
                DailyDiagnosticTopic("Modern Indian History & Polity", "History", 30),
                DailyDiagnosticTopic("Macroeconomics & Fiscal Policy", "Economics", 30),
                DailyDiagnosticTopic("Physical Geography & Environment", "Geography", 30)
            )
            else -> listOf(
                DailyDiagnosticTopic("Core Fundamentals & Application", "General Science", 30),
                DailyDiagnosticTopic("Logical Reasoning & Critical Thinking", "Aptitude", 30)
            )
        }
    }
}
