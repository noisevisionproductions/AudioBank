package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

actual class LikeService {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val authService = AuthService()

    actual suspend fun toggleLikePost(postId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            val uid = authService.getCurrentUserId()
            if (uid != null) {
                try {
                    // Transaction code to toggle like on a post
                    Result.success(true)
                } catch (e: Exception) {
                    Log.e("LikeService", "Error toggling like on post: ${e.message}", e)
                    Result.failure(e)
                }
            } else {
                Result.failure(Exception("User not logged in"))
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
}