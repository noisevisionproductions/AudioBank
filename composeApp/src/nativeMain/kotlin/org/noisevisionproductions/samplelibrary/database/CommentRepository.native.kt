package org.noisevisionproductions.samplelibrary.database
// In iosMain

import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRFirestoreException
import kotlinx.coroutines.await
import org.noisevisionproductions.samplelibrary.errors.AppError
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.NetworkUtils
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel
import platform.Foundation.NSLog

actual class CommentRepository actual constructor() {

    private val firestore = FIRFirestore.firestore()

    actual suspend fun addCommentToPost(
        postId: String,
        comment: CommentModel
    ): Result<Boolean> = executeWithNetworkCheck {
        val newComment = initializeNewComment(comment, postId)
        firestore.collection("comments").document(newComment.commentId).setData(newComment.toMap())
            .await()
        Result.success(true)
    }

    actual suspend fun addReplyToComment(
        postId: String,
        parentCommentId: String,
        reply: CommentModel
    ): Result<Unit> = executeWithNetworkCheck {
        val newReply = initializeNewReply(reply, postId, parentCommentId)
        firestore.collection("comments").document(newReply.commentId).setData(newReply.toMap())
            .await()
        Result.success(Unit)
    }

    actual suspend fun getCommentsForPost(postId: String): List<CommentModel> {
        return try {
            firestore.collection("comments")
                .whereField("postId", isEqualTo = postId)
                .getDocuments()
                .await()
                .documents
                .mapNotNull { it.data()?.toCommentModel() }
                .let { buildCommentTree(it) }
        } catch (e: Exception) {
            NSLog("Error fetching comments: ${e.localizedMessage}")
            emptyList()
        }
    }

    private fun initializeNewComment(comment: CommentModel, postId: String): CommentModel {
        val commentId = firestore.collection("comments").document().documentID
        return comment.copy(commentId = commentId, postId = postId)
    }

    private fun initializeNewReply(
        reply: CommentModel,
        postId: String,
        parentCommentId: String
    ): CommentModel {
        val replyId = firestore.collection("comments").document().documentID
        return reply.copy(commentId = replyId, postId = postId, parentCommentId = parentCommentId)
    }

    private suspend fun <T> executeWithNetworkCheck(action: suspend () -> Result<T>): Result<T> {
        return try {
            NetworkUtils().checkNetworkAvailabilityOrThrow()
            action()
        } catch (e: Throwable) {
            Result.failure(handleError(e))
        }
    }

    private fun handleError(e: Throwable): AppError = when (e) {
        is AppError -> e
        is FIRFirestoreException -> AppError.ApiError(
            message = "Error during comment operation",
            errorCode = e.code.toString(),
            statusCode = -1,
            technicalDetails = e.toString()
        )

        else -> AppError.UnexpectedError(
            throwable = e,
            message = "Unexpected error during comment operation"
        )
    }

    private fun buildCommentTree(comments: List<CommentModel>): List<CommentModel> {
        val commentMap = comments.associateBy { it.commentId }
        val rootComments = mutableListOf<CommentModel>()

        comments.forEach { comment ->
            if (comment.parentCommentId == null) {
                rootComments.add(comment)
            } else {
                val parentComment = commentMap[comment.parentCommentId]
                parentComment?.replies?.add(comment)
            }
        }
        return rootComments
    }
}
