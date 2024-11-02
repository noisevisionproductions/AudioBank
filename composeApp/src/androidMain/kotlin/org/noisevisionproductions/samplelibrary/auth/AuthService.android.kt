package org.noisevisionproductions.samplelibrary.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

actual class AuthService actual constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    actual suspend fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    actual suspend fun signIn(email: String, password: String): Result<String> = executeAuthTask {
        val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        task.user?.uid ?: throw Exception("User login failed.")
    }

    actual suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<String> = executeAuthTask {
        val task = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = task.user ?: throw Exception("User registration failed.")
        updateUserProfile(user, username)
        saveUserToFirestore(user.uid, username)
        user.uid
    }

    private suspend fun updateUserProfile(user: FirebaseUser, username: String) {
        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(username).build()
        user.updateProfile(profileUpdates).await()
    }

    private suspend fun saveUserToFirestore(uid: String, username: String) {
        val userModel = UserModel(
            id = uid,
            username = username,
            label = "",
            registrationDate = getCurrentTimestamp()
        )
        firestore.collection("users").document(uid).set(userModel).await()
    }

    private suspend fun <T> executeAuthTask(task: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(task())
            } catch (e: Exception) {
                Log.e("AuthService", "Error during authentication: ${e.message}", e)
                Result.failure(e)
            }
        }
}

