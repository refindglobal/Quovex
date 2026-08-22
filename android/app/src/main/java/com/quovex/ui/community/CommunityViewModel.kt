package com.quovex.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.StudyRoomModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityUiState(
    val rooms: List<StudyRoomModel> = emptyList(),
    val selectedFilter: String = "All",
    val isLoading: Boolean = true,
    val joinedRoomId: String? = null
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val firestoreService: FirebaseFirestoreService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun joinRoom(roomId: String) {
        _uiState.update { it.copy(joinedRoomId = roomId) }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            // Ensure Firestore has initial room collection
            firestoreService.seedInitialRoomsIfEmpty()

            firestoreService.getStudyRoomsFlow().collect { roomList ->
                _uiState.update { it.copy(rooms = roomList, isLoading = false) }
            }
        }
    }
}
