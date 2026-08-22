package com.quovex.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * QuovexMessagingService — handles FCM push notifications.
 * Registered in AndroidManifest.xml.
 *
 * Handles:
 * - Daily streak reminders
 * - Flashcard due alerts
 * - Study room invites
 * - AI query result ready
 */
class QuovexMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Notification data is available in message.notification and message.data
        // Android shows the notification automatically when the app is in background.
        // Foreground handling can be added here if needed.
    }

    /**
     * Called when a new FCM token is generated (first launch or token refresh).
     * Saves the token to Firestore so backend can send targeted notifications.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            saveFcmTokenToFirestore(token)
        }
    }

    private suspend fun saveFcmTokenToFirestore(token: String) {
        try {
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val uid = auth.currentUser?.uid ?: return

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(mapOf("fcmToken" to token, "lastActiveAt" to System.currentTimeMillis()), SetOptions.merge())
                .await()
        } catch (_: Exception) {}
    }
}

/**
 * Call this on first app launch (after user is signed in)
 * to register and sync the current FCM token.
 */
suspend fun registerFcmToken(uid: String) {
    try {
        val token = FirebaseMessaging.getInstance().token.await()
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .await()
    } catch (_: Exception) {}
}
