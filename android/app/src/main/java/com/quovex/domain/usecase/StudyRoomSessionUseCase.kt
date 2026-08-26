package com.quovex.domain.usecase

import com.quovex.data.remote.FirebaseFirestoreService
import com.quovex.domain.model.RoomChatMessage
import com.quovex.domain.model.RoomMember
import com.quovex.domain.model.StudyRoomSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Manages all real-time interactions within a Live Study Room:
 * - Joining / leaving room presence
 * - Real-time member list observation
 * - Break-time chat message streaming and sending
 */
class StudyRoomSessionUseCase @Inject constructor(
    private val firestoreService: FirebaseFirestoreService
) {

    /**
     * Write the current user's presence to a room and increment its member count.
     */
    suspend fun joinRoom(roomId: String, member: RoomMember): Result<Unit> =
        firestoreService.joinStudyRoom(roomId, member)

    /**
     * Remove the current user's presence from a room and decrement its member count.
     */
    suspend fun leaveRoom(roomId: String, userId: String): Result<Unit> =
        firestoreService.leaveStudyRoom(roomId, userId)

    /**
     * Observe real-time live members for the given room.
     */
    fun observeMembers(roomId: String): Flow<List<RoomMember>> =
        firestoreService.getRoomMembersFlow(roomId)

    /**
     * Observe real-time break-time chat messages for the given room.
     */
    fun observeChat(roomId: String): Flow<List<RoomChatMessage>> =
        firestoreService.getRoomChatFlow(roomId)

    /**
     * Post a new chat message to the room's break-time chat.
     */
    suspend fun sendMessage(roomId: String, message: RoomChatMessage): Result<Unit> =
        firestoreService.sendRoomChatMessage(roomId, message)
}
