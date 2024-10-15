package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

expect class ForumService() {
    suspend fun getCategories(onCategoriesLoaded: (List<CategoryModel>) -> Unit)
    suspend fun getCategoryName(categoryId: String): String
    suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
        onPostCreated: (Boolean) -> Unit,
    )

    suspend fun getPostsFromFirestore(): Result<List<PostModel>>
}