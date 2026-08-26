package com.quovex.domain.usecase

import com.quovex.domain.model.AppCategory
import com.quovex.domain.repository.DistractionBlockerRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ToggleBlockedAppUseCaseTest {

    private lateinit var repository: DistractionBlockerRepository
    private lateinit var useCase: ToggleBlockedAppUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = ToggleBlockedAppUseCase(repository)
    }

    @Test
    fun `toggleApp delegates to repository`() = runTest {
        coEvery { repository.toggleAppBlocked("com.instagram.android", true) } returns Unit

        useCase.toggleApp("com.instagram.android", true)
        coVerify { repository.toggleAppBlocked("com.instagram.android", true) }
    }

    @Test
    fun `setCategoryBlocked delegates to repository`() = runTest {
        coEvery { repository.setCategoryBlocked(AppCategory.GAMING, false) } returns Unit

        useCase.setCategoryBlocked(AppCategory.GAMING, false)
        coVerify { repository.setCategoryBlocked(AppCategory.GAMING, false) }
    }

    @Test
    fun `setShieldEnabled delegates to repository`() = runTest {
        coEvery { repository.setShieldEnabled(false) } returns Unit

        useCase.setShieldEnabled(false)
        coVerify { repository.setShieldEnabled(false) }
    }
}
