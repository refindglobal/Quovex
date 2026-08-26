package com.quovex.domain.usecase

import com.quovex.domain.repository.BillingRepository
import javax.inject.Inject

class GrantBonusAiQueriesUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(bonusCount: Int = 5) {
        billingRepository.grantBonusAiQueries(bonusCount)
    }
}
