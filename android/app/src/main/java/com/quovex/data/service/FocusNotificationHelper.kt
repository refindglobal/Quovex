package com.quovex.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.quovex.MainActivity
import com.quovex.R
import com.quovex.domain.util.TimerFormatter

object FocusNotificationHelper {

    const val CHANNEL_ID = "quovex_focus_timer_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Focus Session Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows remaining time for your active Quovex focus session."
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildTimerNotification(
        context: Context,
        subject: String,
        remainingSeconds: Int,
        strictFocusEnabled: Boolean
    ): Notification {
        val formattedTime = TimerFormatter.formatRemainingTime(remainingSeconds)
        val strictTag = if (strictFocusEnabled) " • Strict Focus" else ""

        val returnIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            returnIntent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Focus Session: $subject")
            .setContentText("$formattedTime remaining$strictTag")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }
}
