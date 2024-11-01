package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory
import java.util.UUID

actual class PostsRepository {
    private val firestore = Firebase.firestore
    private val postsCollection = firestore.collection("posts")
    private val forumRepository = ForumRepository()

    actual suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
        onPostCreated: (Boolean) -> Unit,
    ) {
        val postId = UUID.randomUUID().toString()
        val postModel = PostModel(
            postId = postId,
            userId = userId,
            username = username,
            title = title,
            content = content,
            categoryId = categoryId
        )

        val firestore = FirebaseFirestore.getInstance()
        val postDocRef = firestore.collection("posts").document(postId)
        val userDocRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val userSnapshot = transaction.get(userDocRef)

            val currentPostIds =
                (userSnapshot.get("postIds") as? List<*>)?.filterIsInstance<String>()
                    ?: emptyList()
            val updatedPostIds = currentPostIds + postId

            transaction.set(postDocRef, postModel)
            transaction.update(userDocRef, "postIds", updatedPostIds)

            null
        }.addOnSuccessListener {
            onPostCreated(true)
        }.addOnFailureListener { e ->
            onPostCreated(false)
            Log.e("CreatePost", "Error creating post and updating user post IDs", e)
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
                else -> query
            }

            if (selectedSortingOption == null) {
                query = query.orderBy("timestamp", Query.Direction.DESCENDING)
            }

            if (lastPostId != null) {
                val lastDocument = postsCollection.document(lastPostId).get().await()
                if (lastDocument.exists()) {
                    query = query.startAfter(lastDocument)
                }
            }

            query = query.limit(10)

            val querySnapshot = query.get().await()
            val postList = querySnapshot.documents.mapNotNull { document ->
                document.toObject(PostModel::class.java)?.copy(postId = document.id)
            }

            val categoryIds = postList.map { it.categoryId }.distinct()
            val categoryMap = forumRepository.getCategoryNames(categoryIds)

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

    actual suspend fun getPost(postId: String): Result<PostModel> = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = postsCollection.document(postId).get().await()
            if (documentSnapshot.exists()) {
                val post =
                    documentSnapshot.toObject<PostModel>()?.copy(postId = documentSnapshot.id)
                post?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Failed to convert document to PostModel"))
            } else {
                Result.failure(Exception("Post not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}