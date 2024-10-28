package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes

expect class LikeService() {
    suspend fun toggleLikePost(postId: String): Result<Boolean>
    suspend fun isPostLiked(postId: String): Boolean
    suspend fun isCommentLiked(commentId: String): Boolean
    suspend fun toggleLikeComment(commentId: String): Result<Unit>
}