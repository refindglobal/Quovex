package com.quovex.domain.model

/**
 * Subscription plan tiers supported by Quovex.
 */
enum class SubscriptionTier(val title: String) {
    FREE("Scholar Free"),
    PRO_MONTHLY("Quovex Pro Monthly"),
    PRO_ANNUAL("Quovex Pro Annual"),
    LIFETIME("Founder Lifetime")
}

/**
 * Catalog item for an available in-app subscription or one-time purchase plan.
 */
data class SubscriptionPlan(
    val id: String,
    val productId: String,
    val title: String,
    val subtitle: String,
    val formattedPrice: String,
    val periodDescription: String,
    val badge: String? = null,
    val isBestValue: Boolean = false,
    val isPopular: Boolean = false,
    val savingsPercentage: Int = 0,
    val features: List<String> = emptyList()
)

/**
 * The user's active entitlement and AI quota status.
 */
data class UserEntitlement(
    val isPremiumActive: Boolean = false,
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val dailyAiQueriesRemaining: Int = 10,
    val dailyAiQueriesMax: Int = 10,
    val isUnlimitedAi: Boolean = false,
    val isUnlimitedPdfScanning: Boolean = false,
    val isAdFree: Boolean = false,
    val isAdvancedAnalyticsUnlocked: Boolean = false,
    val expiryDateMillis: Long? = null,
    val expiryDateFormatted: String? = null,
    val autoRenewing: Boolean = false
) {
    companion object {
        const val FREE_TIER_MAX_DAILY_AI_QUERIES = 10
        const val UNLIMITED_QUOTA = 999999

        fun createFreeTier(remainingQueries: Int = FREE_TIER_MAX_DAILY_AI_QUERIES) = UserEntitlement(
            isPremiumActive = false,
            tier = SubscriptionTier.FREE,
            dailyAiQueriesRemaining = remainingQueries.coerceIn(0, FREE_TIER_MAX_DAILY_AI_QUERIES),
            dailyAiQueriesMax = FREE_TIER_MAX_DAILY_AI_QUERIES,
            isUnlimitedAi = false,
            isUnlimitedPdfScanning = false,
            isAdFree = false,
            isAdvancedAnalyticsUnlocked = false
        )

        fun createProTier(
            tier: SubscriptionTier = SubscriptionTier.PRO_ANNUAL,
            expiryDateMillis: Long? = null,
            expiryFormatted: String? = null
        ) = UserEntitlement(
            isPremiumActive = true,
            tier = tier,
            dailyAiQueriesRemaining = UNLIMITED_QUOTA,
            dailyAiQueriesMax = UNLIMITED_QUOTA,
            isUnlimitedAi = true,
            isUnlimitedPdfScanning = true,
            isAdFree = true,
            isAdvancedAnalyticsUnlocked = true,
            expiryDateMillis = expiryDateMillis,
            expiryDateFormatted = expiryFormatted,
            autoRenewing = tier != SubscriptionTier.LIFETIME
        )
    }
}

/**
 * Result of a Google Play Billing checkout flow.
 */
sealed class PurchaseResult {
    object Success : PurchaseResult()
    object UserCanceled : PurchaseResult()
    object Pending : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

/**
 * Catalog of built-in standard subscription plans.
 */
object DefaultSubscriptionPlans {
    val MONTHLY = SubscriptionPlan(
        id = "monthly",
        productId = "quovex_pro_monthly",
        title = "Pro Monthly",
        subtitle = "Flexible month-to-month revision",
        formattedPrice = "₹199",
        periodDescription = "/ month",
        badge = "LAUNCH OFFER • ₹99 1st MONTH",
        savingsPercentage = 0,
        features = listOf(
            "Unlimited Groq & Cerebras AI Tutoring",
            "Unlimited NCERT PDF Multi-Page Scanning",
            "All 9 Binaural Beats & Ambient Soundscapes",
            "AI Camera Focus & Drowsiness Tracking",
            "100% Ad-Free Experience"
        )
    )

    val ANNUAL = SubscriptionPlan(
        id = "annual",
        productId = "quovex_pro_annual",
        title = "Pro Annual",
        subtitle = "Full 1-Year Exam Season Access",
        formattedPrice = "₹999",
        periodDescription = "/ year (₹83/mo)",
        badge = "⭐ 7-DAY FREE TRIAL • SAVE 60%",
        isBestValue = true,
        isPopular = true,
        savingsPercentage = 60,
        features = listOf(
            "7-Day Free Trial (Zero Upfront Charge)",
            "Unlimited 24/7 AI Tutoring & Study Plans",
            "Advanced AI Exam Mistake Analysis Heatmaps",
            "Unlimited PDF Scanning & OCR Text Extraction",
            "All 9 Ambient Soundscapes & Camera Focus",
            "Priority AI Inference Speed & Support",
            "100% Ad-Free Clean Deep Work"
        )
    )

    val LIFETIME = SubscriptionPlan(
        id = "lifetime",
        productId = "quovex_pro_lifetime",
        title = "Founder Lifetime",
        subtitle = "One-time payment, permanent VIP",
        formattedPrice = "₹2,499",
        periodDescription = "one-time payment",
        badge = "🚀 FOUNDER PASS • PERMANENT ACCESS",
        savingsPercentage = 80,
        features = listOf(
            "Pay Once, Own Quovex Pro Forever",
            "All Future AI Models & Study Tools Included",
            "Exclusive Founder Discord VIP Role & Badge",
            "Permanent Ad-Free & Unlimited Quotas",
            "Direct Roadmap Voting & Early Beta Access"
        )
    )

    val ALL = listOf(ANNUAL, MONTHLY, LIFETIME)
}
