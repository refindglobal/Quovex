package com.quovex.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth?
) {
    open val currentUser: FirebaseUser?
        get() = try { auth?.currentUser } catch (e: Exception) { null }

    open val currentUserId: String
        get() = try { auth?.currentUser?.uid ?: "user_anonymous" } catch (e: Exception) { "user_anonymous" }

    open val isUserLoggedIn: Boolean
        get() = try { auth?.currentUser != null } catch (e: Exception) { false }

    open suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        val user = currentUser ?: return null
        return try {
            user.getIdToken(forceRefresh).await()?.token
        } catch (e: Exception) {
            null
        }
    }

    // ─── Google Sign-In (Primary Auth Method) ─────────────────────────────────
    suspend fun signInWithGoogle(context: Context, webClientId: String): Result<FirebaseUser> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // allow any Google account
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = credentialResponse.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(credential.data)
                    .idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = requireNotNull(auth) { "FirebaseAuth instance not initialized" }.signInWithCredential(firebaseCredential).await()
                val user = authResult.user ?: throw Exception("Google Sign-In returned null user")
                Result.success(user)
            } else {
                Result.failure(Exception("Unexpected credential type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Email Sign-In (Secondary) ────────────────────────────────────────────
    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = requireNotNull(auth) { "FirebaseAuth instance not initialized" }.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("Email sign-in returned null user")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = requireNotNull(auth) { "FirebaseAuth instance not initialized" }.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw Exception("Email sign-up returned null user")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Anonymous (Fallback Guest Mode) ─────────────────────────────────────
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            val authResult = requireNotNull(auth) { "FirebaseAuth instance not initialized" }.signInAnonymously().await()
            val user = authResult.user ?: throw Exception("Anonymous sign-in returned null user")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth?.signOut()
    }
}
