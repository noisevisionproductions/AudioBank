package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.auth.AuthService

expect class LikeRepository(
    authService: AuthService = AuthService()
) {
    suspend fun toggleLikePost(postId: String): Result<Boolean>
    suspend fun getPostLikesCount(postId: String): Result<Int>
    suspend fun isPostLiked(postId: String): Result<Boolean>
    suspend fun isCommentLiked(commentId: String): Result<Boolean>
    suspend fun toggleLikeComment(commentId: String): Result<Unit>
    suspend fun toggleSoundLike(soundId: String): Result<Unit>
}