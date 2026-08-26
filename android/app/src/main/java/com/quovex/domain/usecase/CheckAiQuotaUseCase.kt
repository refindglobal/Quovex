package com.quovex.domain.usecase

import com.quovex.domain.repository.BillingRepository
import javax.inject.Inject

class CheckAiQuotaUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    /**
     * Checks if the user is authorized to perform an AI query and consumes 1 credit if on free tier.
     * @return True if permitted, False if free daily limit (10 queries/day) is exhausted.
     */
    suspend operator fun invoke(): Boolean {
        return billingRepository.checkAndConsumeAiQuery()
    }
}
