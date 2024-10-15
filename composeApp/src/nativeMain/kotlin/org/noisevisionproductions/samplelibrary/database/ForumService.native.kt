package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

actual class ForumService {
    actual suspend fun getCategories(onCategoriesLoaded: (List<CategoryModel>) -> Unit) {
    }
}