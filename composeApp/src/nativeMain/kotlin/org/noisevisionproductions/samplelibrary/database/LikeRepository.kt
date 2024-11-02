package org.noisevisionproductions.samplelibrary.database

import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRTransaction
import kotlinx.coroutines.await
import org.noisevisionproductions.samplelibrary.auth.AuthService
import platform.Foundation.NSLog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class LikeRepository actual constructor(
    private val authService: AuthService
) {
    private val firestore: FIRFirestore = FIRFirestore.firestore()

    private suspend fun getCurrentUserId(): Result<String> {
        val uid = authService.getCurrentUserId()
        return if (uid != null) {
            Result.success(uid)
        } else {
            Result.failure(Exception("User not authenticated"))
        }
    }

    actual suspend fun toggleLikePost(postId: String): Result<Boolean> = try {
        getCurrentUserId().fold(
            onSuccess = { uid ->
                val userLikesRef = firestore.collection("users").document(uid)
                val postRef = firestore.collection("posts").document(postId)

                var isLiked = false
                firestore.runTransaction { transaction, error ->
                    if (error != null) throw error

                    val postDoc = transaction.getDocument(postRef)
                    val userDoc = transaction.getDocument(userLikesRef)

                    val currentLikes = postDoc.getLong("likesCount")?.toInt() ?: 0
                    val likedPosts = userDoc.get("likedPosts") as? List<*> ?: listOf<String>()

                    isLiked = postId !in likedPosts

                    val newLikes =
                        if (isLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)

                    val newLikedPosts = if (isLiked) {
                        likedPosts + postId
                    } else {
                        likedPosts - postId
                    }

                    transaction.updateData(mapOf("likesCount" to newLikes), postRef)
                    transaction.updateData(mapOf("likedPosts" to newLikedPosts), userLikesRef)
                }.await()

                Result.success(isLiked)
            },
            onFailure = { Result.failure(it) }
        )
    } catch (e: Exception) {
        NSLog("Error toggling like on post: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun getPostLikesCount(postId: String): Result<Int> = try {
        val postDoc = firestore.collection("posts").document(postId).getDocument().await()
        val likesCount = postDoc.getLong("likesCount")?.toInt() ?: 0
        Result.success(likesCount)
    } catch (e: Exception) {
        NSLog("Error getting post likes count: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun isPostLiked(postId: String): Result<Boolean> = try {
        getCurrentUserId().fold(
            onSuccess = { uid ->
                val userDoc = firestore.collection("users").document(uid).getDocument().await()
                val likedPosts = userDoc.get("likedPosts") as? List<*>
                Result.success(likedPosts?.contains(postId) == true)
            },
            onFailure = { Result.failure(it) }
        )
    } catch (e: Exception) {
        NSLog("Error checking if post is liked: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun isCommentLiked(commentId: String): Result<Boolean> = try {
        getCurrentUserId().fold(
            onSuccess = { uid ->
                val userDoc = firestore.collection("users").document(uid).getDocument().await()
                val likedComments = userDoc.get("likedComments") as? List<*>
                Result.success(likedComments?.contains(commentId) == true)
            },
            onFailure = { Result.failure(it) }
        )
    } catch (e: Exception) {
        NSLog("Error checking if comment is liked: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun toggleLikeComment(commentId: String): Result<Unit> = try {
        getCurrentUserId().fold(
            onSuccess = { uid ->
                val userRef = firestore.collection("users").document(uid)
                val commentRef = firestore.collection("comments").document(commentId)

                firestore.runTransaction { transaction, error ->
                    if (error != null) throw error

                    val userDoc = transaction.getDocument(userRef)
                    val commentDoc = transaction.getDocument(commentRef)

                    val likedComments = userDoc.get("likedComments") as? List<*> ?: listOf<String>()
                    val isLiked = likedComments.contains(commentId)

                    val newLikedComments = if (isLiked) {
                        likedComments - commentId
                    } else {
                        likedComments + commentId
                    }

                    val currentLikes = commentDoc.getLong("likesCount")?.toInt() ?: 0
                    val updatedLikesCount = if (isLiked) {
                        (currentLikes - 1).coerceAtLeast(0)
                    } else {
                        currentLikes + 1
                    }

                    transaction.updateData(mapOf("likedComments" to newLikedComments), userRef)
                    transaction.updateData(mapOf("likesCount" to updatedLikesCount), commentRef)
                }.await()

                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    } catch (e: Exception) {
        NSLog("Error toggling like on comment: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun toggleSoundLike(soundId: String): Result<Unit> = try {
        getCurrentUserId().fold(
            onSuccess = { uid ->
                val userRef = firestore.collection("users").document(uid)

                firestore.runTransaction { transaction, error ->
                    if (error != null) throw error

                    val userDoc = transaction.getDocument(userRef)
                    val likedSounds = userDoc.get("likedSounds") as? List<*> ?: listOf<String>()
                    val isLiked = likedSounds.contains(soundId)

                    val updatedLikedSounds = if (isLiked) {
                        likedSounds - soundId
                    } else {
                        likedSounds + soundId
                    }

                    transaction.updateData(mapOf("likedSounds" to updatedLikedSounds), userRef)
                }.await()

                Result.success(Unit)
            },
            onFailure = { Result.failure(it) }
        )
    } catch (e: Exception) {
        NSLog("Error toggling sound like: ${e.localizedMessage}")
        Result.failure(e)
    }
}