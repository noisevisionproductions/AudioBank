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

actual class UserRepository actual constructor() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getCurrentUserIdOrError(): Result<String> {
        return firebaseAuth.currentUser?.uid?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("User not logged in"))
    }

    actual suspend fun getCurrentUserId(): Result<String> = getCurrentUserIdOrError()

    actual suspend fun getCurrentUser(): Result<UserModel?> = withContext(Dispatchers.IO) {
        val userIdResult = getCurrentUserIdOrError()
        userIdResult.fold(
            onSuccess = { uid ->
                try {
                    val documentSnapshot = firestore.collection("users")
                        .document(uid)
                        .get()
                        .await()
                    Result.success(documentSnapshot.toObject(UserModel::class.java))
                } catch (e: Exception) {
                    handleError("Error fetching current user data", e)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    actual suspend fun getUsernameById(userId: String): Result<String?> =
        withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                val username = documentSnapshot.toObject(UserModel::class.java)?.username
                Result.success(username)
            } catch (e: Exception) {
                handleError("Error fetching username", e)
            }
        }

    actual suspend fun getUserLabelById(userId: String): Result<String?> =
        withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                val label = documentSnapshot.toObject(UserModel::class.java)?.label
                Result.success(label)
            } catch (e: Exception) {
                handleError("Error fetching user label", e)
            }
        }

    actual suspend fun getCurrentUserAvatarPath(): Result<String?> = withContext(Dispatchers.IO) {
        val userIdResult = getCurrentUserIdOrError()
        userIdResult.fold(
            onSuccess = { uid ->
                try {
                    val userDocument = firestore.collection("users")
                        .document(uid)
                        .get()
                        .await()
                    Result.success(userDocument.toObject(UserModel::class.java)?.avatarUrl)
                } catch (e: Exception) {
                    handleError("Error fetching avatar URL", e)
                }
            }, onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    actual suspend fun updateAvatarUrl(url: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userIdResult = getCurrentUserIdOrError()
        userIdResult.fold(
            onSuccess = { uid ->
                try {
                    firestore.collection("users")
                        .document(uid)
                        .update("avatarUrl", url)
                        .await()
                    Result.success(Unit)
                } catch (e: Exception) {
                    handleError("Error updating avatar URL", e)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    actual suspend fun getPostsByIds(postIds: List<String>): Result<List<PostModel>> =
        withContext(Dispatchers.IO) {
            try {
                val posts = postIds.map { postId ->
                    async {
                        firestore.collection("posts")
                            .document(postId)
                            .get()
                            .await()
                            .toObject(PostModel::class.java)
                    }
                }.awaitAll().filterNotNull()
                Result.success(posts)
            } catch (e: Exception) {
                handleError("Error fetching posts by IDs", e)
            }
        }

    actual suspend fun getLikedPosts(): Result<List<PostModel>> = withContext(Dispatchers.IO) {
        val userIdResult = getCurrentUserIdOrError()
        userIdResult.fold(
            onSuccess = { uid ->
                try {
                    val userDocument = firestore.collection("users")
                        .document(uid)
                        .get()
                        .await()
                    val likedPostIds =
                        userDocument.toObject(UserModel::class.java)?.likedPosts ?: emptyList()
                    val likedPosts = likedPostIds.map { postId ->
                        async {
                            firestore.collection("posts")
                                .document(postId)
                                .get()
                                .await()
                                .toObject(PostModel::class.java)
                        }
                    }.awaitAll().filterNotNull()
                    Result.success(likedPosts)
                } catch (e: Exception) {
                    handleError("Error fetching liked posts", e)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    actual suspend fun removeLikedPost(postId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userIdResult = getCurrentUserIdOrError()
        userIdResult.fold(
            onSuccess = { uid ->
                try {
                    val userDocRef = firestore.collection("users")
                        .document(uid)
                    val postDocRef = firestore.collection("posts")
                        .document(postId)

                    firestore.runTransaction { transaction ->
                        val userSnapshot = transaction.get(userDocRef)
                        val likedPosts =
                            (userSnapshot.get("likedPosts") as? List<*>)?.mapNotNull { it as? String }
                                ?: emptyList()
                        val updatedLikedPosts = likedPosts.filterNot { it == postId }

                        val postSnapshot = transaction.get(postDocRef)
                        val currentLikes = postSnapshot.getLong("likesCount") ?: 0L

                        transaction.update(postDocRef, "likedPosts", updatedLikedPosts)
                        transaction.update(
                            postDocRef,
                            "likesCount",
                            (currentLikes - 1).coerceAtLeast(0)
                        )

                        null
                    }.await()
                    Result.success(Unit)
                } catch (e: Exception) {
                    handleError("Error removing liked post", e)
                }
            }, onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    actual suspend fun getLikedSounds(): Result<List<String>> = withContext(Dispatchers.IO) {
        val userIdResult = getCurrentUserIdOrError()
        userIdResult.fold(
            onSuccess = { uid ->
                try {
                    val userDoc = firestore.collection("users")
                        .document(uid)
                        .get()
                        .await()
                    val likedSounds =
                        (userDoc.get("likedSounds") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList()
                    Result.success(likedSounds)
                } catch (e: Exception) {
                    handleError("Error fetching liked sounds", e)
                }
            }, onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    private fun <T> handleError(errorMessage: String, e: Exception): Result<T> {
        Log.e("UserRepository", "$errorMessage: ${e.message}", e)
        return Result.failure(e)
    }
}