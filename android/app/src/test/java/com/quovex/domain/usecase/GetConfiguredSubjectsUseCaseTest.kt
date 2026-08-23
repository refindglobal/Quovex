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

        assertEquals("Physics", subjects[0])
        assertEquals("Chemistry", subjects[1])
        assertTrue(subjects.contains("Physics"))
        assertTrue(subjects.contains("Chemistry"))
        assertTrue(subjects.size >= 2)
    }

    @Test
    fun `returns default academic catalog when user has no decks`() = runTest {
        fakeRepository.decksList = emptyList()

        val subjects = useCase().first()

        assertEquals(com.quovex.domain.model.SubjectCatalog.chatSelectorNames, subjects)
        assertTrue(subjects.contains("Physics"))
        assertTrue(subjects.contains("Mathematics"))
        assertTrue(subjects.contains("Accountancy"))
        assertTrue(subjects.contains("History"))
    }
}
