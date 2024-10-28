package org.noisevisionproductions.samplelibrary.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

actual class AuthService actual constructor() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    actual suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    actual suspend fun signIn(email: String, password: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val task = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val user = task.user
                if (user != null) {
                    Result.success(user.uid)
                } else {
                    Result.failure(Exception("Nie udało się zalogować użytkownika."))
                }
            } catch (e: Exception) {
                Log.e("AuthService", "Błąd podczas rejestracji: ${e.message}", e)
                Result.failure(e)
            }
        }

    actual suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val task = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = task.user

            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()

                user.updateProfile(profileUpdates).await()

                val userModel = UserModel(
                    id = user.uid,
                    username = username,
                    label = "",
                    registrationDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                )

                firestore.collection("users").document(user.uid).set(userModel).await()

                Result.success(user.uid)
            } else {
                Result.failure(Exception("Nie udało się zarejestrować użytkownika."))
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Błąd podczas rejestracji: ${e.message}", e)
            Result.failure(e)
        }
    }
}

