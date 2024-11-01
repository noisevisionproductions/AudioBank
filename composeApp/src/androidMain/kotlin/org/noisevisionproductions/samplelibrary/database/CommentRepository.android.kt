package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.errors.AppError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.NetworkUtils
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

actual class CommentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    actual suspend fun addCommentToPost(
        postId: String,
        comment: CommentModel
    ): Result<Boolean> {
        return try {
            NetworkUtils().checkNetworkAvailabilityOrThrow()

            val commentsCollection = firestore.collection("comments")
            val newCommentRef = commentsCollection.document()
            val newComment = comment.copy(
                commentId = newCommentRef.id,
                postId = postId
            )
            newCommentRef.set(newComment).await()
            Result.success(true)
        } catch (e: Throwable) {
            val error = when (e) {
                is AppError -> e
                is FirebaseFirestoreException -> AppError.ApiError(
                    message = "Błąd podczas dodawania komentarza",
                    errorCode = e.code.toString(),
                    statusCode = -1,
                    technicalDetails = e.stackTraceToString()
                )

                else -> AppError.UnexpectedError(
                    throwable = e,
                    message = "Nieoczekiwany błąd podczas dodawania komentarza"
                )
            }
            Result.failure(error)
        }
    }

    actual suspend fun addReplyToComment(
        postId: String,
        parentCommentId: String,
        reply: CommentModel
    ): Result<Unit> {
        return try {
            NetworkUtils().checkNetworkAvailabilityOrThrow()

            val commentsCollection = firestore.collection("comments")
            val newReplyRef = commentsCollection.document()
            val newReply = reply.copy(
                commentId = newReplyRef.id,
                postId = postId,
                parentCommentId = parentCommentId
            )
            newReplyRef.set(newReply).await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    actual suspend fun getCommentsForPost(postId: String): List<CommentModel> {
        return try {
            val commentsSnapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()

            val commentsList = commentsSnapshot.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(CommentModel::class.java)
            }

            buildCommentTree(commentsList)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildCommentTree(comments: List<CommentModel>): List<CommentModel> {
        val commentMap = comments.associateBy { it.commentId }
        val rootComments = mutableListOf<CommentModel>()

        comments.forEach { comment ->
            if (comment.parentCommentId == null) {
                rootComments.add(comment)
            } else {
                val parentComment = commentMap[comment.parentCommentId]
                if (parentComment != null) {
                    parentComment.replies += comment
                }
            }
        }
        return rootComments
    }
}