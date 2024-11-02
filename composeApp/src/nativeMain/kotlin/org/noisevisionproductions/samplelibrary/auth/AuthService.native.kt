package org.noisevisionproductions.samplelibrary.auth

import platform.Foundation.NSLog
import kotlinx.coroutines.*
import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRUser
import cocoapods.FirebaseFirestore.FIRFirestore
import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp
import org.noisevisionproductions.samplelibrary.utils.models.UserModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class AuthService actual constructor() {

    private val firebaseAuth: FIRAuth = FIRAuth.auth()
    private val firestore: FIRFirestore = FIRFirestore.firestore()

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    actual suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    actual suspend fun signIn(email: String, password: String): Result<String> =
        executeAuthTask {
            val userId = suspendCoroutine<String> { continuation ->
                firebaseAuth.signInWithEmail(email, password) { user, error ->
                    if (user != null) {
                        continuation.resume(user.uid)
                    } else {
                        continuation.resumeWithException(
                            Exception(
                                error?.localizedDescription ?: "Sign-in failed"
                            )
                        )
                    }
                }
            }
            userId
        }

    actual suspend fun signUp(username: String, email: String, password: String): Result<String> =
        executeAuthTask {
            val userId = suspendCoroutine<String> { continuation ->
                firebaseAuth.createUserWithEmail(email, password) { user, error ->
                    if (user != null) {
                        updateUserProfile(user, username)
                        saveUserToFirestore(user.uid, username)
                        continuation.resume(user.uid)
                    } else {
                        continuation.resumeWithException(
                            Exception(
                                error?.localizedDescription ?: "Sign-up failed"
                            )
                        )
                    }
                }
            }
            userId
        }

    private suspend fun updateUserProfile(user: FIRUser, username: String) {
        suspendCoroutine<Unit> { continuation ->
            val changeRequest = user.profileChangeRequest()
            changeRequest.displayName = username
            changeRequest.commitChangesWithCompletion { error ->
                if (error == null) continuation.resume(Unit)
                else continuation.resumeWithException(Exception(error.localizedDescription))
            }
        }
    }

    private suspend fun saveUserToFirestore(uid: String, username: String) {
        val userModel = UserModel(
            id = uid,
            username = username,
            label = "",
            registrationDate = getCurrentTimestamp()
        )
        suspendCoroutine<Unit> { continuation ->
            firestore.collection("users").document(uid).setData(userModel.toMap()) { error ->
                if (error == null) continuation.resume(Unit)
                else continuation.resumeWithException(Exception(error.localizedDescription))
            }
        }
    }

    private suspend fun <T> executeAuthTask(task: suspend () -> T): Result<T> =
        withContext(Dispatchers.Main) {
            try {
                Result.success(task())
            } catch (e: Exception) {
                NSLog("Error during authentication: ${e.localizedMessage}")
                Result.failure(e)
            }
        }
}
