package com.quovex.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubjectCatalogTest {

    @Test
    fun `catalog contains all major streams`() {
        val categories = SubjectCatalog.ALL.map { it.category }.distinct()
        assertTrue(categories.contains(SubjectCategory.SCIENCE))
        assertTrue(categories.contains(SubjectCategory.COMMERCE))
        assertTrue(categories.contains(SubjectCategory.HUMANITIES))
        assertTrue(categories.contains(SubjectCategory.LANGUAGES))
        assertTrue(categories.contains(SubjectCategory.MATHEMATICS))
    }

    @Test
    fun `science stream contains core subjects`() {
        val scienceSubjects = SubjectCatalog.byCategory(SubjectCategory.SCIENCE).map { it.subjectName }
        assertTrue(scienceSubjects.contains("Physics"))
        assertTrue(scienceSubjects.contains("Chemistry"))
        assertTrue(scienceSubjects.contains("Biology"))
    }

    @Test
    fun `commerce stream contains core subjects`() {
        val commerceSubjects = SubjectCatalog.byCategory(SubjectCategory.COMMERCE).map { it.subjectName }
        assertTrue(commerceSubjects.contains("Accountancy"))
        assertTrue(commerceSubjects.contains("Business Studies"))
        assertTrue(commerceSubjects.contains("Economics"))
    }

    @Test
    fun `humanities stream contains core subjects`() {
        val humanitiesSubjects = SubjectCatalog.byCategory(SubjectCategory.HUMANITIES).map { it.subjectName }
        assertTrue(humanitiesSubjects.contains("History"))
        assertTrue(humanitiesSubjects.contains("Political Science"))
        assertTrue(humanitiesSubjects.contains("Geography"))
        assertTrue(humanitiesSubjects.contains("Sociology"))
        assertTrue(humanitiesSubjects.contains("Psychology"))
    }

    @Test
    fun `chatSelectorNames is non-empty and universal`() {
        val names = SubjectCatalog.chatSelectorNames
        assertTrue(names.isNotEmpty())
        assertTrue(names.contains("Mathematics"))
        assertTrue(names.contains("Physics"))
        assertTrue(names.contains("Accountancy"))
        assertTrue(names.contains("History"))
        assertTrue(names.contains("English"))
    }

    @Test
    fun `findByName is case-insensitive`() {
        val physics = SubjectCatalog.findByName("physics")
        assertNotNull(physics)
        assertEquals("Physics", physics?.subjectName)
        assertEquals(SubjectCategory.SCIENCE, physics?.category)

        val accountancy = SubjectCatalog.findByName("ACCOUNTANCY")
        assertNotNull(accountancy)
        assertEquals("Accountancy", accountancy?.subjectName)
        assertEquals(SubjectCategory.COMMERCE, accountancy?.category)
    }
}
