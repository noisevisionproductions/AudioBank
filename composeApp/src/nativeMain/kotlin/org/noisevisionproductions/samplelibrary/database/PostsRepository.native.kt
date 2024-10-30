package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

actual class PostsRepository {
    actual suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
        onPostCreated: (Boolean) -> Unit
    ) {
    }

    actual suspend fun getPostsFromFirestore(
        lastPostId: String?,
        selectedCategoryId: String?,
        selectedSortingOption: String?
    ): Result<List<PostWithCategory>> {
        TODO("Not yet implemented")
    }

    actual suspend fun getPost(postId: String): Result<PostModel> {
        TODO("Not yet implemented")
    }

}