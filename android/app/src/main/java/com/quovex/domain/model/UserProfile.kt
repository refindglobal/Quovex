package com.quovex.domain.model

data class UserProfile(
    val id: String = "user_default",
    val name: String = "Arjun Sharma",
    val avatarId: Int = 1, // 1 through 12
    val targetExam: String = "JEE Advanced",
    val dailyGoalHours: Float = 4.0f,
    val streakDays: Int = 14,
    val xp: Int = 12400,
    val level: Int = 24,
    val isOnboarded: Boolean = false,
    val email: String = "arjun.sharma@quovex.app"
)
