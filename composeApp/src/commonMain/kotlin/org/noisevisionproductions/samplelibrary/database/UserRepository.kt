package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel

expect class UserRepository() {
    suspend fun getCurrentUserId(): String?
    suspend fun getCurrentUser(): UserModel?
    suspend fun getUsernameById(userId: String): String?
    suspend fun getUserLabelById(userId: String): String?
    suspend fun updateAvatarUrl(url: String)
    suspend fun getCurrentUserAvatarPath(): String?
    suspend fun getLikedPosts(): Result<List<PostModel>>
    suspend fun removeLikedPost(postId: String)
    suspend fun getPostsByIds(postIds: List<String>): List<PostModel>
    suspend fun getLikedSounds(): List<String>
}