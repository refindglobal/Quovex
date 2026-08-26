package com.quovex.domain.usecase

import com.quovex.domain.model.SubscriptionPlan
import com.quovex.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetSubscriptionPlansUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(): StateFlow<List<SubscriptionPlan>> = billingRepository.availablePlans
}
