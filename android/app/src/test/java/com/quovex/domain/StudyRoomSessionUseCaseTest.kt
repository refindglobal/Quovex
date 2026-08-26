package com.quovex.domain.usecase

import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.RoomChatMessage
import com.quovex.domain.model.RoomMember
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudyRoomSessionUseCaseTest {

    private lateinit var firestoreService: FirebaseFirestoreService
    private lateinit var useCase: StudyRoomSessionUseCase

    private val testRoomId = "room_jee_physics"
    private val testMember = RoomMember(
        userId = "user_123",
        userName = "Alice",
        avatarId = 2,
        scholarRank = "Scholar I",
        currentSessionMinutes = 45,
        isStudying = true,
        focusScore = 87
    )

    @Before
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        useCase = StudyRoomSessionUseCase(firestoreService)
    }

    /** joinRoom calls firestoreService.joinStudyRoom with correct params and returns its result. */
    @Test
    fun `joinRoom delegates to firestoreService and returns success`() = runTest {
        coEvery { firestoreService.joinStudyRoom(testRoomId, testMember) } returns Result.success(Unit)

        val result = useCase.joinRoom(testRoomId, testMember)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { firestoreService.joinStudyRoom(testRoomId, testMember) }
    }

    /** joinRoom propagates a failure Result when the service throws. */
    @Test
    fun `joinRoom returns failure when firestoreService fails`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { firestoreService.joinStudyRoom(testRoomId, testMember) } returns Result.failure(error)

        val result = useCase.joinRoom(testRoomId, testMember)

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    /** leaveRoom calls firestoreService with the correct userId. */
    @Test
    fun `leaveRoom delegates to firestoreService with correct userId`() = runTest {
        coEvery { firestoreService.leaveStudyRoom(testRoomId, "user_123") } returns Result.success(Unit)

        val result = useCase.leaveRoom(testRoomId, "user_123")

        assertTrue(result.isSuccess)
        coVerify { firestoreService.leaveStudyRoom(testRoomId, "user_123") }
    }

    /** observeMembers wraps the Firestore flow and emits the same member list. */
    @Test
    fun `observeMembers emits the members from firestoreService`() = runTest {
        val members = listOf(testMember)
        every { firestoreService.getRoomMembersFlow(testRoomId) } returns flowOf(members)

        val emitted = mutableListOf<List<RoomMember>>()
        useCase.observeMembers(testRoomId).collect { emitted.add(it) }

        assertEquals(1, emitted.size)
        assertEquals("Alice", emitted[0][0].userName)
    }

    /** observeChat wraps the Firestore flow and emits the same message list. */
    @Test
    fun `observeChat emits the messages from firestoreService`() = runTest {
        val messages = listOf(
            RoomChatMessage(
                messageId = "m1", roomId = testRoomId, senderId = "user_123",
                senderName = "Alice", messageText = "Let's study!",
                timestampMillis = System.currentTimeMillis()
            )
        )
        every { firestoreService.getRoomChatFlow(testRoomId) } returns flowOf(messages)

        val emitted = mutableListOf<List<RoomChatMessage>>()
        useCase.observeChat(testRoomId).collect { emitted.add(it) }

        assertEquals(1, emitted.size)
        assertEquals("Let's study!", emitted[0][0].messageText)
    }

    /** sendMessage delegates to firestoreService and returns its result. */
    @Test
    fun `sendMessage delegates to firestoreService`() = runTest {
        val message = RoomChatMessage(
            roomId = testRoomId, senderId = "user_123",
            senderName = "Alice", messageText = "Quick break!"
        )
        coEvery { firestoreService.sendRoomChatMessage(testRoomId, message) } returns Result.success(Unit)

        val result = useCase.sendMessage(testRoomId, message)

        assertTrue(result.isSuccess)
        coVerify { firestoreService.sendRoomChatMessage(testRoomId, message) }
    }
}
