package com.quovex.ui.community

import androidx.lifecycle.SavedStateHandle
import com.quovex.domain.model.RoomChatMessage
import com.quovex.domain.model.RoomMember
import com.quovex.domain.usecase.StudyRoomSessionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyRoomLiveViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var studyRoomSessionUseCase: StudyRoomSessionUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var authService: com.quovex.data.remote.FirebaseAuthService
    private lateinit var userPreferencesManager: com.quovex.data.local.UserPreferencesManager
    private lateinit var viewModel: StudyRoomLiveViewModel

    private val testRoomId = "room_jee_advanced"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studyRoomSessionUseCase = mockk(relaxed = true)
        savedStateHandle = SavedStateHandle(mapOf("roomId" to testRoomId))
        authService = mockk(relaxed = true)
        userPreferencesManager = mockk(relaxed = true)

        every { authService.currentUserId } returns "user_test"
        every { userPreferencesManager.userProfile } returns kotlinx.coroutines.flow.MutableStateFlow(
            com.quovex.domain.model.UserProfile(
                id = "user_test",
                name = "Aspirant",
                avatarId = 1,
                targetExam = "JEE Advanced",
                dailyGoalHours = 4.0f,
                streakDays = 1,
                xp = 600,
                level = 2,
                isOnboarded = true,
                email = "test@quovex.app",
                rescueTokens = 1
            )
        )

        // Default stubs
        coEvery { studyRoomSessionUseCase.joinRoom(any(), any()) } returns Result.success(Unit)
        every { studyRoomSessionUseCase.observeMembers(any()) } returns flowOf(emptyList())
        every { studyRoomSessionUseCase.observeChat(any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): StudyRoomLiveViewModel =
        StudyRoomLiveViewModel(
            savedStateHandle = savedStateHandle,
            studyRoomSessionUseCase = studyRoomSessionUseCase,
            authService = authService,
            userPreferencesManager = userPreferencesManager
        )

    /** ViewModel initialises with the roomId from SavedStateHandle. */
    @Test
    fun `roomId is read from SavedStateHandle`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(testRoomId, viewModel.uiState.value.roomId)
    }

    /** isJoined becomes true when joinRoom succeeds. */
    @Test
    fun `isJoined is true after successful joinRoom`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isJoined)
    }

    /** isJoined is false when joinRoom fails. */
    @Test
    fun `isJoined is false when joinRoom fails`() = runTest {
        coEvery { studyRoomSessionUseCase.joinRoom(any(), any()) } returns Result.failure(RuntimeException("Failed"))
        viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isJoined)
    }

    /** Members from the flow appear in state. */
    @Test
    fun `observeMembers updates members in state`() = runTest {
        val members = listOf(
            RoomMember(userId = "u1", userName = "Bob", avatarId = 2, isStudying = true)
        )
        every { studyRoomSessionUseCase.observeMembers(testRoomId) } returns flowOf(members)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.members.size)
        assertEquals("Bob", viewModel.uiState.value.members[0].userName)
    }

    /** Chat messages from the flow appear in state. */
    @Test
    fun `observeChat updates chatMessages in state`() = runTest {
        val messages = listOf(
            RoomChatMessage(messageId = "m1", senderId = "u1", senderName = "Bob", messageText = "Hey!")
        )
        every { studyRoomSessionUseCase.observeChat(testRoomId) } returns flowOf(messages)
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.chatMessages.size)
        assertEquals("Hey!", viewModel.uiState.value.chatMessages[0].messageText)
    }

    /** onChatInputChange updates chatInput in state. */
    @Test
    fun `onChatInputChange updates chatInput`() = runTest {
        viewModel = createViewModel()
        viewModel.onChatInputChange("Let's do this!")
        assertEquals("Let's do this!", viewModel.uiState.value.chatInput)
    }

    /** toggleBreakChat flips isBreakChatOpen. */
    @Test
    fun `toggleBreakChat flips chat panel visibility`() = runTest {
        viewModel = createViewModel()
        assertFalse(viewModel.uiState.value.isBreakChatOpen)
        viewModel.toggleBreakChat()
        assertTrue(viewModel.uiState.value.isBreakChatOpen)
        viewModel.toggleBreakChat()
        assertFalse(viewModel.uiState.value.isBreakChatOpen)
    }

    /** sendMessage calls use case and clears chatInput on completion. */
    @Test
    fun `sendMessage sends and clears chatInput`() = runTest {
        coEvery { studyRoomSessionUseCase.sendMessage(any(), any()) } returns Result.success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onChatInputChange("Great session!")
        viewModel.sendMessage()
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.chatInput)
        coVerify { studyRoomSessionUseCase.sendMessage(testRoomId, any()) }
    }

    /** sendMessage is a no-op when chatInput is blank. */
    @Test
    fun `sendMessage does nothing when chatInput is blank`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onChatInputChange("   ")
        viewModel.sendMessage()
        advanceUntilIdle()

        coVerify(exactly = 0) { studyRoomSessionUseCase.sendMessage(any(), any()) }
    }

    /** leaveRoom calls use case and sets isJoined to false. */
    @Test
    fun `leaveRoom calls use case and updates isJoined`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.leaveRoom()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isJoined)
        coVerify { studyRoomSessionUseCase.leaveRoom(testRoomId, any()) }
    }
}
