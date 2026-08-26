package com.quovex.domain.usecase

import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.repository.BillingRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveUserEntitlementUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(): StateFlow<UserEntitlement> = billingRepository.userEntitlement
}
