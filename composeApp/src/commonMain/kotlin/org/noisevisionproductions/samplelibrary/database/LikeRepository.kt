package org.noisevisionproductions.samplelibrary.database


expect class LikeRepository() {
    suspend fun toggleLikePost(postId: String): Result<Boolean>
    suspend fun isPostLiked(postId: String): Boolean
    suspend fun getPostLikesCount(postId: String): Result<Int>
    suspend fun isCommentLiked(commentId: String): Boolean
    suspend fun toggleLikeComment(commentId: String): Result<Unit>
    suspend fun toggleSoundLike(soundId: String)
}