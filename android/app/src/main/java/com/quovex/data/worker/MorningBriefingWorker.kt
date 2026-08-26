package com.quovex.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.quovex.MainActivity
import com.quovex.R
import com.quovex.data.local.UserPreferencesManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker executing the Morning Briefing daily notification loop (PRD Module F2).
 * Delivers personalized morning focus targets and motivational quotes to students.
 */
class MorningBriefingWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = context.getSharedPreferences("quovex_user_prefs", Context.MODE_PRIVATE)
            val userPrefsManager = UserPreferencesManager(prefs)
            val profile = userPrefsManager.userProfile.value

            val quotes = listOf(
                "The secret of getting ahead is getting started.",
                "Small daily improvements over time lead to stunning results.",
                "Deep work is not about working more; it is about focusing completely.",
                "Success is the sum of small efforts repeated day in and day out.",
                "Your focus determines your reality. Make today count.",
                "Consistency beats intensity. Build your momentum today.",
                "One focused session today brings you one step closer to your exam goal."
            )

            val quote = quotes.random()
            val streak = profile.streakDays
            val hours = profile.dailyGoalHours

            showNotification(streak, hours, quote)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(streak: Int, goalHours: Float, quote: String) {
        val channelId = "quovex_morning_briefing"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Morning Briefing",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning study motivation and goal reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("☀️ Morning Briefing • $streak Day Streak")
            .setContentText("Target: ${String.format("%.1f", goalHours)}h. $quote")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Today's Target: ${String.format("%.1f", goalHours)} hours • $quote")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    companion object {
        const val WORK_NAME = "QuovexMorningBriefing"

        /**
         * Schedules the daily morning briefing at 07:00 AM local time.
         */
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            var target = now.withHour(7).withMinute(0).withSecond(0)
            if (now.isAfter(target)) {
                target = target.plusDays(1)
            }

            val initialDelay = Duration.between(now, target).toMinutes()

            val workRequest = PeriodicWorkRequestBuilder<MorningBriefingWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
