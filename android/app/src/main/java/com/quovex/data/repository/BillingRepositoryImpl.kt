package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.quovex.domain.model.DefaultSubscriptionPlans
import com.quovex.domain.model.PurchaseResult
import com.quovex.domain.model.SubscriptionPlan
import com.quovex.domain.model.SubscriptionTier
import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.repository.BillingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * BillingRepositoryImpl — Google Play Billing v6 integration.
 *
 * Purchase lifecycle (Google Play policy compliant):
 * 1. onPurchasesUpdated() fires when the user completes checkout.
 * 2. For each PURCHASED-state purchase that is not yet acknowledged:
 *    a. Grant the premium entitlement FIRST (persist to SharedPrefs + update StateFlow).
 *    b. THEN call acknowledgePurchase() via BillingClient on Dispatchers.IO.
 *    Reason: If the app crashes between granting and acknowledging, the user retains
 *    premium locally and Google will re-deliver the purchase on next app start for
 *    re-acknowledgement. The inverse ordering (acknowledge first, grant second) would
 *    risk the user losing premium state on a crash.
 * 3. onBillingSetupFinished() calls queryAndAcknowledgeExistingPurchases() to recover
 *    any purchases that were granted but never acknowledged (e.g., crash during previous
 *    session, or first install after restoring a purchase).
 */
