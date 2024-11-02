package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

expect class PostsRepository(
    forumRepository: ForumRepository = ForumRepository()
) {
    suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
    ): Result<Boolean>

    suspend fun getPostsFromFirestore(
        lastPostId: String? = null,
        selectedCategoryId: String?,
        selectedSortingOption: String?
    ): Result<List<PostWithCategory>>

    suspend fun getPost(postId: String): Result<PostModel>
}