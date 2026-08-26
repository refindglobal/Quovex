package com.quovex.data.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.SessionStatus
import com.quovex.domain.repository.DistractionBlockerRepository
import com.quovex.ui.blocker.BlockerOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuovexAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var sessionStateManager: SessionStateManager

    @Inject
    lateinit var distractionBlockerRepository: DistractionBlockerRepository

    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastBlockedPackage: String? = null
    private var lastBlockTimestamp: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        distractionBlockerRepository.updateAccessibilityStatus(true)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        // Never intercept self
        if (packageName == applicationContext.packageName) {
            return
        }

        val activeSession = sessionStateManager.activeSession.value
        if (!activeSession.isActive || activeSession.status != SessionStatus.RUNNING) {
            return
        }

        val shieldState = distractionBlockerRepository.shieldState.value
        if (!shieldState.isShieldEnabled) {
            return
        }

        if (shieldState.blockedPackages.contains(packageName)) {
            // Debounce within 1.5 seconds for same package to prevent loop flicker
            val now = System.currentTimeMillis()
            if (packageName == lastBlockedPackage && (now - lastBlockTimestamp) < 1500) {
                return
            }
            lastBlockedPackage = packageName
            lastBlockTimestamp = now

            scope.launch {
                val attempts = distractionBlockerRepository.recordDistractionAttempt(packageName)

                val intent = Intent(this@QuovexAccessibilityService, BlockerOverlayActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    putExtra(BlockerOverlayActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                    putExtra(BlockerOverlayActivity.EXTRA_SUBJECT, activeSession.subject)
                    putExtra(BlockerOverlayActivity.EXTRA_REMAINING_SECONDS, activeSession.remainingSeconds)
                    putExtra(BlockerOverlayActivity.EXTRA_ATTEMPTS, attempts)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        distractionBlockerRepository.updateAccessibilityStatus(false)
    }
}
