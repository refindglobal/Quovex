package com.quovex.domain.model

/**
 * Domain representation of focus session presets.
 * Source of truth for mode definitions, focus durations, and break durations.
 */
sealed class FocusMode(
    val title: String,
    val focusDurationMinutes: Int,
    val breakDurationMinutes: Int,
    val description: String
) {
    object Pomodoro : FocusMode(
        title = "Pomodoro",
        focusDurationMinutes = 25,
        breakDurationMinutes = 5,
        description = "25m focus • 5m break"
    )

    object DeepWork : FocusMode(
        title = "Deep Work",
        focusDurationMinutes = 50,
        breakDurationMinutes = 10,
        description = "50m focus • 10m break"
    )

    object LongDeepWork : FocusMode(
        title = "Long Deep Work",
        focusDurationMinutes = 90,
        breakDurationMinutes = 20,
        description = "90m focus • 20m break"
    )

    data class Custom(
        val customFocusMinutes: Int = 30,
        val customBreakMinutes: Int = 5
    ) : FocusMode(
        title = "Custom",
        focusDurationMinutes = customFocusMinutes,
        breakDurationMinutes = customBreakMinutes,
        description = "${customFocusMinutes}m focus • ${customBreakMinutes}m break"
    )

    companion object {
        val defaultPresets: List<FocusMode> = listOf(Pomodoro, DeepWork, LongDeepWork)
    }
}
