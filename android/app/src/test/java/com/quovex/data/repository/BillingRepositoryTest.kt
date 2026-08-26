package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.quovex.domain.model.DefaultSubscriptionPlans
import com.quovex.domain.model.PurchaseResult
import com.quovex.domain.model.SubscriptionTier
import com.quovex.domain.model.UserEntitlement
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillingRepositoryTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val memoryStorage = mutableMapOf<String, Any>()

    private lateinit var repository: BillingRepositoryImpl

    @Before
    fun setUp() {
        memoryStorage.clear()
        context = mockk(relaxed = true)
        prefs = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { context.getSharedPreferences("quovex_billing_prefs", Context.MODE_PRIVATE) } returns prefs
        every { prefs.edit() } returns editor

        every { prefs.getString("subscription_tier", any()) } answers {
            (memoryStorage["subscription_tier"] as? String) ?: SubscriptionTier.FREE.name
        }
        every { prefs.getLong("subscription_expiry_millis", any()) } answers {
            (memoryStorage["subscription_expiry_millis"] as? Long) ?: -1L
        }
        every { prefs.getString("daily_ai_date", any()) } answers {
            (memoryStorage["daily_ai_date"] as? String) ?: ""
        }
        every { prefs.getInt("daily_ai_used_count", any()) } answers {
            (memoryStorage["daily_ai_used_count"] as? Int) ?: 0
        }

        every { editor.putString(any(), any()) } answers {
            memoryStorage[firstArg()] = secondArg<String>()
            editor
        }
        every { editor.putLong(any(), any()) } answers {
            memoryStorage[firstArg()] = secondArg<Long>()
            editor
        }
        every { editor.putInt(any(), any()) } answers {
            memoryStorage[firstArg()] = secondArg<Int>()
            editor
        }
        every { editor.apply() } returns Unit

        repository = BillingRepositoryImpl(context)
    }

    @Test
    fun `initial state defaults to Free tier with 10 remaining AI queries`() {
        val entitlement = repository.userEntitlement.value
        assertFalse(entitlement.isPremiumActive)
        assertEquals(SubscriptionTier.FREE, entitlement.tier)
        assertEquals(10, entitlement.dailyAiQueriesRemaining)
        assertEquals(10, entitlement.dailyAiQueriesMax)
        assertFalse(entitlement.isUnlimitedAi)
    }

    @Test
    fun `checkAndConsumeAiQuery decrements daily quota on Free tier`() = runTest {
        val permitted = repository.checkAndConsumeAiQuery()
        assertTrue(permitted)

        val entitlement = repository.userEntitlement.value
        assertEquals(9, entitlement.dailyAiQueriesRemaining)
    }

    @Test
    fun `checkAndConsumeAiQuery rejects when 10 queries are exhausted on Free tier`() = runTest {
        repeat(10) {
            val result = repository.checkAndConsumeAiQuery()
            assertTrue(result)
        }

        assertEquals(0, repository.userEntitlement.value.dailyAiQueriesRemaining)

        // 11th query should be blocked
        val result11 = repository.checkAndConsumeAiQuery()
        assertFalse(result11)
        assertEquals(0, repository.userEntitlement.value.dailyAiQueriesRemaining)
    }

    @Test
    fun `activatePlan unlocks Pro tier with unlimited AI queries`() = runTest {
        val result = repository.activatePlan(DefaultSubscriptionPlans.ANNUAL)
        assertTrue(result is PurchaseResult.Success)

        val entitlement = repository.userEntitlement.value
        assertTrue(entitlement.isPremiumActive)
        assertEquals(SubscriptionTier.PRO_ANNUAL, entitlement.tier)
        assertTrue(entitlement.isUnlimitedAi)
        assertTrue(entitlement.isAdFree)
        assertTrue(entitlement.isUnlimitedPdfScanning)

        // Unlimited queries never blocked
        repeat(20) {
            assertTrue(repository.checkAndConsumeAiQuery())
        }
    }

    @Test
    fun `restorePurchases reloads active entitlement`() = runTest {
        repository.activatePlan(DefaultSubscriptionPlans.LIFETIME)
        val restoreResult = repository.restorePurchases()

        assertTrue(restoreResult.isSuccess)
        val entitlement = restoreResult.getOrNull()
        assertTrue(entitlement?.isPremiumActive == true)
        assertEquals(SubscriptionTier.LIFETIME, entitlement?.tier)
    }
}
