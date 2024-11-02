package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

// In iosMain

import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRDocumentSnapshot
import cocoapods.FirebaseFirestore.FIRQuery
import kotlinx.coroutines.await
import platform.Foundation.NSLog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class ForumRepository actual constructor() {
    private val firestore: FIRFirestore = FIRFirestore.firestore()

    actual suspend fun getCategories(): Result<List<CategoryModel>> = try {
        val querySnapshot = firestore.collection("categories").getDocuments().await()
        val categories = querySnapshot.documents.mapNotNull { it.toCategoryModel() }
        Result.success(categories)
    } catch (e: Exception) {
        NSLog("Error getting categories: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun getCategoryName(categoryId: String): Result<String> = try {
        val documentSnapshot =
            firestore.collection("categories").document(categoryId).getDocument().await()
        val categoryName = documentSnapshot.toCategoryModel()?.name ?: "Nieznana kategoria"
        Result.success(categoryName)
    } catch (e: Exception) {
        NSLog("Error getting category name: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun getCategoryNames(categoryIds: List<String>): Result<Map<String, CategoryModel>> =
        try {
            val categoryMap = mutableMapOf<String, CategoryModel>()
            val categoriesCollection = firestore.collection("categories")

            if (categoryIds.isEmpty()) {
                val querySnapshot = categoriesCollection.getDocuments().await()
                for (document in querySnapshot.documents) {
                    document.toCategoryModel()?.let { categoryMap[it.id] = it }
                }
            } else {
                categoryIds.distinct().chunked(10).forEach { chunk ->
                    val querySnapshot = categoriesCollection
                        .whereField(FieldPath.documentId(), arrayOf(*chunk.toTypedArray()))
                        .getDocuments()
                        .await()

                    for (document in querySnapshot.documents) {
                        document.toCategoryModel()?.let { categoryMap[it.id] = it }
                    }
                }
            }

            Result.success(categoryMap)
        } catch (e: Exception) {
            NSLog("Error getting category names: ${e.localizedMessage}")
            Result.failure(e)
        }
}

// Extension functions to map Firestore documents to CategoryModel
private fun FIRDocumentSnapshot.toCategoryModel(): CategoryModel? {
    // Implement mapping from FIRDocumentSnapshot to CategoryModel
    // Ensure you have a constructor or mapping logic for CategoryModel
    return try {
        CategoryModel(
            id = this.documentID,
            name = this.getString("name") ?: "Unknown"
        )
    } catch (e: Exception) {
        NSLog("Error mapping document ${this.documentID} to CategoryModel: ${e.localizedMessage}")
        null
    }
}
