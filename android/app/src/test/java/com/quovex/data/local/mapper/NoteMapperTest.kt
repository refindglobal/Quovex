package com.quovex.data.local.mapper

import com.quovex.data.local.entity.NoteEntity
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteMapperTest {

    @Test
    fun `toDomain maps entity to domain correctly with json keypoints`() {
        val entity = NoteEntity(
            id = 42L,
            cloudId = "cloud-123",
            title = "Kinematics Summary",
            subject = "Physics",
            content = "Equations of motion under uniform acceleration.",
            status = "READY",
            inputType = "SCAN",
            sourceUrl = null,
            storageRef = "notes/user1/42/scan.jpg",
            keyPointsJson = """["v = u + at", "s = ut + 0.5at^2", "v^2 = u^2 + 2as"]""",
            flashcardCount = 3,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val domain = entity.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("cloud-123", domain.cloudId)
        assertEquals("Kinematics Summary", domain.title)
        assertEquals("Physics", domain.subject)
        assertEquals("Equations of motion under uniform acceleration.", domain.content)
        assertEquals(NoteProcessingStatus.READY, domain.status)
        assertEquals(NoteInputType.SCAN, domain.inputType)
        assertEquals("notes/user1/42/scan.jpg", domain.storageRef)
        assertEquals(3, domain.keyPoints.size)
        assertEquals("v = u + at", domain.keyPoints[0])
        assertEquals(3, domain.flashcardCount)
        assertEquals(1000L, domain.createdAt)
        assertEquals(2000L, domain.updatedAt)
    }

    @Test
    fun `toEntity maps domain to entity correctly with json serialization`() {
        val domain = NoteItem(
            id = 10L,
            cloudId = "c-999",
            title = "Organic Reactions",
            subject = "Chemistry",
            content = "Aldol condensation mechanism.",
            status = NoteProcessingStatus.PROCESSING,
            inputType = NoteInputType.URL,
            sourceUrl = "https://example.com/chemistry",
            storageRef = null,
            keyPoints = listOf("Enolate formation", "Nucleophilic addition"),
            flashcardCount = 2,
            createdAt = 5000L,
            updatedAt = 6000L
        )

        val entity = domain.toEntity()

        assertEquals(10L, entity.id)
        assertEquals("c-999", entity.cloudId)
        assertEquals("Organic Reactions", entity.title)
        assertEquals("Chemistry", entity.subject)
        assertEquals("PROCESSING", entity.status)
        assertEquals("URL", entity.inputType)
        assertEquals("https://example.com/chemistry", entity.sourceUrl)
        assertNotNull(entity.keyPointsJson)
        assertTrue(entity.keyPointsJson!!.contains("Enolate formation"))
        assertEquals(2, entity.flashcardCount)
    }

    @Test
    fun `toDomain gracefully handles corrupt json or invalid enum values`() {
        val corruptEntity = NoteEntity(
            id = 1L,
            title = "Fallback Test",
            subject = "Maths",
            content = "Content",
            status = "UNKNOWN_STATUS",
            inputType = "UNKNOWN_TYPE",
            keyPointsJson = "invalid-json-{"
        )

        val domain = corruptEntity.toDomain()

        assertEquals(NoteProcessingStatus.READY, domain.status)
        assertEquals(NoteInputType.TEXT, domain.inputType)
        assertTrue(domain.keyPoints.isEmpty())
    }
}
