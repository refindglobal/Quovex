package com.quovex.domain.usecase

import android.app.Activity
import com.quovex.domain.manager.AdManager
import javax.inject.Inject

class ShowInterstitialAdUseCase @Inject constructor(
    private val adManager: AdManager
) {
    operator fun invoke(activity: Activity): Boolean {
        return adManager.showInterstitial(activity)
    }

    fun preload() {
        adManager.preloadInterstitial()
    }
}
