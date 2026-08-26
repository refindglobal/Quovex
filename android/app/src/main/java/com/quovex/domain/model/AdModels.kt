package com.quovex.domain.model

/**
 * Result returned when a user engages with a Rewarded Ad.
 */
sealed class AdRewardResult {
    data class Success(val bonusQueries: Int = 3) : AdRewardResult()
    object DismissedEarly : AdRewardResult()
    data class Error(val message: String) : AdRewardResult()
    object AdFreeSubscriber : AdRewardResult()
}

/**
 * Dynamic configuration fetched from Firestore (config/admob) or fallback defaults.
 */
data class AdMobRemoteConfig(
    val bannerAdUnitId: String = AdUnitIds.TEST_BANNER,
    val interstitialAdUnitId: String = AdUnitIds.TEST_INTERSTITIAL,
    val rewardedAdUnitId: String = AdUnitIds.TEST_REWARDED,
    val isAdsEnabledGlobally: Boolean = true,
    val bonusAiQueriesPerReward: Int = 3
)

/**
 * Observable status of cached Google Mobile Ads and dynamic unit IDs.
 */
data class AdState(
    val isBannerEnabled: Boolean = true,
    val isRewardedAdReady: Boolean = false,
    val isInterstitialAdReady: Boolean = false,
    val bannerAdUnitId: String = AdUnitIds.TEST_BANNER,
    val bonusAiQueriesPerReward: Int = 3
)

/**
 * Standard test Ad Unit IDs provided by Google AdMob for secure development and verification.
 */
object AdUnitIds {
    const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
}
