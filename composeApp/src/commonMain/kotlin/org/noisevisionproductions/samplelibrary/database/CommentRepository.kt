package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

expect class CommentRepository() {
    suspend fun addCommentToPost(postId: String, comment: CommentModel): Result<Boolean>
    suspend fun addReplyToComment(
        postId: String,
        parentCommentId: String,
        reply: CommentModel
    ): Result<Unit>

    suspend fun getCommentsForPost(postId: String): List<CommentModel>

}