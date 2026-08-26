package com.quovex.data.repository

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.view.accessibility.AccessibilityManager
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.BlockedAppInfo
import com.quovex.domain.model.DistractionShieldState
import com.quovex.domain.model.KnownDistractorPackages
import com.quovex.domain.repository.DistractionBlockerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DistractionBlockerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DistractionBlockerRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("quovex_distraction_shield_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _shieldState = MutableStateFlow(
        DistractionShieldState(
            isShieldEnabled = prefs.getBoolean(KEY_SHIELD_ENABLED, true),
            isAccessibilityServiceEnabled = isAccessibilityServiceRunning()
        )
    )
    override val shieldState: StateFlow<DistractionShieldState> = _shieldState.asStateFlow()

    private val attemptCounters = mutableMapOf<String, Int>()

    init {
        scope.launch {
            refreshInstalledApps()
        }
    }

    override suspend fun setShieldEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHIELD_ENABLED, enabled).apply()
        _shieldState.update { it.copy(isShieldEnabled = enabled) }
    }

    override suspend fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        val currentBlocked = getPersistedBlockedPackages().toMutableSet()
        if (isBlocked) {
            currentBlocked.add(packageName)
        } else {
            currentBlocked.remove(packageName)
        }
        persistBlockedPackages(currentBlocked)

        _shieldState.update { state ->
            val updatedList = state.installedApps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(isBlocked = isBlocked)
                } else app
            }
            state.copy(installedApps = updatedList)
        }
    }

    override suspend fun setCategoryBlocked(category: AppCategory, isBlocked: Boolean) {
        val currentBlocked = getPersistedBlockedPackages().toMutableSet()

        _shieldState.update { state ->
            val updatedList = state.installedApps.map { app ->
                if (app.category == category) {
                    if (isBlocked) currentBlocked.add(app.packageName) else currentBlocked.remove(app.packageName)
                    app.copy(isBlocked = isBlocked)
                } else app
            }
            persistBlockedPackages(currentBlocked)
            state.copy(installedApps = updatedList)
        }
    }

    override suspend fun refreshInstalledApps() {
        try {
            val packageManager = context.packageManager ?: return
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = try {
                packageManager.queryIntentActivities(intent, 0)
            } catch (_: Throwable) {
                emptyList()
            } ?: emptyList()

            val persistedBlocked = getPersistedBlockedPackages()
            val ownPackage = try { context.packageName } catch (_: Throwable) { "com.quovex" } ?: "com.quovex"

            val apps = resolveInfos
                .mapNotNull { it.activityInfo?.applicationInfo }
                .distinctBy { it.packageName }
                .filter { it.packageName != ownPackage }
                .map { appInfo ->
                    val pkgName = appInfo.packageName ?: ""
                    val label = try { packageManager.getApplicationLabel(appInfo)?.toString() } catch (_: Throwable) { null } ?: pkgName
                    val category = KnownDistractorPackages.categorizePackage(pkgName)
                    val isBlocked = if (persistedBlocked.isEmpty()) {
                        category == AppCategory.SOCIAL || category == AppCategory.GAMING || category == AppCategory.STREAMING
                    } else {
                        persistedBlocked.contains(pkgName)
                    }

                    BlockedAppInfo(
                        packageName = pkgName,
                        appName = label,
                        category = category,
                        isBlocked = isBlocked,
                        attemptsResistedCount = attemptCounters[pkgName] ?: 0
                    )
                }
                .sortedWith(compareBy({ !it.isBlocked }, { it.category.ordinal }, { it.appName }))

            if (persistedBlocked.isEmpty() && apps.isNotEmpty()) {
                persistBlockedPackages(apps.filter { it.isBlocked }.map { it.packageName }.toSet())
            }

            _shieldState.update {
                it.copy(
                    installedApps = apps,
                    isAccessibilityServiceEnabled = isAccessibilityServiceRunning()
                )
            }
        } catch (_: Throwable) {
            // Guard background initialization
        }
    }

    override suspend fun recordDistractionAttempt(packageName: String): Int {
        val count = (attemptCounters[packageName] ?: 0) + 1
        attemptCounters[packageName] = count
        val totalAttempts = attemptCounters.values.sum()

        _shieldState.update { state ->
            val updatedApps = state.installedApps.map { app ->
                if (app.packageName == packageName) {
                    app.copy(attemptsResistedCount = count)
                } else app
            }
            state.copy(
                installedApps = updatedApps,
                totalAttemptsResistedToday = totalAttempts
            )
        }
        return count
    }

    override fun isAccessibilityServiceRunning(): Boolean {
        return try {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
            val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC) ?: emptyList()
            val expectedService = "${context.packageName}/com.quovex.data.service.QuovexAccessibilityService"
            val shortExpectedService = "${context.packageName}/.data.service.QuovexAccessibilityService"

            enabledServices.any { service ->
                val id = service?.id ?: ""
                id.contains(expectedService, ignoreCase = true) ||
                    id.contains(shortExpectedService, ignoreCase = true) ||
                    id.contains("QuovexAccessibilityService", ignoreCase = true)
            }
        } catch (_: Throwable) {
            false
        }
    }

    override fun updateAccessibilityStatus(isActive: Boolean) {
        _shieldState.update { it.copy(isAccessibilityServiceEnabled = isActive) }
    }

    private fun getPersistedBlockedPackages(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_PACKAGES, emptySet()) ?: emptySet()
    }

    private fun persistBlockedPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_PACKAGES, packages).apply()
    }

    companion object {
        private const val KEY_SHIELD_ENABLED = "key_distraction_shield_enabled"
        private const val KEY_BLOCKED_PACKAGES = "key_blocked_package_names"
    }
}
