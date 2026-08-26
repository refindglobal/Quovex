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
import com.quovex.domain.model.SoundscapePresets
import com.quovex.domain.repository.FocusDetectionRepository
import com.quovex.domain.repository.SoundscapeRepository
import com.quovex.domain.usecase.EndFocusSessionUseCase
import com.quovex.data.service.FocusNotificationHelper
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

    @Inject
    lateinit var soundscapeRepository: SoundscapeRepository

    @Inject
    lateinit var focusDetectionRepository: FocusDetectionRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

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

        // Trigger ambient soundscape auto-play if enabled
        val soundState = soundscapeRepository.soundscapeState.value
        if (soundState.isAutoPlayWithTimerEnabled && soundState.selectedPreset != SoundscapePresets.NONE) {
            soundscapeRepository.play()
        }

        // Start camera tracking if enabled
        if (focusDetectionRepository.focusTrackingState.value.isEnabled) {
            focusDetectionRepository.resetSession()
            focusDetectionRepository.setCameraActive(true)
        }

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
                        val focusState = focusDetectionRepository.focusTrackingState.value
                        endFocusSessionUseCase(
                            isCompleted = true,
                            endTimeMillis = now,
                            focusScore = if (focusState.isEnabled) focusState.focusScore else null,
                            distractionsCount = focusState.distractionsCount,
                            drowsinessCount = focusState.drowsinessCount,
                            cameraTrackingEnabled = focusState.isEnabled
                        )
                    }
                    break
                }

                delay(1000L)
            }

            // Pause soundscape and camera tracking when session finishes
            soundscapeRepository.pause()
            focusDetectionRepository.setCameraActive(false)

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun handleStop(isCompleted: Boolean) {
        tickerJob?.cancel()
        soundscapeRepository.pause()
        focusDetectionRepository.setCameraActive(false)

        val session = sessionStateManager.activeSession.value
        if (session.isActive) {
            serviceScope.launch {
                val focusState = focusDetectionRepository.focusTrackingState.value
                endFocusSessionUseCase(
                    isCompleted = isCompleted,
                    endTimeMillis = System.currentTimeMillis(),
                    focusScore = if (focusState.isEnabled) focusState.focusScore else null,
                    distractionsCount = focusState.distractionsCount,
                    drowsinessCount = focusState.drowsinessCount,
                    cameraTrackingEnabled = focusState.isEnabled
                )
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerJob?.cancel()
        soundscapeRepository.pause()
        focusDetectionRepository.setCameraActive(false)
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "com.quovex.action.START_TIMER"
        const val ACTION_STOP = "com.quovex.action.STOP_TIMER"
        const val ACTION_CANCEL = "com.quovex.action.CANCEL_TIMER"

        fun start(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
