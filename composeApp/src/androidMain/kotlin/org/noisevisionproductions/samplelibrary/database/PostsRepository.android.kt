package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory
import java.util.UUID

actual class PostsRepository actual constructor(
    private val forumRepository: ForumRepository
) {
    private val firestore = Firebase.firestore
    private val postsCollection = firestore.collection("posts")

    actual suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val postId = UUID.randomUUID().toString()
        val postModel = PostModel(
            postId = postId,
            userId = userId,
            username = username,
            title = title,
            content = content,
            categoryId = categoryId
        )
        try {
            firestore.runTransaction { transaction ->
                val userDocRef = firestore.collection("users").document(userId)
                val userSnapshot = transaction.get(userDocRef)

                val currentPostIds = userSnapshot.get("postIds") as? List<*> ?: emptyList<String>()
                val updatedPostIds = currentPostIds + postId

                transaction.set(postsCollection.document(postId), postModel)
                transaction.update(userDocRef, "postIds", updatedPostIds)
                null
            }.await()

            Result.success(true)
        } catch (e: Exception) {
            Log.e("CreatePost", "Error creating post and updating user post IDs", e)
            Result.failure(e)
        }
    }

    actual suspend fun getPostsFromFirestore(
        lastPostId: String?,
        selectedCategoryId: String?,
        selectedSortingOption: String?
    ): Result<List<PostWithCategory>> = withContext(Dispatchers.IO) {
        try {
            var query: Query = postsCollection

            selectedCategoryId?.let { categoryId ->
                query = query.whereEqualTo("categoryId", categoryId)
            }

            query = when (selectedSortingOption) {
                "Najnowsze" -> query.orderBy("timestamp", Query.Direction.DESCENDING)
                "Najstarsze" -> query.orderBy("timestamp", Query.Direction.ASCENDING)
                else -> query.orderBy("timestamp", Query.Direction.DESCENDING)  // Default sorting
            }

            lastPostId?.let { lastId ->
                val lastDocument = postsCollection.document(lastId).get().await()
                if (lastDocument.exists()) {
                    query = query.startAfter(lastDocument)
                }
            }

            val querySnapshot = query.limit(10).get().await()
            val postList = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PostModel::class.java)?.copy(postId = document.id)
            }

            val categoryIds = postList.map { it.categoryId }.distinct()
            val categoryResult = forumRepository.getCategoryNames(categoryIds)

            val postsWithCategories = categoryResult.fold(
                onSuccess = { categoryMap ->
                    postList.map { post ->
                        PostWithCategory(
                            post = post,
                            categoryName = categoryMap[post.categoryId]?.name ?: "Unknown Category"
                        )
                    }
                },
                onFailure = { exception ->
                    Log.e("FirestoreError", "Error getting categories", exception)
                    postList.map { post ->
                        PostWithCategory(
                            post = post,
                            categoryName = "Unknown Category"
                        )
                    }
                }
            )

            Result.success(postsWithCategories)
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting posts: ", e)
            Result.failure(e)
        }
    }

    actual suspend fun getPost(postId: String): Result<PostModel> =
        withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = postsCollection.document(postId).get().await()
                if (documentSnapshot.exists()) {
                    documentSnapshot.toObject<PostModel>()?.copy(postId = documentSnapshot.id)
                        ?.let { Result.success(it) }
                        ?: Result.failure(Exception("Failed to convert document to PostModel"))
                } else {
                    Result.failure(Exception("Post not found"))
                }
            } catch (e: Exception) {
                Log.e("GetPost", "Error getting post with ID $postId", e)
                Result.failure(e)
            }
        }
}