package com.quovex.data.local

import android.content.SharedPreferences
import com.quovex.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

open class UserPreferencesManager(
    private val prefs: SharedPreferences? = null
) {

    private val _userProfile = MutableStateFlow(loadProfileFromPrefs())
    open val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeModeFromPrefs())
    open val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private fun loadProfileFromPrefs(): UserProfile {
        val p = prefs ?: return UserProfile(
            id = "user_1",
            name = "Aspirant",
            avatarId = 1,
            targetExam = "JEE Advanced",
            dailyGoalHours = 4.0f,
            streakDays = 1,
            xp = 0,
            level = 1,
            isOnboarded = true,
            email = "student@quovex.app",
            rescueTokens = 1
        )
        return UserProfile(
            id = p.getString("user_id", "user_1") ?: "user_1",
            name = p.getString("user_name", "Aspirant") ?: "Aspirant",
            avatarId = p.getInt("avatar_id", 1),
            targetExam = p.getString("target_exam", "JEE Advanced") ?: "JEE Advanced",
            dailyGoalHours = p.getFloat("daily_goal_hours", 4.0f),
            streakDays = p.getInt("streak_days", 1),
            xp = p.getInt("user_xp", 0),
            level = p.getInt("user_level", 1),
            isOnboarded = p.getBoolean("is_onboarded", true),
            email = p.getString("user_email", "student@quovex.app") ?: "student@quovex.app",
            rescueTokens = p.getInt("rescue_tokens", 1)
        )
    }

    private fun loadThemeModeFromPrefs(): ThemeMode {
        val p = prefs ?: return ThemeMode.SYSTEM
        val modeName = p.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        return try {
            ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    open fun setThemeMode(mode: ThemeMode) {
        prefs?.edit()?.putString("theme_mode", mode.name)?.apply()
        _themeMode.value = mode
    }

    open fun saveUserProfile(profile: UserProfile) {
        prefs?.edit()?.apply {
            putString("user_id", profile.id)
            putString("user_name", profile.name)
            putInt("avatar_id", profile.avatarId)
            putString("target_exam", profile.targetExam)
            putFloat("daily_goal_hours", profile.dailyGoalHours)
            putInt("streak_days", profile.streakDays)
            putInt("user_xp", profile.xp)
            putInt("user_level", profile.level)
            putBoolean("is_onboarded", profile.isOnboarded)
            putString("user_email", profile.email)
            putInt("rescue_tokens", profile.rescueTokens)
            apply()
        }
        _userProfile.value = profile
    }

    open fun isOnboardingCompleted(): Boolean {
        return prefs?.getBoolean("is_onboarded", false) ?: false
    }

    open fun setOnboardingCompleted(completed: Boolean) {
        prefs?.edit()?.putBoolean("is_onboarded", completed)?.apply()
        _userProfile.value = _userProfile.value.copy(isOnboarded = completed)
    }

    open fun setDailyStudyTarget(hours: Float) {
        prefs?.edit()?.putFloat("daily_goal_hours", hours)?.apply()
        _userProfile.value = _userProfile.value.copy(dailyGoalHours = hours)
    }

    open fun completeOnboarding(name: String, avatarId: Int, targetExam: String, dailyGoalHours: Float) {
        val current = _userProfile.value
        val updated = current.copy(
            name = name,
            avatarId = avatarId,
            targetExam = targetExam,
            dailyGoalHours = dailyGoalHours,
            isOnboarded = true
        )
        saveUserProfile(updated)
    }

    open fun addXpAndSession(focusMinutes: Long, gainedXp: Int) {
        val current = _userProfile.value
        val newXp = current.xp + gainedXp
        val newLevel = (newXp / 500) + 1
        val updated = current.copy(
            xp = newXp,
            level = newLevel
        )
        saveUserProfile(updated)
    }

    open fun spendRescueToken(): Boolean {
        val current = _userProfile.value
        if (current.rescueTokens <= 0) return false
        val updated = current.copy(rescueTokens = current.rescueTokens - 1)
        saveUserProfile(updated)
        return true
    }

    open fun grantRescueToken(amount: Int = 1) {
        val current = _userProfile.value
        val updated = current.copy(rescueTokens = (current.rescueTokens + amount).coerceAtMost(5))
        saveUserProfile(updated)
    }

    open fun resetProfileForNewUser() {
        val fresh = UserProfile(
            id = "user_${System.currentTimeMillis()}",
            name = "",
            avatarId = 1,
            targetExam = "JEE Advanced",
            dailyGoalHours = 4.0f,
            streakDays = 1,
            xp = 0,
            level = 1,
            isOnboarded = false,
            email = "student@quovex.app",
            rescueTokens = 1
        )
        saveUserProfile(fresh)
    }
}
