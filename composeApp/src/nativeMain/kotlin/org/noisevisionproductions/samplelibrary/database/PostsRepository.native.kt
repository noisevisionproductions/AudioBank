package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

// In iosMain

import cocoapods.FirebaseFirestore.FIRFirestore
import cocoapods.FirebaseFirestore.FIRTransaction
import cocoapods.FirebaseFirestore.FIRQuery
import kotlinx.coroutines.await
import platform.Foundation.NSLog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class PostsRepository actual constructor(
    private val forumRepository: ForumRepository
) {
    private val firestore = FIRFirestore.firestore()
    private val postsCollection = firestore.collection("posts")

    actual suspend fun createPost(
        title: String,
        content: String,
        username: String,
        categoryId: String,
        userId: String,
    ): Result<Boolean> = try {
        val postId = UUID().UUIDString() // Generate a UUID for iOS
        val postModel = PostModel(
            postId = postId,
            userId = userId,
            username = username,
            title = title,
            content = content,
            categoryId = categoryId
        )

        firestore.runTransaction { transaction, error ->
            if (error != null) throw error

            val userDocRef = firestore.collection("users").document(userId)
            val userSnapshot = transaction.getDocument(userDocRef)

            val currentPostIds = userSnapshot.get("postIds") as? List<*> ?: listOf<String>()
            val updatedPostIds = currentPostIds + postId

            transaction.setData(postModel.toMap(), postsCollection.document(postId))
            transaction.updateData(mapOf("postIds" to updatedPostIds), userDocRef)
            null
        }.await()

        Result.success(true)
    } catch (e: Exception) {
        NSLog("Error creating post and updating user post IDs: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun getPostsFromFirestore(
        lastPostId: String?,
        selectedCategoryId: String?,
        selectedSortingOption: String?
    ): Result<List<PostWithCategory>> = try {
        var query: FIRQuery = postsCollection

        selectedCategoryId?.let { categoryId ->
            query = query.whereField("categoryId", equalTo = categoryId)
        }

        query = when (selectedSortingOption) {
            "Najnowsze" -> query.orderBy("timestamp", FIRQuerySortDirectionDescending)
            "Najstarsze" -> query.orderBy("timestamp", FIRQuerySortDirectionAscending)
            else -> query.orderBy("timestamp", FIRQuerySortDirectionDescending)
        }

        lastPostId?.let { lastId ->
            val lastDocument = postsCollection.document(lastId).getDocument().await()
            if (lastDocument.exists()) {
                query = query.start(afterDocument = lastDocument)
            }
        }

        val querySnapshot = query.limit(to = 10).getDocuments().await()
        val postList = querySnapshot.documents.mapNotNull { document ->
            document.toPostModel()?.copy(postId = document.documentID)
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
                NSLog("Error getting categories: ${exception.localizedMessage}")
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
        NSLog("Error getting posts: ${e.localizedMessage}")
        Result.failure(e)
    }

    actual suspend fun getPost(postId: String): Result<PostModel> = try {
        val documentSnapshot = postsCollection.document(postId).getDocument().await()
        if (documentSnapshot.exists()) {
            documentSnapshot.toPostModel()?.copy(postId = documentSnapshot.documentID)
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Failed to convert document to PostModel"))
        } else {
            Result.failure(Exception("Post not found"))
        }
    } catch (e: Exception) {
        NSLog("Error getting post with ID $postId: ${e.localizedMessage}")
        Result.failure(e)
    }
}

// Extension functions to map Firestore documents to PostModel
private fun FIRDocumentSnapshot.toPostModel(): PostModel? {
    // Implement mapping from FIRDocumentSnapshot to PostModel
    // Ensure you have a constructor or mapping logic for PostModel
    return try {
        PostModel(
            postId = this.documentID,
            userId = this.getString("userId") ?: "",
            username = this.getString("username") ?: "Unknown",
            title = this.getString("title") ?: "",
            content = this.getString("content") ?: "",
            categoryId = this.getString("categoryId") ?: ""
        )
    } catch (e: Exception) {
        NSLog("Error mapping document ${this.documentID} to PostModel: ${e.localizedMessage}")
        null
    }
}

private fun PostModel.toMap(): Map<String, Any> {
    return mapOf(
        "postId" to postId,
        "userId" to userId,
        "username" to username,
        "title" to title,
        "content" to content,
        "categoryId" to categoryId,
        // Add other fields if necessary
    )
}
