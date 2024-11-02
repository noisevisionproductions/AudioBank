package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory
import samplelibrary.composeapp.generated.resources.Res

expect class ForumRepository() {
    suspend fun getCategories(): Result<List<CategoryModel>>
    suspend fun getCategoryName(categoryId: String): Result<String>
    suspend fun getCategoryNames(categoryIds: List<String>): Result<Map<String, CategoryModel>>
}