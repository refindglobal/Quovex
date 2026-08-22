package com.quovex.data.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.quovex.data.local.SessionStateManager
import com.quovex.domain.usecase.EndFocusSessionUseCase
import com.quovex.domain.util.FocusTimerEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimerForegroundService : Service() {

    @Inject
    lateinit var sessionStateManager: SessionStateManager

    @Inject
    lateinit var endFocusSessionUseCase: EndFocusSessionUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickerJob: Job? = null

    companion object {
        const val ACTION_START = "com.quovex.action.START_TIMER"
        const val ACTION_STOP = "com.quovex.action.STOP_TIMER"
        const val ACTION_CANCEL = "com.quovex.action.CANCEL_TIMER"

        fun startService(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun cancelService(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        FocusNotificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_STOP -> handleStop(isCompleted = false)
            ACTION_CANCEL -> handleStop(isCompleted = false)
            else -> {
                // If service was restarted by system, check if session is still active
                val session = sessionStateManager.activeSession.value
                if (session.isActive) {
                    handleStart()
                } else {
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun handleStart() {
        val session = sessionStateManager.activeSession.value
        if (!session.isActive) {
            stopSelf()
            return
        }

        val notification = FocusNotificationHelper.buildTimerNotification(
            context = this,
            subject = session.subject,
            remainingSeconds = session.remainingSeconds,
            strictFocusEnabled = session.strictFocusEnabled
        )

        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        ServiceCompat.startForeground(
            this,
            FocusNotificationHelper.NOTIFICATION_ID,
            notification,
            serviceType
        )

        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            while (isActive) {
                val now = System.currentTimeMillis()
                val session = sessionStateManager.activeSession.value

                if (!session.isActive) {
                    break
                }

                val remaining = sessionStateManager.updateTick(now)

                // Update ongoing notification
                val updatedNotification = FocusNotificationHelper.buildTimerNotification(
                    context = this@TimerForegroundService,
                    subject = session.subject,
                    remainingSeconds = remaining,
                    strictFocusEnabled = session.strictFocusEnabled
                )
                notificationManager.notify(FocusNotificationHelper.NOTIFICATION_ID, updatedNotification)

                if (remaining <= 0 || FocusTimerEngine.isTimerFinished(session.endTimeMillis, now)) {
                    if (sessionStateManager.tryClaimCompletion()) {
                        endFocusSessionUseCase(isCompleted = true, endTimeMillis = now)
                    }
                    break
                }

                delay(1000L)
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun handleStop(isCompleted: Boolean) {
        tickerJob?.cancel()
        val session = sessionStateManager.activeSession.value
        if (session.isActive) {
            serviceScope.launch {
                endFocusSessionUseCase(isCompleted = isCompleted)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
