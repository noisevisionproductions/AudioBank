package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

actual class UserRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    actual suspend fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    actual suspend fun getCurrentUser(): UserModel? = withContext(Dispatchers.IO) {
        val uid = getCurrentUserId()
        if (uid != null) {
            try {
                val documentSnapshot = firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()
                documentSnapshot.toObject(UserModel::class.java)
            } catch (e: Exception) {
                Log.e(
                    "UserRepository",
                    "Błąd podczas pobierania danych użytkownika: ${e.message}",
                    e
                )
                null
            }
        } else {
            null
        }
    }

    actual suspend fun getUsernameById(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val userModel = documentSnapshot.toObject(UserModel::class.java)
            userModel?.username
        } catch (e: Exception) {
            Log.e("AuthService", "Błąd podczas pobierania nazwy użytkownika: ${e.message}", e)
            null
        }
    }

    actual suspend fun getUserLabelById(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            val userModel = documentSnapshot.toObject(UserModel::class.java)
            userModel?.label
        } catch (e: Exception) {
            Log.e("AuthService", "Błąd podczas pobierania labela: ${e.message}", e)
            null
        }
    }

    actual suspend fun updateAvatarUrl(url: String) {
        withContext(Dispatchers.IO) {
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw IllegalStateException("User not logged in")
                firestore.collection("users").document(userId)
                    .update("avatarUrl", url)
                    .await()
            } catch (e: Exception) {
                Log.e("UserRepository", "Error updating avatar URL", e)
                throw e
            }
        }
    }

    actual suspend fun getPostsByIds(postIds: List<String>): List<PostModel> =
        withContext(Dispatchers.IO) {
            try {
                postIds.map { postId ->
                    async {
                        val documentSnapshot = firestore.collection("posts")
                            .document(postId)
                            .get()
                            .await()
                        documentSnapshot.toObject(PostModel::class.java)
                    }
                }.awaitAll().filterNotNull()
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching posts by IDs: ${e.message}", e)
                emptyList()
            }
        }

    actual suspend fun getLikedPosts(): Result<List<PostModel>> =
        withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId() ?: return@withContext Result.failure(IllegalStateException("User not logged in"))

                val userDocument = firestore.collection("users").document(userId).get().await()
                val userModel = userDocument.toObject(UserModel::class.java)

                val likedPostIds = userModel?.likedPosts ?: emptyList()

                val posts = likedPostIds.map { postId ->
                    async {
                        val documentSnapshot = firestore.collection("posts")
                            .document(postId)
                            .get()
                            .await()
                        documentSnapshot.toObject(PostModel::class.java)
                    }
                }.awaitAll().filterNotNull()

                Result.success(posts)
            } catch (e: Exception) {
                Log.e("UserRepository", "Error fetching liked posts: ${e.message}", e)
                Result.failure(e)
            }
        }


    actual suspend fun removeLikedPost(postId: String) {
        val uid = getCurrentUserId()
        if (uid != null) {
            try {
                val userDocRef = firestore.collection("users").document(uid)
                val postDocRef = firestore.collection("poss").document(postId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(userDocRef)
                    val likedPosts =
                        (snapshot.get("likedPosts") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList()

                    val postSnapshot = transaction.get(postDocRef)
                    val currentLikes = (postSnapshot.get("likesCount") as? Long) ?: 0L

                    val updatedLikedPosts = likedPosts.filterNot { it == postId }
                    transaction.update(userDocRef, "likedPosts", updatedLikedPosts)

                    transaction.update(
                        postDocRef,
                        "likesCount",
                        (currentLikes - 1).coerceAtLeast(0)
                    )
                }.await()
            } catch (e: Exception) {
                Log.e("UserRepository", "Błąd podczas usuwania polubionego posta: ${e.message}", e)
                throw e
            }
        } else {
            throw Exception("Nie udało się pobrać ID użytkownika.")
        }
    }
}