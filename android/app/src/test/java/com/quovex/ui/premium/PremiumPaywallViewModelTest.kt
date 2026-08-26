package com.quovex.ui.premium

import com.quovex.domain.model.DefaultSubscriptionPlans
import com.quovex.domain.model.PurchaseResult
import com.quovex.domain.model.SubscriptionTier
import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.repository.BillingRepository
import com.quovex.domain.usecase.GetSubscriptionPlansUseCase
import com.quovex.domain.usecase.ObserveUserEntitlementUseCase
import com.quovex.domain.usecase.PurchaseSubscriptionUseCase
import com.quovex.domain.usecase.RestorePurchasesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PremiumPaywallViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var billingRepository: BillingRepository
    private lateinit var observeUserEntitlementUseCase: ObserveUserEntitlementUseCase
    private lateinit var getSubscriptionPlansUseCase: GetSubscriptionPlansUseCase
    private lateinit var purchaseSubscriptionUseCase: PurchaseSubscriptionUseCase
    private lateinit var restorePurchasesUseCase: RestorePurchasesUseCase

    private val entitlementFlow = MutableStateFlow(UserEntitlement.createFreeTier())
    private val plansFlow = MutableStateFlow(DefaultSubscriptionPlans.ALL)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        billingRepository = mockk(relaxed = true)

        every { billingRepository.userEntitlement } returns entitlementFlow
        every { billingRepository.availablePlans } returns plansFlow

        observeUserEntitlementUseCase = ObserveUserEntitlementUseCase(billingRepository)
        getSubscriptionPlansUseCase = GetSubscriptionPlansUseCase(billingRepository)
        purchaseSubscriptionUseCase = PurchaseSubscriptionUseCase(billingRepository)
        restorePurchasesUseCase = RestorePurchasesUseCase(billingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): PremiumPaywallViewModel {
        return PremiumPaywallViewModel(
            observeUserEntitlementUseCase = observeUserEntitlementUseCase,
            getSubscriptionPlansUseCase = getSubscriptionPlansUseCase,
            purchaseSubscriptionUseCase = purchaseSubscriptionUseCase,
            restorePurchasesUseCase = restorePurchasesUseCase
        )
    }

    @Test
    fun `initial state has annual plan selected and free entitlement`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(DefaultSubscriptionPlans.ANNUAL.id, vm.uiState.value.selectedPlan.id)
        assertFalse(vm.uiState.value.entitlement.isPremiumActive)
        assertEquals(3, vm.uiState.value.plans.size)
    }

    @Test
    fun `selectPlan updates selected plan in state`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectPlan(DefaultSubscriptionPlans.MONTHLY)
        assertEquals(DefaultSubscriptionPlans.MONTHLY.id, vm.uiState.value.selectedPlan.id)

        vm.selectPlan(DefaultSubscriptionPlans.LIFETIME)
        assertEquals(DefaultSubscriptionPlans.LIFETIME.id, vm.uiState.value.selectedPlan.id)
    }

    @Test
    fun `purchaseSelectedPlan invokes usecase and sets success message`() = runTest {
        coEvery { billingRepository.activatePlan(any()) } returns PurchaseResult.Success

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.purchaseSelectedPlan()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isPurchasing)
        assertNotNull(vm.uiState.value.successMessage)
        coVerify { billingRepository.activatePlan(DefaultSubscriptionPlans.ANNUAL) }
    }

    @Test
    fun `purchaseSelectedPlan sets error message on failure`() = runTest {
        coEvery { billingRepository.activatePlan(any()) } returns PurchaseResult.Error("Network failure")

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.purchaseSelectedPlan()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isPurchasing)
        assertEquals("Network failure", vm.uiState.value.errorMessage)
    }

    @Test
    fun `restorePurchases invokes usecase and updates state`() = runTest {
        coEvery { billingRepository.restorePurchases() } returns Result.success(
            UserEntitlement.createProTier(SubscriptionTier.PRO_ANNUAL)
        )

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.restorePurchases()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isRestoring)
        assertNotNull(vm.uiState.value.successMessage)
        coVerify { billingRepository.restorePurchases() }
    }
}
