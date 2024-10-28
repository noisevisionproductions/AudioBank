package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

actual class ForumRepository {
    actual suspend fun getCategories(onCategoriesLoaded: (List<CategoryModel>) -> Unit) {
    }
}