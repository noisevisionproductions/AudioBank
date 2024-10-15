package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.azure.core.annotation.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import java.util.UUID

actual class ForumService actual constructor() {
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

    actual suspend fun getPostsFromFirestore(): Result<List<PostModel>> {
        val postList = mutableListOf<PostModel>()
        val firestore = FirebaseFirestore.getInstance()

        return try {
            val querySnapshot = firestore.collection("posts").get().await()
            for (document in querySnapshot.documents) {
                val post = document.toObject(PostModel::class.java)
                if (post != null) {
                    postList.add(post)
                }
            }
            Result.success(postList)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting posts: ", e)
            Result.failure(e)
        }
    }
}