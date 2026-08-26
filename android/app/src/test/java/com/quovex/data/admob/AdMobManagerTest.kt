package com.quovex.data.admob

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.quovex.domain.model.AdRewardResult
import com.quovex.domain.model.SubscriptionTier
import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.repository.BillingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdMobManagerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var billingRepository: BillingRepository
    private val entitlementFlow = MutableStateFlow(UserEntitlement())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(any(), any()) } returns Unit

        billingRepository = mockk(relaxed = true)
        every { billingRepository.userEntitlement } returns entitlementFlow
    }

    @After
    fun tearDown() {
        unmockkStatic(MobileAds::class)
        Dispatchers.resetMain()
    }

    @Test
    fun testProUserBypassesRewardedAdImmediately() = runTest {
        // Set Pro Tier
        entitlementFlow.value = UserEntitlement.createProTier(SubscriptionTier.PRO_ANNUAL)

        val context = mockk<android.content.Context>(relaxed = true)
        val adManager = AdMobManagerImpl(context, billingRepository)
        advanceUntilIdle()

        val mockActivity = mockk<Activity>(relaxed = true)

        var result: AdRewardResult? = null
        adManager.showRewarded(mockActivity) {
            result = it
        }

        assertEquals(AdRewardResult.AdFreeSubscriber, result)
        assertFalse(adManager.adState.value.isBannerEnabled)
    }

    @Test
    fun testProUserBypassesInterstitialAd() = runTest {
        entitlementFlow.value = UserEntitlement.createProTier(SubscriptionTier.LIFETIME)

        val context = mockk<android.content.Context>(relaxed = true)
        val adManager = AdMobManagerImpl(context, billingRepository)
        advanceUntilIdle()

        val mockActivity = mockk<Activity>(relaxed = true)

        val shown = adManager.showInterstitial(mockActivity)
        assertFalse(shown)
    }

    @Test
    fun testFreeUserBannerIsEnabledByDefault() = runTest {
        entitlementFlow.value = UserEntitlement(tier = SubscriptionTier.FREE, isAdFree = false)

        val context = mockk<android.content.Context>(relaxed = true)
        val adManager = AdMobManagerImpl(context, billingRepository)
        advanceUntilIdle()

        assertTrue(adManager.adState.value.isBannerEnabled)
    }
}
