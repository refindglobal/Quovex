package com.quovex.ui.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.remote.FirebaseAuthService
import com.quovex.domain.model.RoomChatMessage
import com.quovex.domain.model.RoomMember
import com.quovex.domain.usecase.StudyRoomSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudyRoomLiveUiState(
    val roomId: String = "",
    val members: List<RoomMember> = emptyList(),
    val chatMessages: List<RoomChatMessage> = emptyList(),
    val isBreakChatOpen: Boolean = false,
    val chatInput: String = "",
    val isJoined: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StudyRoomLiveViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studyRoomSessionUseCase: StudyRoomSessionUseCase,
    private val authService: FirebaseAuthService,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val roomId: String = savedStateHandle["roomId"] ?: ""

    private val _uiState = MutableStateFlow(StudyRoomLiveUiState(roomId = roomId))
    val uiState: StateFlow<StudyRoomLiveUiState> = _uiState.asStateFlow()

    private val currentUser: RoomMember
        get() {
            val profile = userPreferencesManager.userProfile.value
            val rankName = when {
                profile.xp >= 5000 -> "Grandmaster Scholar"
                profile.xp >= 2500 -> "Master Scholar"
                profile.xp >= 1000 -> "Adept Scholar"
                profile.xp >= 500 -> "Apprentice Scholar"
                else -> "Novice Scholar"
            }
            return RoomMember(
                userId = authService.currentUserId,
                userName = profile.name.ifBlank { "Aspirant" },
                avatarId = profile.avatarId,
                scholarRank = rankName,
                currentSessionMinutes = 0,
                isStudying = true,
                focusScore = 100
            )
        }

    init {
        if (roomId.isNotBlank()) {
            joinRoom()
            observeMembers()
            observeChat()
        }
    }

    /** Join the room on first load and mark the user as present. */
    private fun joinRoom() {
        viewModelScope.launch {
            val result = studyRoomSessionUseCase.joinRoom(roomId, currentUser)
            _uiState.update { it.copy(isJoined = result.isSuccess) }
        }
    }

    /** Real-time observation of all members present in the room. */
    private fun observeMembers() {
        viewModelScope.launch {
            studyRoomSessionUseCase.observeMembers(roomId).collect { members ->
                _uiState.update { it.copy(members = members) }
            }
        }
    }

    /** Real-time observation of break-time chat messages. */
    private fun observeChat() {
        viewModelScope.launch {
            studyRoomSessionUseCase.observeChat(roomId).collect { messages ->
                _uiState.update { it.copy(chatMessages = messages) }
            }
        }
    }

    fun onChatInputChange(value: String) {
        _uiState.update { it.copy(chatInput = value) }
    }

    fun toggleBreakChat() {
        _uiState.update { it.copy(isBreakChatOpen = !it.isBreakChatOpen) }
    }

    /** Send the composed break-time chat message. */
    fun sendMessage() {
        val text = _uiState.value.chatInput.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            val message = RoomChatMessage(
                roomId = roomId,
                senderId = currentUser.userId,
                senderName = currentUser.userName,
                messageText = text,
                timestampMillis = System.currentTimeMillis()
            )
            studyRoomSessionUseCase.sendMessage(roomId, message)
            _uiState.update { it.copy(chatInput = "") }
        }
    }

    /** Called when the user navigates away — removes presence from Firestore. */
    fun leaveRoom() {
        viewModelScope.launch {
            studyRoomSessionUseCase.leaveRoom(roomId, currentUser.userId)
            _uiState.update { it.copy(isJoined = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Best-effort leave on ViewModel destruction
        viewModelScope.launch { studyRoomSessionUseCase.leaveRoom(roomId, currentUser.userId) }
    }
}
