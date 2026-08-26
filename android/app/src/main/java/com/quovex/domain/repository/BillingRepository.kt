package com.quovex.domain.repository

import com.quovex.domain.model.PurchaseResult
import com.quovex.domain.model.SubscriptionPlan
import com.quovex.domain.model.SubscriptionTier
import com.quovex.domain.model.UserEntitlement
import kotlinx.coroutines.flow.StateFlow

/**
 * Domain repository contract for managing subscriptions, Google Play billing, and AI quota entitlements.
 */
interface BillingRepository {

    /**
     * Real-time stream of the user's active entitlement and remaining AI queries.
     */
    val userEntitlement: StateFlow<UserEntitlement>

    /**
     * Real-time stream of available subscription and one-time purchase plans.
     */
    val availablePlans: StateFlow<List<SubscriptionPlan>>

    /**
     * Checks if the user has remaining AI queries or Pro access, and decrements the daily counter if on Free tier.
     * @return True if the query is permitted, False if the free tier daily limit has been reached.
     */
    suspend fun checkAndConsumeAiQuery(): Boolean

    /**
     * Restores previously purchased subscriptions from Google Play and updates entitlement state.
     */
    suspend fun restorePurchases(): Result<UserEntitlement>

    /**
     * Refreshes the user's subscription and daily quota status.
     */
    suspend fun refreshEntitlements()

    /**
     * Grants bonus AI queries to the user's daily quota (e.g. from watching a rewarded ad).
     */
    fun grantBonusAiQueries(bonusCount: Int)

    /**
     * Simulates purchase activation (used for debug/offline preview or testing).
     */
    suspend fun activatePlan(plan: SubscriptionPlan): PurchaseResult
}
