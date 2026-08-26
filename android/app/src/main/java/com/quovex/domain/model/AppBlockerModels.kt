package com.quovex.domain.model

/**
 * Categories of installed apps for quick batch blocking.
 */
enum class AppCategory(val title: String, val iconEmoji: String) {
    SOCIAL("Social Media", "📱"),
    STREAMING("Video & Streaming", "🎬"),
    GAMING("Gaming", "🎮"),
    BROWSING("Web Browsers", "🌐"),
    ENTERTAINMENT("Entertainment", "🍿"),
    CUSTOM("Other Apps", "📦")
}

/**
 * Information about an installed application that can be blocked during focus sessions.
 */
data class BlockedAppInfo(
    val packageName: String,
    val appName: String,
    val category: AppCategory = AppCategory.CUSTOM,
    val isBlocked: Boolean = false,
    val attemptsResistedCount: Int = 0
)

/**
 * Log record of a blocked app interception event during a focus session.
 */
data class DistractionEvent(
    val packageName: String,
    val appName: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val sessionSubject: String = ""
)

/**
 * Active state of the Distraction Shield engine.
 */
data class DistractionShieldState(
    val isShieldEnabled: Boolean = true,
    val isAccessibilityServiceEnabled: Boolean = false,
    val installedApps: List<BlockedAppInfo> = emptyList(),
    val totalAttemptsResistedToday: Int = 0
) {
    val blockedPackages: Set<String>
        get() = if (isShieldEnabled) {
            installedApps.filter { it.isBlocked }.map { it.packageName }.toSet()
        } else {
            emptySet()
        }

    val blockedCount: Int
        get() = if (isShieldEnabled) installedApps.count { it.isBlocked } else 0
}

/**
 * Default curated dictionary mapping package signatures to app categories.
 */
object KnownDistractorPackages {
    val SOCIAL_PACKAGES = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.snapchat.android",
        "com.twitter.android",
        "com.facebook.katana",
        "com.facebook.orca", // Messenger
        "com.reddit.frontpage",
        "com.discord",
        "org.telegram.messenger",
        "com.whatsapp",
        "com.threads.android",
        "com.pinterest"
    )

    val STREAMING_PACKAGES = setOf(
        "com.google.android.youtube",
        "com.netflix.mediaclient",
        "com.amazon.avod.thirdpartyclient", // Prime Video
        "com.disney.disneyplus",
        "in.startv.hotstar", // Disney+ Hotstar
        "com.spotify.music",
        "tv.twitch.android.app",
        "com.jio.media.ondemand" // JioCinema
    )

    val GAMING_PACKAGES = setOf(
        "com.tencent.ig", // PUBG
        "com.pubg.imobile", // BGMI
        "com.dts.freefireth",
        "com.dts.freefiremax",
        "com.king.candycrushsaga",
        "com.supercell.clashofclans",
        "com.supercell.clashroyale",
        "com.roblox.client",
        "com.ea.gp.fifamobile",
        "com.miHoYo.GenshinImpact"
    )

    val BROWSING_PACKAGES = setOf(
        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.brave.browser",
        "com.microsoft.emmx" // Edge
    )

    fun categorizePackage(packageName: String): AppCategory {
        return when {
            SOCIAL_PACKAGES.contains(packageName) -> AppCategory.SOCIAL
            STREAMING_PACKAGES.contains(packageName) -> AppCategory.STREAMING
            GAMING_PACKAGES.contains(packageName) -> AppCategory.GAMING
            BROWSING_PACKAGES.contains(packageName) -> AppCategory.BROWSING
            else -> AppCategory.CUSTOM
        }
    }
}
