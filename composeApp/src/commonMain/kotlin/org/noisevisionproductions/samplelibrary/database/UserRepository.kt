package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

expect class UserRepository() {
    suspend fun getCurrentUserId(): Result<String>
    suspend fun getCurrentUser(): Result<UserModel?>
    suspend fun getUsernameById(userId: String): Result<String?>
    suspend fun getUserLabelById(userId: String): Result<String?>
    suspend fun getCurrentUserAvatarPath(): Result<String?>
    suspend fun updateAvatarUrl(url: String): Result<Unit>
    suspend fun getPostsByIds(postIds: List<String>): Result<List<PostModel>>
    suspend fun getLikedPosts(): Result<List<PostModel>>
    suspend fun removeLikedPost(postId: String): Result<Unit>
    suspend fun getLikedSounds(): Result<List<String>>
}