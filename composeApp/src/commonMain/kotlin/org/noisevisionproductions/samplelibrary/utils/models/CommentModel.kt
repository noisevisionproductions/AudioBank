package org.noisevisionproductions.samplelibrary.utils.models

data class CommentModel(
    val commentId: String = "",
    val postId: String = "",
    val parentCommentId: String? = null,
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: String = "",
    var replies: List<CommentModel> = emptyList()
)
