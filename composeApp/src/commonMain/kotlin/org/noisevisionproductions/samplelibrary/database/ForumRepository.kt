package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

expect class ForumRepository() {
    suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
        onPostCreated: (Boolean) -> Unit,
    )

    suspend fun getCategories(onCategoriesLoaded: (List<CategoryModel>) -> Unit)
    suspend fun getCategoryName(categoryId: String): String
    suspend fun getCategoryNames(categoryIds: List<String>): Map<String, CategoryModel>
    suspend fun getPostsFromFirestore(
        lastPostId: String? = null,
        selectedCategoryId: String?,
        selectedSortingOption: String?
    ): Result<List<PostWithCategory>>
}