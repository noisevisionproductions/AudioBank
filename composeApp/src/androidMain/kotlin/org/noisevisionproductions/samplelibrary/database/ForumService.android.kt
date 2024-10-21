package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory
import java.util.UUID

actual class ForumService actual constructor() {

    actual suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
        onPostCreated: (Boolean) -> Unit,
    ) {
        val postModel = PostModel(
            postId = UUID.randomUUID().toString(),
            userId = userId,
            username = username,
            title = title,
            content = content,
            categoryId = categoryId
        )

        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(postModel.postId)
            .set(postModel)
            .addOnSuccessListener {
                onPostCreated(true)
            }
            .addOnFailureListener { e ->
                onPostCreated(false)
                Log.w("CreatePost", "Error creating post", e)
            }
    }

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

    actual suspend fun getPostsFromFirestore(
        lastPostId: String?,
        selectedCategoryId: String?,
        selectedSortingOption: String?
    ): Result<List<PostWithCategory>> {
        val postList = mutableListOf<PostModel>()
        val firestore = FirebaseFirestore.getInstance()

        var query: Query = firestore.collection("posts")

        selectedCategoryId?.let { categoryId ->
            Log.d("CategoryFilter", "Filtering posts by category: $categoryId")
            query = query.whereEqualTo("categoryId", categoryId)
        }

        query = when (selectedSortingOption) {
            "Najnowsze" -> query.orderBy("timestamp", Query.Direction.DESCENDING)
            "Najstarsze" -> query.orderBy("timestamp", Query.Direction.ASCENDING)
            else -> query
        }

        query = query.limit(10)

        if (lastPostId != null) {
            val lastDocument = firestore.collection("posts")
                .document(lastPostId).get().await()
            query = query.startAfter(lastDocument)
        }

        return try {
            val querySnapshot = query.get().await()
            val categoryIds = mutableSetOf<String>()
            for (document in querySnapshot.documents) {
                val post = document.toObject(PostModel::class.java)
                post?.let {
                    postList.add(post)
                    categoryIds.add(it.categoryId)
                }
            }

            val categoryMap = getCategoryNames(categoryIds.toList())

            val postsWithCategories = postList.map { post ->
                PostWithCategory(
                    post = post,
                    categoryName = categoryMap[post.categoryId]?.name ?: "Unknown Category"
                )
            }
            Result.success(postsWithCategories)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting posts: ", e)
            Result.failure(e)
        }
    }
}