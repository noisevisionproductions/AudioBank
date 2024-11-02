package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.errors.AppError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.NetworkUtils
import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

actual class CommentRepository actual constructor() {

    private val firestore = FirebaseFirestore.getInstance()

    actual suspend fun addCommentToPost(
        postId: String,
        comment: CommentModel
    ): Result<Boolean> =
        executeWithNetworkCheck {
            val newComment = initializeNewComment(comment, postId)
            firestore.collection("comments").document(newComment.commentId).set(newComment).await()
            Result.success(true)
        }

    actual suspend fun addReplyToComment(
        postId: String,
        parentCommentId: String,
        reply: CommentModel
    ): Result<Unit> = executeWithNetworkCheck {
        val newReply = initializeNewReply(reply, postId, parentCommentId)
        firestore.collection("comments").document(newReply.commentId).set(newReply).await()
        Result.success(Unit)
    }


    actual suspend fun getCommentsForPost(postId: String): List<CommentModel> {
        return try {
            firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(CommentModel::class.java) }
                .let { buildCommentTree(it) }
        } catch (e: Exception) {
            println(e)
            emptyList()
        }
    }

    private fun initializeNewComment(comment: CommentModel, postId: String): CommentModel {
        val commentId = firestore.collection("comments").document().id
        return comment.copy(commentId = commentId, postId = postId)
    }

    private fun initializeNewReply(
        reply: CommentModel,
        postId: String,
        parentCommentId: String
    ): CommentModel {
        val replyId = firestore.collection("comments").document().id
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
        is FirebaseFirestoreException -> AppError.ApiError(
            message = "Błąd podczas operacji na komentarzu",
            errorCode = e.code.toString(),
            statusCode = -1,
            technicalDetails = e.stackTraceToString()
        )

        else -> AppError.UnexpectedError(
            throwable = e,
            message = "Nieoczekiwany błąd podczas operacji na komentarzu"
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
                if (parentComment != null) {
                    parentComment.replies += comment
                }
            }
        }
        return rootComments
    }
}