package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

actual class ForumRepository actual constructor() {

    actual suspend fun getCategories(onCategoriesLoaded: (List<CategoryModel>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categories = result.map { document ->
                    document.toObject(CategoryModel::class.java)
                }
                onCategoriesLoaded(categories)
            }
            .addOnFailureListener { e ->
                Log.w("GetCategories", "Error getting categories", e)
            }
    }

    actual suspend fun getCategoryName(categoryId: String): String {
        return try {
            val documentSnapshot = FirebaseFirestore.getInstance()
                .collection("categories")
                .document(categoryId)
                .get()
                .await()

            val category = documentSnapshot.toObject(CategoryModel::class.java)
            category?.name ?: "Unknown Category"
        } catch (e: Exception) {
            Log.w("GetCategoryName", "Error getting category name", e)
            "Unknown Category"
        }
    }

    actual suspend fun getCategoryNames(categoryIds: List<String>): Map<String, CategoryModel> {
        val categoryMap = mutableMapOf<String, CategoryModel>()
        val categoriesCollection = FirebaseFirestore.getInstance().collection("categories")

        if (categoryIds.isEmpty()) {
            val querySnapshot = categoriesCollection.get().await()
            for (document in querySnapshot.documents) {
                val category = document.toObject(CategoryModel::class.java)
                if (category != null) {
                    categoryMap[category.id] = category
                }
            }
        } else {
            val chunks = categoryIds.distinct().chunked(10)

            val tasks = chunks.map { chunk ->
                categoriesCollection
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
            }
            val results = tasks.map { it.await() }

            results.forEach { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val category = document.toObject(CategoryModel::class.java)
                    if (category != null) {
                        categoryMap[category.id] = category
                    }
                }
            }
        }

        return categoryMap
    }


}