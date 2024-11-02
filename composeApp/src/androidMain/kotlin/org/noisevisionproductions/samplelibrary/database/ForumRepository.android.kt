package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

actual class ForumRepository actual constructor() {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    actual suspend fun getCategories(): Result<List<CategoryModel>> = try {
        val querySnapshot = firestore.collection("categories").get().await()
        val categories =
            querySnapshot.documents.mapNotNull { it.toObject(CategoryModel::class.java) }
        Result.success(categories)
    } catch (e: Exception) {
        Log.e("ForumRepository", "Error getting categories", e)
        Result.failure(e)
    }

    actual suspend fun getCategoryName(categoryId: String): Result<String> = try {
        val documentSnapshot = firestore.collection("categories").document(categoryId).get().await()
        val categoryName =
            documentSnapshot.toObject(CategoryModel::class.java)?.name ?: "Nieznana kategoria"
        Result.success(categoryName)
    } catch (e: Exception) {
        Log.e("GetCategoryName", "Error getting category name", e)
        Result.failure(e)
    }


    actual suspend fun getCategoryNames(categoryIds: List<String>): Result<Map<String, CategoryModel>> =
        try {
            val categoryMap = mutableMapOf<String, CategoryModel>()
            val categoriesCollection = firestore.collection("categories")

            if (categoryIds.isEmpty()) {
                val querySnapshot = categoriesCollection.get().await()
                for (document in querySnapshot.documents) {
                    val category = document.toObject(CategoryModel::class.java)
                    if (category != null) {
                        categoryMap[category.id] = category
                    }
                }
            } else {
                categoryIds.distinct().chunked(10).forEach { chunk ->
                    val querySnapshot = categoriesCollection
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .await()

                    querySnapshot.documents.forEach { document ->
                        val category = document.toObject(CategoryModel::class.java)
                        if (category != null) {
                            categoryMap[category.id] = category
                        }
                    }
                }
            }

            Result.success(categoryMap)
        } catch (e: Exception) {
            Log.w("ForumRepository", "Error getting category names", e)
            Result.failure(e)
        }
}