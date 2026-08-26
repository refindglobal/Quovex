package com.quovex.domain.repository

import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.DistractionShieldState
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain repository contract for managing installed apps discovery, blocked package list, and distraction resistance metrics.
 */
interface DistractionBlockerRepository {

    /**
     * Real-time stream of the distraction shield configuration and resisted attempts.
     */
    val shieldState: StateFlow<DistractionShieldState>

    /**
     * Toggles global distraction shield on or off.
     */
    suspend fun setShieldEnabled(enabled: Boolean)

    /**
     * Toggles blocked status for a specific app package.
     */
    suspend fun toggleAppBlocked(packageName: String, isBlocked: Boolean)

    /**
     * Batch blocks or unblocks all apps within a category (e.g. Social, Gaming).
     */
    suspend fun setCategoryBlocked(category: AppCategory, isBlocked: Boolean)

    /**
     * Scans and refreshes the installed launchable applications list on the device.
     */
    suspend fun refreshInstalledApps()

    /**
     * Logs an intercepted distraction attempt when a student tries to open a blocked app.
     * @return The updated number of times this specific package was resisted today.
     */
    suspend fun recordDistractionAttempt(packageName: String): Int

    /**
     * Checks if the Quovex Accessibility Service is currently active in Android settings.
     */
    fun isAccessibilityServiceRunning(): Boolean

    /**
     * Updates accessibility service active flag.
     */
    fun updateAccessibilityStatus(isActive: Boolean)
}
