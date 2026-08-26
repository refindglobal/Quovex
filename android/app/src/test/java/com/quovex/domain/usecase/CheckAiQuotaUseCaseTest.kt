package com.quovex.domain.usecase

import com.quovex.domain.repository.BillingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckAiQuotaUseCaseTest {

    private lateinit var billingRepository: BillingRepository
    private lateinit var useCase: CheckAiQuotaUseCase

    @Before
    fun setUp() {
        billingRepository = mockk(relaxed = true)
        useCase = CheckAiQuotaUseCase(billingRepository)
    }

    @Test
    fun `invoke returns true when repository allows AI query`() = runTest {
        coEvery { billingRepository.checkAndConsumeAiQuery() } returns true

        val allowed = useCase()
        assertTrue(allowed)
        coVerify { billingRepository.checkAndConsumeAiQuery() }
    }

    @Test
    fun `invoke returns false when daily free quota is exhausted`() = runTest {
        coEvery { billingRepository.checkAndConsumeAiQuery() } returns false

        val allowed = useCase()
        assertFalse(allowed)
        coVerify { billingRepository.checkAndConsumeAiQuery() }
    }
}
