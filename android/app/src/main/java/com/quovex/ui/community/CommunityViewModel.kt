package com.quovex.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.FriendProfile
import com.quovex.domain.model.LeaderboardEntry
import com.quovex.domain.model.LeaderboardType
import com.quovex.domain.model.StudyBattle
import com.quovex.domain.model.StudyRoomModel
import com.quovex.domain.usecase.GetLeaderboardUseCase
import com.quovex.domain.usecase.ManageFriendsAndBattlesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Tabs in the Community hub. */
enum class CommunityTab { ROOMS, LEADERBOARD, BATTLES }

data class CommunityUiState(
    // ---- Rooms tab ----
    val rooms: List<StudyRoomModel> = emptyList(),
    val selectedRoomFilter: String = "All",
    val isRoomsLoading: Boolean = true,
    val joinedRoomId: String? = null,

    // ---- Leaderboard tab ----
    val leaderboardType: LeaderboardType = LeaderboardType.GLOBAL,
    val leaderboardSubjectFilter: String = "ALL",
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val isLeaderboardLoading: Boolean = false,

    // ---- Battles & Friends tab ----
    val friends: List<FriendProfile> = emptyList(),
    val battles: List<StudyBattle> = emptyList(),
    val isBattlesLoading: Boolean = false,

    // ---- Global ----
    val activeTab: CommunityTab = CommunityTab.ROOMS,
    val error: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val firestoreService: FirebaseFirestoreService,
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    private val manageFriendsAndBattlesUseCase: ManageFriendsAndBattlesUseCase,
    private val authService: FirebaseAuthService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    private val currentUserId: String
        get() = authService.currentUserId

    init {
        loadRooms()
        observeFriends()
        observeBattles()
    }

    // ---- Tab navigation ----

    fun selectTab(tab: CommunityTab) {
        _uiState.update { it.copy(activeTab = tab) }
        when (tab) {
            CommunityTab.LEADERBOARD -> loadLeaderboard()
            CommunityTab.BATTLES -> { /* already observing */ }
            else -> Unit
        }
    }

    // ---- Rooms ----

    fun selectRoomFilter(filter: String) {
        _uiState.update { it.copy(selectedRoomFilter = filter) }
    }

    fun joinRoom(roomId: String) {
        _uiState.update { it.copy(joinedRoomId = roomId) }
    }

    fun clearJoinedRoom() {
        _uiState.update { it.copy(joinedRoomId = null) }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            firestoreService.seedInitialRoomsIfEmpty()
            firestoreService.getStudyRoomsFlow().collect { roomList ->
                _uiState.update { it.copy(rooms = roomList, isRoomsLoading = false) }
            }
        }
    }

    // ---- Leaderboard ----

    fun selectLeaderboardType(type: LeaderboardType) {
        _uiState.update { it.copy(leaderboardType = type) }
        loadLeaderboard()
    }

    fun selectLeaderboardSubject(subject: String) {
        _uiState.update { it.copy(leaderboardSubjectFilter = subject) }
        if (_uiState.value.leaderboardType == LeaderboardType.SUBJECT) loadLeaderboard()
    }

    private fun loadLeaderboard() {
        val state = _uiState.value
        _uiState.update { it.copy(isLeaderboardLoading = true) }
        viewModelScope.launch {
            val entries = getLeaderboardUseCase.execute(
                type = state.leaderboardType,
                subjectFilter = state.leaderboardSubjectFilter,
                currentUserId = currentUserId
            )
            _uiState.update { it.copy(leaderboardEntries = entries, isLeaderboardLoading = false) }
        }
    }

    // ---- Friends & Battles ----

    private fun observeFriends() {
        viewModelScope.launch {
            manageFriendsAndBattlesUseCase.observeFriends().collect { friends ->
                _uiState.update { it.copy(friends = friends) }
            }
        }
    }

    private fun observeBattles() {
        viewModelScope.launch {
            manageFriendsAndBattlesUseCase.observeBattles(currentUserId).collect { battles ->
                _uiState.update { it.copy(battles = battles, isBattlesLoading = false) }
            }
        }
    }
}
