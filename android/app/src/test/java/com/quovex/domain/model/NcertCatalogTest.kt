package com.quovex.domain.model

import com.google.gson.Gson
import com.quovex.data.remote.dto.NcertCatalogResponseDto
import com.quovex.data.remote.dto.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NcertCatalogTest {

    private val gson = Gson()

    @Test
    fun testCatalogJsonStructureAndMetadataOnly() {
        val catalogFile = File("src/main/assets/ncert/ncert_catalog_v1.json")
        assertTrue("Catalog file must exist in assets", catalogFile.exists())

        val jsonContent = catalogFile.readText()
        val dto = gson.fromJson(jsonContent, NcertCatalogResponseDto::class.java)
        assertNotNull(dto)

        val catalog = dto.toDomain()
        assertTrue("Catalog should contain books for Classes 9-12", catalog.books.isNotEmpty())
        assertTrue("Catalog should contain chapters", catalog.chapters.isNotEmpty())

        // 1. Verify Classes 9, 10, 11, 12 are present
        val classLevels = catalog.books.map { it.classLevel }.distinct()
        assertTrue(classLevels.contains(9))
        assertTrue(classLevels.contains(10))
        assertTrue(classLevels.contains(11))
        assertTrue(classLevels.contains(12))

        // 2. Verify Metadata-only compliance: every chapter must have official NCERT URL and OFFICIAL_RESOURCE content type
        catalog.chapters.forEach { chapter ->
            assertEquals(NcertContentType.OFFICIAL_RESOURCE, chapter.contentType)
            assertEquals("NCERT", chapter.publisher)
            assertTrue("Official URL must point to NCERT official portal", chapter.officialSourceUrl.startsWith("https://ncert.nic.in/"))
            assertTrue("Chapter must have valid chapter number", chapter.chapterNumber > 0)
            assertTrue("Chapter must have title", chapter.chapterTitle.isNotBlank())
            assertTrue("Chapter must have book title", chapter.bookTitle.isNotBlank())
            assertNotNull(chapter.lastVerifiedAt)
            assertEquals(NcertVerificationStatus.VERIFIED, chapter.verificationStatus)
        }

        // 3. Verify Books metadata
        catalog.books.forEach { book ->
            assertEquals(NcertContentType.OFFICIAL_RESOURCE, book.contentType)
            assertEquals("NCERT", book.publisher)
            assertTrue("Book code must be specified", book.bookCode.isNotBlank())
            assertTrue("Book must have chapter count", book.totalChapters > 0)
        }
    }

    @Test
    fun testClassAndSubjectHierarchy() {
        val catalogFile = File("src/main/assets/ncert/ncert_catalog_v1.json")
        val dto = gson.fromJson(catalogFile.readText(), NcertCatalogResponseDto::class.java)
        val catalog = dto.toDomain()

        // Class 12 must have Physics, Chemistry, Mathematics, Biology
        val class12Subjects = catalog.books.filter { it.classLevel == 12 }.map { it.subject }.distinct()
        assertTrue(class12Subjects.contains("Physics"))
        assertTrue(class12Subjects.contains("Chemistry"))
        assertTrue(class12Subjects.contains("Mathematics"))
        assertTrue(class12Subjects.contains("Biology"))

        // Class 10 must have Science and Mathematics
        val class10Subjects = catalog.books.filter { it.classLevel == 10 }.map { it.subject }.distinct()
        assertTrue(class10Subjects.contains("Science"))
        assertTrue(class10Subjects.contains("Mathematics"))
    }
}
