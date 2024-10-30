package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory
import samplelibrary.composeapp.generated.resources.Res

expect class ForumRepository() {
    suspend fun getCategories(onCategoriesLoaded: (List<CategoryModel>) -> Unit)
    suspend fun getCategoryName(categoryId: String): String
    suspend fun getCategoryNames(categoryIds: List<String>): Map<String, CategoryModel>
}