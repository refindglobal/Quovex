package com.quovex.domain.usecase

import com.quovex.domain.model.DeckItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetConfiguredSubjectsUseCaseTest {

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var useCase: GetConfiguredSubjectsUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeQuovexRepository()
        useCase = GetConfiguredSubjectsUseCase(fakeRepository)
    }

    @Test
    fun `returns subjects from user decks when decks exist`() = runTest {
        fakeRepository.decksList = listOf(
            DeckItem(1, "Thermodynamics", "Physics", 20),
            DeckItem(2, "Organic Chemistry", "Chemistry", 15),
            DeckItem(3, "Electrostatics", "Physics", 10) // duplicate subject
        )

        val subjects = useCase().first()

        assertEquals(2, subjects.size)
        assertTrue(subjects.contains("Physics"))
        assertTrue(subjects.contains("Chemistry"))
    }

    @Test
    fun `returns default academic catalog when user has no decks`() = runTest {
        fakeRepository.decksList = emptyList()

        val subjects = useCase().first()

        assertEquals(GetConfiguredSubjectsUseCase.defaultSubjectCatalog, subjects)
        assertTrue(subjects.contains("Physics"))
        assertTrue(subjects.contains("Mathematics"))
    }
}
