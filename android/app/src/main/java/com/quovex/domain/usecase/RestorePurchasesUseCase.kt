package com.quovex.domain.usecase

import com.quovex.domain.model.UserEntitlement
import com.quovex.domain.repository.BillingRepository
import javax.inject.Inject

class RestorePurchasesUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(): Result<UserEntitlement> {
        return billingRepository.restorePurchases()
    }
}
