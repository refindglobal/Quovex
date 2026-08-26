package com.quovex.domain.manager

import android.app.Activity
import com.quovex.domain.model.AdRewardResult
import com.quovex.domain.model.AdState
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for managing Google Mobile Ads (AdMob) loading, presentation, and Pro-tier exemptions.
 */
interface AdManager {

    /**
     * Observable stream of ad readiness and visibility state.
     */
    val adState: StateFlow<AdState>

    /**
     * Initializes Google Mobile Ads SDK on app startup.
     */
    fun initialize()

    /**
     * Preloads an Interstitial Ad into memory if the user is on the Free tier.
     */
    fun preloadInterstitial()

    /**
     * Displays a full-screen Interstitial Ad (e.g. after a focus session) for Free-tier users.
     * @return true if an ad was shown, false if bypassed (Pro subscriber) or unavailable.
     */
    fun showInterstitial(activity: Activity): Boolean

    /**
     * Preloads a Rewarded Video Ad into memory if the user is on the Free tier.
     */
    fun preloadRewarded()

    /**
     * Presents an opt-in Rewarded Video Ad to grant bonus AI queries.
     */
    fun showRewarded(
        activity: Activity,
        onResult: (AdRewardResult) -> Unit
    )
}
