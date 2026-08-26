package com.quovex.domain.usecase

import com.quovex.domain.repository.BillingRepository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class GrantBonusAiQueriesUseCaseTest {

    @Test
    fun testGrantBonusAiQueriesInvokesRepository() {
        val billingRepository = mockk<BillingRepository>(relaxed = true)
        val useCase = GrantBonusAiQueriesUseCase(billingRepository)

        useCase(5)

        verify(exactly = 1) { billingRepository.grantBonusAiQueries(5) }
    }

    @Test
    fun testGrantBonusAiQueriesDefaultAmount() {
        val billingRepository = mockk<BillingRepository>(relaxed = true)
        val useCase = GrantBonusAiQueriesUseCase(billingRepository)

        useCase()

        verify(exactly = 1) { billingRepository.grantBonusAiQueries(5) }
    }
}
