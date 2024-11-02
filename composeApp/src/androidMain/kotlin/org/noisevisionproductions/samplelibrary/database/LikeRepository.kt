package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.auth.AuthService

actual class LikeRepository actual constructor(
    private val authService: AuthService
) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private suspend fun getCurrentUserId(): Result<String> {
        val uid = authService.getCurrentUserId()
        return if (uid != null) {
            Result.success(uid)
        } else {
            Result.failure(Exception("User not authenticated"))
        }
    }

    actual suspend fun toggleLikePost(postId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            getCurrentUserId().fold(
                onSuccess = { uid ->
                    try {
                        val userLikesRef = firestore.collection("users").document(uid)
                        val postRef = firestore.collection("posts").document(postId)

                        var isLiked = false
                        firestore.runTransaction { transaction ->
                            val postDoc = transaction.get(postRef)
                            val userDoc = transaction.get(userLikesRef)

                            val currentLikes = postDoc.getLong("likesCount")?.toInt() ?: 0
                            val likedPosts =
                                userDoc.get("likedPosts") as? List<*> ?: listOf<String>()

                            isLiked = postId !in likedPosts

                            val newLikes =
                                if (isLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(
                                    0
                                )

                            val newLikedPosts = if (isLiked) {
                                likedPosts + postId
                            } else {
                                likedPosts - postId
                            }

                            transaction.update(postRef, "likesCount", newLikes)
                            transaction.update(userLikesRef, "likedPosts", newLikedPosts)
                        }.await()

                        Result.success(isLiked)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }

    actual suspend fun getPostLikesCount(postId: String): Result<Int> =
        withContext(Dispatchers.IO) {
            try {
                val postDoc = firestore.collection("posts").document(postId).get().await()
                val likesCount = postDoc.getLong("likesCount")?.toInt() ?: 0
                Result.success(likesCount)
            } catch (e: Exception) {
                Log.e("LikeRepository", "Error getting likes count: ${e.message}", e)
                Result.failure(e)
            }
        }

    actual suspend fun isPostLiked(postId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        getCurrentUserId().fold(
            onSuccess = { uid ->
                try {
                    val documentSnapshot = firestore.collection("users").document(uid).get().await()
                    val likedPosts = documentSnapshot.get("likedPosts") as? List<*>
                    Result.success(likedPosts?.contains(postId) == true)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )

    }

    actual suspend fun isCommentLiked(commentId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            getCurrentUserId().fold(
                onSuccess = { uid ->
                    val documentSnapshot =
                        firestore.collection("users").document(uid).get().await()
                    val likedComments = documentSnapshot.get("likedComments") as? List<*>
                    Result.success(likedComments?.contains(commentId) == true)
                },
                onFailure = { Result.failure(it) }
            )

        }

    actual suspend fun toggleLikeComment(commentId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            getCurrentUserId().fold(
                onSuccess = { uid ->
                    try {
                        val userReference = firestore.collection("users").document(uid)
                        val commentReference = firestore.collection("comments").document(commentId)

                        firestore.runTransaction { transaction ->
                            val userSnapshot = transaction.get(userReference)
                            val commentSnapshot = transaction.get(commentReference)

                            val likedComments =
                                userSnapshot.get("likedComments") as? List<*> ?: listOf<String>()
                            val isCurrentlyLiked = likedComments.contains(commentId)

                            val updatedLikedComments = if (isCurrentlyLiked) {
                                likedComments - commentId
                            } else {
                                likedComments + commentId
                            }

                            val currentLikes = commentSnapshot.getLong("likesCount")?.toInt() ?: 0
                            val updatedLikesCount = if (isCurrentlyLiked) {
                                (currentLikes - 1).coerceAtLeast(0)
                            } else {
                                currentLikes + 1
                            }

                            transaction.update(userReference, "likedComments", updatedLikedComments)
                            transaction.update(commentReference, "likesCount", updatedLikesCount)
                        }.await()

                        Result.success(Unit)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }

    actual suspend fun toggleSoundLike(soundId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            getCurrentUserId().fold(
                onSuccess = { uid ->
                    try {
                        val userRef = firestore.collection("users").document(uid)
                        firestore.runTransaction { transaction ->
                            val userSnapshot = transaction.get(userRef)
                            val likedSounds =
                                userSnapshot.get("likedSounds") as? List<*> ?: listOf<String>()

                            val updatedLikedSounds = if (likedSounds.contains(soundId)) {
                                likedSounds - soundId
                            } else {
                                likedSounds + soundId
                            }
                            transaction.update(userRef, "likedSounds", updatedLikedSounds)
                        }.await()

                        Result.success(Unit)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }
}