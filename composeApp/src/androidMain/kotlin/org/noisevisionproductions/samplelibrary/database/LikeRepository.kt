package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

actual class LikeRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authService = AuthService()

    actual suspend fun toggleLikePost(postId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val uid = authService.getCurrentUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                val userLikesRef = firestore.collection("users").document(uid)
                val postRef = firestore.collection("posts").document(postId)

                var isLiked = false
                firestore.runTransaction { transaction ->
                    val postDoc = transaction.get(postRef)
                    val userDoc = transaction.get(userLikesRef)

                    val currentLikes = postDoc.getLong("likesCount")?.toInt() ?: 0
                    val likedPosts =
                        (userDoc.get("likedPosts") as? List<*>)?.filterIsInstance<String>()
                            ?: listOf()

                    isLiked = postId !in likedPosts

                    val newLikes = if (isLiked) currentLikes + 1 else currentLikes - 1

                    val newLikedPosts = if (isLiked) {
                        likedPosts + postId
                    } else {
                        likedPosts - postId
                    }

                    transaction.update(postRef, "likesCount", newLikes)
                    transaction.update(userLikesRef, "likedPosts", newLikedPosts)
                }.await()

                return@withContext Result.success(isLiked)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
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

    actual suspend fun isPostLiked(postId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = authService.getCurrentUserId()
        if (uid != null) {
            val documentSnapshot = firestore.collection("users").document(uid).get().await()
            val userModel = documentSnapshot.toObject(UserModel::class.java)
            userModel?.likedPosts?.contains(postId) == true
        } else {
            false
        }
    }

    actual suspend fun isCommentLiked(commentId: String): Boolean {
        val userId = authService.getCurrentUserId() ?: return false
        val documentSnapshot = firestore.collection("users").document(userId).get().await()
        val userModel = documentSnapshot.toObject(UserModel::class.java)
        return userModel?.likedComments?.contains(commentId) == true
    }

    actual suspend fun toggleLikeComment(commentId: String): Result<Unit> {
        val userId =
            authService.getCurrentUserId() ?: return Result.failure(Exception("User not logged in"))

        val userReference = firestore.collection("users").document(userId)
        val commentReference = firestore.collection("comments").document(commentId)

        return try {
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userReference)
                val commentSnapshot = transaction.get(commentReference)

                val user = userSnapshot.toObject(UserModel::class.java)
                val comment = commentSnapshot.toObject(CommentModel::class.java)

                if (user != null && comment != null) {
                    val isCurrentlyLiked = user.likedComments.contains(commentId)

                    val updatedLikedComments = if (isCurrentlyLiked) {
                        user.likedComments - commentId
                    } else {
                        user.likedComments + commentId
                    }

                    val updatedLikesCount = if (isCurrentlyLiked) {
                        (comment.likesCount - 1).coerceAtLeast(0)
                    } else {
                        comment.likesCount + 1
                    }

                    transaction.update(userReference, "likedComments", updatedLikedComments)
                    transaction.update(commentReference, "likesCount", updatedLikesCount)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun toggleSoundLike(soundId: String) {
        val userId = authService.getCurrentUserId()
        if (userId != null) {
            val userRef = firestore.collection("users").document(userId)
            firestore.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef)
                val currentLikedSounds =
                    (userSnapshot.get("likedSounds") as? List<*>)?.filterIsInstance<String>()
                        ?: listOf()

                val updatedLikedSounds = if (currentLikedSounds.contains(soundId)) {
                    currentLikedSounds - soundId
                } else {
                    currentLikedSounds + soundId
                }
                transaction.update(userRef, "likedSounds", updatedLikedSounds)
            }
        }
    }


}