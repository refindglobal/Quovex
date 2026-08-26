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
import com.google.firebase.firestore.FirebaseFirestore
import com.quovex.domain.manager.AdManager
import com.quovex.domain.model.AdMobRemoteConfig
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
    private val billingRepository: BillingRepository,
    private val firestore: FirebaseFirestore? = null
) : AdManager {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _remoteConfig = MutableStateFlow(AdMobRemoteConfig())
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
            // Guard against missing services in test
        }

        // Listen for real-time dynamic AdMob configuration updates from Firestore (config/admob)
        try {
            val db = firestore ?: FirebaseFirestore.getInstance()
            db.collection("config").document("admob")
                .addSnapshotListener { snapshot, error ->
                    if (error == null && snapshot != null && snapshot.exists()) {
                        val banner = snapshot.getString("bannerAdUnitId") ?: AdUnitIds.TEST_BANNER
                        val interstitial = snapshot.getString("interstitialAdUnitId") ?: AdUnitIds.TEST_INTERSTITIAL
                        val rewarded = snapshot.getString("rewardedAdUnitId") ?: AdUnitIds.TEST_REWARDED
                        val adsEnabled = snapshot.getBoolean("adsEnabled") ?: true
                        val bonusQueries = snapshot.getLong("bonusAiQueriesPerReward")?.toInt() ?: 3

                        _remoteConfig.value = AdMobRemoteConfig(
                            bannerAdUnitId = banner,
                            interstitialAdUnitId = interstitial,
                            rewardedAdUnitId = rewarded,
                            isAdsEnabledGlobally = adsEnabled,
                            bonusAiQueriesPerReward = bonusQueries
                        )

                        val isAdFree = billingRepository.userEntitlement.value.isAdFree
                        _adState.update { current ->
                            current.copy(
                                isBannerEnabled = adsEnabled && !isAdFree,
                                bannerAdUnitId = banner,
                                bonusAiQueriesPerReward = bonusQueries
                            )
                        }

                        if (!isAdFree && adsEnabled) {
                            preloadInterstitial()
                            preloadRewarded()
                        }
                    }
                }
        } catch (_: Throwable) {
            // Graceful fallback for offline or unit test execution
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
                    val isGloballyEnabled = _remoteConfig.value.isAdsEnabledGlobally
                    _adState.update { it.copy(isBannerEnabled = isGloballyEnabled) }
                    if (isGloballyEnabled) {
                        preloadInterstitial()
                        preloadRewarded()
                    }
                }
            }
        }
    }

    override fun preloadInterstitial() {
        val config = _remoteConfig.value
        if (!config.isAdsEnabledGlobally) return
        if (billingRepository.userEntitlement.value.isAdFree) return
        if (interstitialAd != null || isLoadingInterstitial) return

        isLoadingInterstitial = true
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                config.interstitialAdUnitId,
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
        if (!_remoteConfig.value.isAdsEnabledGlobally) return false
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
        val config = _remoteConfig.value
        if (!config.isAdsEnabledGlobally) return
        if (billingRepository.userEntitlement.value.isAdFree) return
        if (rewardedAd != null || isLoadingRewarded) return

        isLoadingRewarded = true
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                config.rewardedAdUnitId,
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
            val bonusAmount = if (rewardItem.amount > 0) rewardItem.amount else _remoteConfig.value.bonusAiQueriesPerReward
            onResult(AdRewardResult.Success(bonusAmount))
        }
    }
}
