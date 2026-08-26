package com.quovex.domain.usecase

import com.quovex.domain.model.PurchaseResult
import com.quovex.domain.model.SubscriptionPlan
import com.quovex.domain.repository.BillingRepository
import javax.inject.Inject

class PurchaseSubscriptionUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(plan: SubscriptionPlan): PurchaseResult {
        return billingRepository.activatePlan(plan)
    }
}
