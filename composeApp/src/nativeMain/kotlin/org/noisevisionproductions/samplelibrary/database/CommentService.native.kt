package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

actual class CommentService {
    actual suspend fun addCommentToPost(
        postId: String,
        comment: CommentModel
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    actual suspend fun addReplyToComment(
        postId: String,
        commentId: String,
        reply: CommentModel
    ): Result<Boolean> {
        TODO("Not yet implemented")
    }

    actual suspend fun getCommentsForPost(postId: String): List<CommentModel> {
        TODO("Not yet implemented")
    }

}