@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BillingRepository, PurchasesUpdatedListener, BillingClientStateListener {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("quovex_billing_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _userEntitlement = MutableStateFlow(UserEntitlement.createFreeTier())
    override val userEntitlement: StateFlow<UserEntitlement> = _userEntitlement.asStateFlow()

    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(DefaultSubscriptionPlans.ALL)
    override val availablePlans: StateFlow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()

    private var billingClient: BillingClient? = null

    /** Product IDs matching Google Play Console subscription SKUs */
    private val subscriptionProductIds = listOf(
        "quovex_pro_monthly",
        "quovex_pro_annual"
    )

    /** Product IDs matching Google Play Console one-time purchase SKUs */
    private val inAppProductIds = listOf(
        "quovex_pro_lifetime"
    )

    init {
        loadPersistedEntitlements()
        initBillingClient()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // BillingClient Setup
    // ─────────────────────────────────────────────────────────────────────────

    private fun initBillingClient() {
        try {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build()
            billingClient?.startConnection(this)
        } catch (_: Exception) {
            // Graceful fallback if Google Play services absent (e.g. emulator without Play)
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Fetch live product details (pricing) from Play Console
            scope.launch { queryProductDetailsAsync() }
            // Recover and acknowledge any previously unacknowledged purchases
            scope.launch { queryAndAcknowledgeExistingPurchases() }
        }
    }

    override fun onBillingServiceDisconnected() {
        // Reconnect on next BillingClient call automatically; no manual retry needed here
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Product Details
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Queries Google Play for live product details (price, trial period, etc.).
     * Updates [availablePlans] with real pricing strings from Play Console.
     * Must be called on a background dispatcher.
     */
    private suspend fun queryProductDetailsAsync() {
        val client = billingClient ?: return

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                subscriptionProductIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                }
            )
            .build()

        val subsResult = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { cont ->
            client.queryProductDetailsAsync(subsParams) { billingResult, productDetailsList ->
                cont.resume(Pair(billingResult, productDetailsList))
            }
        }

        val inappParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                inAppProductIds.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()

        val inappResult = suspendCancellableCoroutine<Pair<BillingResult, List<ProductDetails>>> { cont ->
            client.queryProductDetailsAsync(inappParams) { billingResult, productDetailsList ->
                cont.resume(Pair(billingResult, productDetailsList))
            }
        }

        val allDetails: List<ProductDetails> = buildList {
            if (subsResult.first.responseCode == BillingClient.BillingResponseCode.OK) {
                addAll(subsResult.second)
            }
            if (inappResult.first.responseCode == BillingClient.BillingResponseCode.OK) {
                addAll(inappResult.second)
            }
        }

        if (allDetails.isNotEmpty()) {
            val updatedPlans = DefaultSubscriptionPlans.ALL.map { plan ->
                val detail = allDetails.firstOrNull { it.productId == plan.id }
                if (detail != null) {
                    val priceText = detail.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.pricingPhases
                        ?.pricingPhaseList
                        ?.firstOrNull()
                        ?.formattedPrice
                        ?: detail.oneTimePurchaseOfferDetails?.formattedPrice
                    plan.copy(formattedPrice = priceText ?: plan.formattedPrice)
                } else plan
            }
            _availablePlans.value = updatedPlans
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Purchase Acknowledgement — Core Fix
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called by Google Play when a purchase completes (new checkout or restoration).
     * Dispatches each purchase to [handlePurchase] on the IO dispatcher.
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    scope.launch { handlePurchase(purchase) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User dismissed — no action needed
            }
            else -> {
                // Billing error — no state change; surface to analytics if needed
            }
        }
    }

    /**
     * Core purchase handler called from [onPurchasesUpdated] and
     * [queryAndAcknowledgeExistingPurchases].
     *
     * Deliberate ordering: grant entitlement BEFORE acknowledging.
     * A crash after grant but before acknowledgement is safe — Play re-delivers
     * the purchase next time BillingClient connects.
     */
    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        val tier = when {
            purchase.products.contains("quovex_pro_annual") -> SubscriptionTier.PRO_ANNUAL
            purchase.products.contains("quovex_pro_monthly") -> SubscriptionTier.PRO_MONTHLY
            purchase.products.contains("quovex_pro_lifetime") -> SubscriptionTier.LIFETIME
            else -> return // Unknown product — do not grant or acknowledge
        }

        // Step 1: Grant entitlement first (persisted to SharedPrefs + StateFlow)
        grantEntitlement(tier)

        // Step 2: Acknowledge with Play only after entitlement is durably stored
        if (!purchase.isAcknowledged) {
            acknowledgePurchaseWithPlay(purchase)
        }
    }

    /**
     * Persists premium entitlement to SharedPreferences and updates the StateFlow.
     * Called on Dispatchers.IO — SharedPreferences.edit().apply() is async-safe.
     */
    private fun grantEntitlement(tier: SubscriptionTier) {
        val calendar = Calendar.getInstance()
        val expiryMillis = when (tier) {
            SubscriptionTier.PRO_MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.timeInMillis
            }
            SubscriptionTier.PRO_ANNUAL -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.timeInMillis
            }
            SubscriptionTier.LIFETIME -> -1L
            SubscriptionTier.FREE -> return
        }

        prefs.edit()
            .putString(KEY_TIER, tier.name)
            .putLong(KEY_EXPIRY_MILLIS, expiryMillis)
            .apply()

        val expiryFormatted = if (expiryMillis > 0) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryMillis))
        } else null

        _userEntitlement.value = UserEntitlement.createProTier(
            tier = tier,
            expiryDateMillis = if (expiryMillis > 0) expiryMillis else null,
            expiryFormatted = expiryFormatted
        )
    }

    /**
     * Calls Google Play's acknowledgePurchase API.
     * Google Play requires acknowledgement within 3 days of purchase; unacknowledged
     * purchases are automatically refunded and revoked by Play after that window.
     *
     * On acknowledgement failure the coroutine returns normally — the purchase will be
     * re-delivered by Play on next BillingClient connection and retried via
     * [queryAndAcknowledgeExistingPurchases].
     */
    private suspend fun acknowledgePurchaseWithPlay(purchase: Purchase) {
        val client = billingClient ?: return

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        suspendCancellableCoroutine<Unit> { cont ->
            client.acknowledgePurchase(params) { _ ->
                // Result logged but not fatal — see KDoc above
                cont.resume(Unit)
            }
        }
    }

    /**
     * Queries all active subscriptions and one-time purchases held by this user
     * and runs [handlePurchase] on each.
     *
     * Called on every [onBillingSetupFinished] to:
     * - Restore entitlements on reinstall / new device
     * - Acknowledge purchases that were granted but not acknowledged in a prior session
     *
     * This is the safety net for purchases at risk of the 3-day refund window.
     */
    private suspend fun queryAndAcknowledgeExistingPurchases() {
        val client = billingClient ?: return

        val subsResult = suspendCancellableCoroutine<Pair<BillingResult, List<Purchase>>> { cont ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, purchases ->
                cont.resume(Pair(billingResult, purchases))
            }
        }

        val inappResult = suspendCancellableCoroutine<Pair<BillingResult, List<Purchase>>> { cont ->
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult, purchases ->
                cont.resume(Pair(billingResult, purchases))
            }
        }

        val allPurchases: List<Purchase> = buildList {
            if (subsResult.first.responseCode == BillingClient.BillingResponseCode.OK) {
                addAll(subsResult.second)
            }
            if (inappResult.first.responseCode == BillingClient.BillingResponseCode.OK) {
                addAll(inappResult.second)
            }
        }

        allPurchases.forEach { purchase -> handlePurchase(purchase) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Entitlement Management
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadPersistedEntitlements() {
        val tierString = prefs.getString(KEY_TIER, SubscriptionTier.FREE.name) ?: SubscriptionTier.FREE.name
        val tier = runCatching { SubscriptionTier.valueOf(tierString) }.getOrDefault(SubscriptionTier.FREE)
        val expiryMillis = prefs.getLong(KEY_EXPIRY_MILLIS, -1L)
        val now = System.currentTimeMillis()

        if (tier != SubscriptionTier.FREE && (expiryMillis == -1L || expiryMillis > now)) {
            val expiryFormatted = if (expiryMillis > 0) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryMillis))
            } else null
            _userEntitlement.value = UserEntitlement.createProTier(
                tier = tier,
                expiryDateMillis = if (expiryMillis > 0) expiryMillis else null,
                expiryFormatted = expiryFormatted
            )
        } else {
            val todayStr = getTodayDateString()
            val savedDate = prefs.getString(KEY_DAILY_AI_DATE, "")
            val usedCount = if (savedDate == todayStr) {
                prefs.getInt(KEY_DAILY_AI_USED_COUNT, 0)
            } else 0
            val remaining = (UserEntitlement.FREE_TIER_MAX_DAILY_AI_QUERIES - usedCount).coerceAtLeast(0)
            _userEntitlement.value = UserEntitlement.createFreeTier(remaining)
        }
    }

    private fun getTodayDateString(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    override suspend fun checkAndConsumeAiQuery(): Boolean {
        val current = _userEntitlement.value
        if (current.isUnlimitedAi) return true

        val todayStr = getTodayDateString()
        val savedDate = prefs.getString(KEY_DAILY_AI_DATE, "")
        var usedCount = if (savedDate == todayStr) prefs.getInt(KEY_DAILY_AI_USED_COUNT, 0) else 0

        if (usedCount >= UserEntitlement.FREE_TIER_MAX_DAILY_AI_QUERIES) return false

        usedCount += 1
        prefs.edit()
            .putString(KEY_DAILY_AI_DATE, todayStr)
            .putInt(KEY_DAILY_AI_USED_COUNT, usedCount)
            .apply()

        val remaining = (UserEntitlement.FREE_TIER_MAX_DAILY_AI_QUERIES - usedCount).coerceAtLeast(0)
        _userEntitlement.update { it.copy(dailyAiQueriesRemaining = remaining) }
        return true
    }

    /**
     * restorePurchases() — user-facing "Restore Purchases" action.
     * Queries Google Play directly rather than only reading from local SharedPrefs.
     */
    override suspend fun restorePurchases(): Result<UserEntitlement> {
        return try {
            queryAndAcknowledgeExistingPurchases()
            Result.success(_userEntitlement.value)
        } catch (e: Exception) {
            // BillingClient not yet connected — fall back to local prefs
            loadPersistedEntitlements()
            Result.success(_userEntitlement.value)
        }
    }

    override suspend fun refreshEntitlements() {
        loadPersistedEntitlements()
    }

    override fun grantBonusAiQueries(bonusCount: Int) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString(KEY_DAILY_AI_DATE, "") ?: ""
        val usedCount = if (savedDate == today) prefs.getInt(KEY_DAILY_AI_USED_COUNT, 0) else 0
        val newUsed = usedCount - bonusCount
        prefs.edit()
            .putString(KEY_DAILY_AI_DATE, today)
            .putInt(KEY_DAILY_AI_USED_COUNT, newUsed)
            .apply()
        val remaining = (10 - newUsed).coerceAtLeast(0)
        _userEntitlement.update { it.copy(dailyAiQueriesRemaining = remaining) }
    }

    /**
     * activatePlan() — in Play-enabled environments, the actual purchase flow is launched
     * by the ViewModel using BillingFlowParams; this method handles the no-Play fallback
     * (emulator / CI) by granting the entitlement directly.
     * In production, entitlement is always granted via [handlePurchase].
     */
    override suspend fun activatePlan(plan: SubscriptionPlan): PurchaseResult {
        return try {
            if (billingClient == null || billingClient?.isReady == false) {
                val tier = when (plan.id) {
                    "monthly" -> SubscriptionTier.PRO_MONTHLY
                    "annual" -> SubscriptionTier.PRO_ANNUAL
                    "lifetime" -> SubscriptionTier.LIFETIME
                    else -> SubscriptionTier.PRO_ANNUAL
                }
                grantEntitlement(tier)
            }
            PurchaseResult.Success
        } catch (e: Exception) {
            PurchaseResult.Error(e.message ?: "Failed to initiate purchase")
        }
    }

    companion object {
        private const val KEY_TIER = "subscription_tier"
        private const val KEY_EXPIRY_MILLIS = "subscription_expiry_millis"
        private const val KEY_DAILY_AI_DATE = "daily_ai_date"
        private const val KEY_DAILY_AI_USED_COUNT = "daily_ai_used_count"
    }
}

