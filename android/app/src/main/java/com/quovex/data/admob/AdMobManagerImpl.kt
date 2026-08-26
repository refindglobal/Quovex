package com.quovex.data.admob

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.quovex.domain.manager.AdManager
import com.quovex.domain.model.AdRewardResult
import com.quovex.domain.model.AdState
import com.quovex.domain.model.AdUnitIds
import com.quovex.domain.repository.BillingRepository
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
class AdMobManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billingRepository: BillingRepository
) : AdManager {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _adState = MutableStateFlow(AdState())
    override val adState: StateFlow<AdState> = _adState.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    init {
        initialize()
    }

    override fun initialize() {
        try {
            MobileAds.initialize(context) {}
        } catch (_: Throwable) {
            // Safe guard against missing services in test
        }

        scope.launch {
            billingRepository.userEntitlement.collect { entitlement ->
                if (entitlement.isAdFree) {
                    interstitialAd = null
                    rewardedAd = null
                    _adState.update {
                        it.copy(
                            isBannerEnabled = false,
                            isRewardedAdReady = false,
                            isInterstitialAdReady = false
                        )
                    }
                } else {
                    _adState.update { it.copy(isBannerEnabled = true) }
                    preloadInterstitial()
                    preloadRewarded()
                }
            }
        }
    }

    override fun preloadInterstitial() {
        if (billingRepository.userEntitlement.value.isAdFree) return
        if (interstitialAd != null || isLoadingInterstitial) return

        isLoadingInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                AdUnitIds.TEST_INTERSTITIAL,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        _adState.update { it.copy(isInterstitialAdReady = true) }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        _adState.update { it.copy(isInterstitialAdReady = false) }
                    }
                }
            )
        } catch (_: Throwable) {
            isLoadingInterstitial = false
        }
    }

    override fun showInterstitial(activity: Activity): Boolean {
        if (billingRepository.userEntitlement.value.isAdFree) return false

        val ad = interstitialAd ?: run {
            preloadInterstitial()
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                _adState.update { it.copy(isInterstitialAdReady = false) }
                preloadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                _adState.update { it.copy(isInterstitialAdReady = false) }
                preloadInterstitial()
            }
        }

        ad.show(activity)
        return true
    }

    override fun preloadRewarded() {
        if (billingRepository.userEntitlement.value.isAdFree) return
        if (rewardedAd != null || isLoadingRewarded) return

        isLoadingRewarded = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                AdUnitIds.TEST_REWARDED,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                        _adState.update { it.copy(isRewardedAdReady = true) }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                        _adState.update { it.copy(isRewardedAdReady = false) }
                    }
                }
            )
        } catch (_: Throwable) {
            isLoadingRewarded = false
        }
    }

    override fun showRewarded(
        activity: Activity,
        onResult: (AdRewardResult) -> Unit
    ) {
        if (billingRepository.userEntitlement.value.isAdFree) {
            onResult(AdRewardResult.AdFreeSubscriber)
            return
        }

        val ad = rewardedAd ?: run {
            preloadRewarded()
            onResult(AdRewardResult.Error("Rewarded video is loading. Please try again in a few seconds."))
            return
        }

        var rewardEarned = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _adState.update { it.copy(isRewardedAdReady = false) }
                preloadRewarded()
                if (!rewardEarned) {
                    onResult(AdRewardResult.DismissedEarly)
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _adState.update { it.copy(isRewardedAdReady = false) }
                preloadRewarded()
                onResult(AdRewardResult.Error(error.message))
            }
        }

        ad.show(activity) { rewardItem ->
            rewardEarned = true
            val bonusAmount = if (rewardItem.amount > 0) rewardItem.amount else 5
            onResult(AdRewardResult.Success(bonusAmount))
        }
    }
}
