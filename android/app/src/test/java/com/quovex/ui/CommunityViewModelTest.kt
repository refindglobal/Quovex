package com.quovex.ui.community

import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.LeaderboardEntry
import com.quovex.domain.model.LeaderboardType
import com.quovex.domain.model.StudyRoomModel
import com.quovex.domain.usecase.GetLeaderboardUseCase
import com.quovex.domain.usecase.ManageFriendsAndBattlesUseCase
import io.mockk.coEvery
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
class CommunityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var firestoreService: FirebaseFirestoreService
    private lateinit var getLeaderboardUseCase: GetLeaderboardUseCase
    private lateinit var manageFriendsAndBattlesUseCase: ManageFriendsAndBattlesUseCase
    private lateinit var authService: com.quovex.data.remote.FirebaseAuthService
    private lateinit var viewModel: CommunityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        firestoreService = mockk(relaxed = true)
        getLeaderboardUseCase = mockk(relaxed = true)
        manageFriendsAndBattlesUseCase = mockk(relaxed = true)
        authService = mockk(relaxed = true)

        every { authService.currentUserId } returns "user_test"

        // Minimal stubs for init block
        every { firestoreService.getStudyRoomsFlow() } returns flowOf(emptyList())
        every { manageFriendsAndBattlesUseCase.observeFriends() } returns flowOf(emptyList())
        every { manageFriendsAndBattlesUseCase.observeBattles(any()) } returns flowOf(emptyList())

        viewModel = CommunityViewModel(
            firestoreService = firestoreService,
            getLeaderboardUseCase = getLeaderboardUseCase,
            manageFriendsAndBattlesUseCase = manageFriendsAndBattlesUseCase,
            authService = authService
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Default active tab is ROOMS. */
    @Test
    fun `initial state has ROOMS as the active tab`() = runTest {
        assertEquals(CommunityTab.ROOMS, viewModel.uiState.value.activeTab)
    }

    /** Selecting the LEADERBOARD tab triggers a leaderboard load. */
    @Test
    fun `selectTab LEADERBOARD triggers leaderboard fetch`() = runTest {
        val entries = listOf(
            LeaderboardEntry(userId = "u1", userName = "Alice", rank = 1, studyMinutes = 300)
        )
        coEvery { getLeaderboardUseCase.execute(any(), any(), any()) } returns entries

        viewModel.selectTab(CommunityTab.LEADERBOARD)
        advanceUntilIdle()

        assertEquals(CommunityTab.LEADERBOARD, viewModel.uiState.value.activeTab)
        assertEquals(1, viewModel.uiState.value.leaderboardEntries.size)
        assertEquals("Alice", viewModel.uiState.value.leaderboardEntries[0].userName)
    }

    /** Rooms emitted by Firestore are reflected in state. */
    @Test
    fun `rooms loaded from Firestore appear in state`() = runTest {
        val rooms = listOf(
            StudyRoomModel(id = "r1", name = "Physics Room", subject = "Physics", activeMembers = 12)
        )
        every { firestoreService.getStudyRoomsFlow() } returns flowOf(rooms)
        every { manageFriendsAndBattlesUseCase.observeFriends() } returns flowOf(emptyList())
        every { manageFriendsAndBattlesUseCase.observeBattles(any()) } returns flowOf(emptyList())

        // Re-create VM so the new flow stub is picked up
        val vm = CommunityViewModel(firestoreService, getLeaderboardUseCase, manageFriendsAndBattlesUseCase, authService)
        advanceUntilIdle()

        assertEquals(1, vm.uiState.value.rooms.size)
        assertEquals("Physics Room", vm.uiState.value.rooms[0].name)
        assertFalse(vm.uiState.value.isRoomsLoading)
    }

    /** joinRoom stores the roomId in state and clearJoinedRoom resets it. */
    @Test
    fun `joinRoom and clearJoinedRoom manage joinedRoomId correctly`() = runTest {
        viewModel.joinRoom("room_xyz")
        assertEquals("room_xyz", viewModel.uiState.value.joinedRoomId)

        viewModel.clearJoinedRoom()
        assertEquals(null, viewModel.uiState.value.joinedRoomId)
    }

    /** Changing the leaderboard type updates state and triggers a fetch. */
    @Test
    fun `selectLeaderboardType updates type in state and fetches`() = runTest {
        coEvery { getLeaderboardUseCase.execute(any(), any(), any()) } returns emptyList()

        viewModel.selectLeaderboardType(LeaderboardType.FRIENDS)
        advanceUntilIdle()

        assertEquals(LeaderboardType.FRIENDS, viewModel.uiState.value.leaderboardType)
    }

    /** Filter selection updates selectedRoomFilter in state. */
    @Test
    fun `selectRoomFilter updates filter in state`() {
        viewModel.selectRoomFilter("Physics")
        assertEquals("Physics", viewModel.uiState.value.selectedRoomFilter)
    }
}
