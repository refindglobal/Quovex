package com.quovex.domain.model

enum class SoundscapeCategory(val displayName: String) {
    ALL("All"),
    BINAURAL("Binaural Beats"),
    NATURE("Nature & Rain"),
    NOISE("Noise Masks")
}

enum class NoiseType {
    NONE,
    WHITE,
    PINK,
    BROWN,
    RAIN
}

data class SoundscapePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: SoundscapeCategory,
    val iconEmoji: String,
    val isBinaural: Boolean = false,
    val baseFrequencyHz: Double = 0.0,
    val beatFrequencyHz: Double = 0.0,
    val noiseType: NoiseType = NoiseType.NONE,
    val tag: String = ""
)

object SoundscapePresets {

    val NONE = SoundscapePreset(
        id = "none",
        title = "Silent Focus",
        subtitle = "No ambient audio",
        description = "Pure silence for distraction-free deep work without soundscapes.",
        category = SoundscapeCategory.ALL,
        iconEmoji = "🔇",
        tag = "Silent"
    )

    val BINAURAL_ALPHA_10HZ = SoundscapePreset(
        id = "binaural_alpha_10hz",
        title = "Binaural Alpha (10 Hz)",
        subtitle = "Deep Flow & Study State",
        description = "Entrains 10 Hz alpha brainwaves associated with relaxed focus, high comprehension, and mental calm.",
        category = SoundscapeCategory.BINAURAL,
        iconEmoji = "🧘",
        isBinaural = true,
        baseFrequencyHz = 200.0,
        beatFrequencyHz = 10.0,
        tag = "Alpha • 10 Hz"
    )

    val BINAURAL_GAMMA_40HZ = SoundscapePreset(
        id = "binaural_gamma_40hz",
        title = "Binaural Gamma (40 Hz)",
        subtitle = "Peak Cognition & Laser Focus",
        description = "40 Hz gamma frequency for intense problem-solving, active recall, and sharp numerical processing.",
        category = SoundscapeCategory.BINAURAL,
        iconEmoji = "⚡",
        isBinaural = true,
        baseFrequencyHz = 280.0,
        beatFrequencyHz = 40.0,
        tag = "Gamma • 40 Hz"
    )

    val BINAURAL_BETA_20HZ = SoundscapePreset(
        id = "binaural_beta_20hz",
        title = "Binaural Beta (20 Hz)",
        subtitle = "Active Exam Simulation",
        description = "20 Hz beta frequency promotes alertness, sustained vigilance, and speed during timed mock tests.",
        category = SoundscapeCategory.BINAURAL,
        iconEmoji = "🎯",
        isBinaural = true,
        baseFrequencyHz = 240.0,
        beatFrequencyHz = 20.0,
        tag = "Beta • 20 Hz"
    )

    val BINAURAL_THETA_6HZ = SoundscapePreset(
        id = "binaural_theta_6hz",
        title = "Binaural Theta (6 Hz)",
        subtitle = "Memory & Intuitive Learning",
        description = "6 Hz theta frequency designed for concept internalization, reading revision, and stress reduction.",
        category = SoundscapeCategory.BINAURAL,
        iconEmoji = "🌌",
        isBinaural = true,
        baseFrequencyHz = 180.0,
        beatFrequencyHz = 6.0,
        tag = "Theta • 6 Hz"
    )

    val RAIN_MONSOON = SoundscapePreset(
        id = "rain_monsoon",
        title = "Deep Monsoon Rain",
        subtitle = "Calming Rainfall & Drops",
        description = "Procedurally synthesized steady rainfall with soft water droplet acoustics to mask noisy surroundings.",
        category = SoundscapeCategory.NATURE,
        iconEmoji = "🌧️",
        noiseType = NoiseType.RAIN,
        tag = "Nature"
    )

    val BROWN_NOISE = SoundscapePreset(
        id = "brown_noise",
        title = "Deep Brown Noise",
        subtitle = "Warm Low-Frequency Hum",
        description = "Deep, warm Brownian noise with heavier bass roll-off. Excellent for ADHD focus and silencing background chatter.",
        category = SoundscapeCategory.NOISE,
        iconEmoji = "☕",
        noiseType = NoiseType.BROWN,
        tag = "ADHD Focus"
    )

    val PINK_NOISE = SoundscapePreset(
        id = "pink_noise",
        title = "Soft Pink Noise",
        subtitle = "Balanced 1/f Spectrum",
        description = "Equal energy per octave noise, mimicking natural waterfalls and wind. Improves memory retention during study.",
        category = SoundscapeCategory.NOISE,
        iconEmoji = "🌸",
        noiseType = NoiseType.PINK,
        tag = "1/f Pink"
    )

    val WHITE_NOISE = SoundscapePreset(
        id = "white_noise",
        title = "Clean White Noise",
        subtitle = "Complete Acoustic Masking",
        description = "Full spectrum uniform noise that blankets unpredictable sudden noises in shared rooms and libraries.",
        category = SoundscapeCategory.NOISE,
        iconEmoji = "📻",
        noiseType = NoiseType.WHITE,
        tag = "Acoustic Mask"
    )

    val ALL_PRESETS = listOf(
        NONE,
        BINAURAL_ALPHA_10HZ,
        BINAURAL_GAMMA_40HZ,
        BINAURAL_BETA_20HZ,
        BINAURAL_THETA_6HZ,
        RAIN_MONSOON,
        BROWN_NOISE,
        PINK_NOISE,
        WHITE_NOISE
    )

    fun findById(id: String): SoundscapePreset {
        return ALL_PRESETS.firstOrNull { it.id == id } ?: NONE
    }
}

data class SoundscapeState(
    val selectedPreset: SoundscapePreset = SoundscapePresets.BINAURAL_ALPHA_10HZ,
    val isPlaying: Boolean = false,
    val volume: Float = 0.75f,
    val isAutoPlayWithTimerEnabled: Boolean = true
)
