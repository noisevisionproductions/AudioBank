package org.noisevisionproductions.samplelibrary.database
// In iosMain

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseFirestore.FIRFirestore
import kotlinx.coroutines.await
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.UserModel
import platform.Foundation.NSLog
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class UserRepository actual constructor() {

    private val firebaseAuth = FIRAuth.auth()
    private val firestore = FIRFirestore.firestore()

    private fun getCurrentUserIdOrError(): Result<String> {
        return firebaseAuth.currentUser?.uid?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("User not logged in"))
    }

    actual suspend fun getCurrentUserId(): Result<String> = getCurrentUserIdOrError()

    actual suspend fun getCurrentUser(): Result<UserModel?> = try {
        getCurrentUserIdOrError().fold(
            onSuccess = { uid ->
                val documentSnapshot = firestore.collection("users").document(uid).getDocument().await()
                Result.success(documentSnapshot.toUserModel())
            },
            onFailure = { error -> Result.failure(error) }
        )
    } catch (e: Exception) {
        handleError("Error fetching current user data", e)
    }

    actual suspend fun getUsernameById(userId: String): Result<String?> = try {
        val documentSnapshot = firestore.collection("users").document(userId).getDocument().await()
        val username = documentSnapshot.toUserModel()?.username
        Result.success(username)
    } catch (e: Exception) {
        handleError("Error fetching username", e)
    }

    actual suspend fun getUserLabelById(userId: String): Result<String?> = try {
        val documentSnapshot = firestore.collection("users").document(userId).getDocument().await()
        val label = documentSnapshot.toUserModel()?.label
        Result.success(label)
    } catch (e: Exception) {
        handleError("Error fetching user label", e)
    }

    actual suspend fun getCurrentUserAvatarPath(): Result<String?> = try {
        getCurrentUserIdOrError().fold(
            onSuccess = { uid ->
                val userDocument = firestore.collection("users").document(uid).getDocument().await()
                Result.success(userDocument.toUserModel()?.avatarUrl)
            },
            onFailure = { error -> Result.failure(error) }
        )
    } catch (e: Exception) {
        handleError("Error fetching avatar URL", e)
    }

    actual suspend fun updateAvatarUrl(url: String): Result<Unit> = try {
        getCurrentUserIdOrError().fold(
            onSuccess = { uid ->
                firestore.collection("users").document(uid).updateData(mapOf("avatarUrl" to url)).await()
                Result.success(Unit)
            },
            onFailure = { error -> Result.failure(error) }
        )
    } catch (e: Exception) {
        handleError("Error updating avatar URL", e)
    }

    actual suspend fun getPostsByIds(postIds: List<String>): Result<List<PostModel>> = try {
        val posts = postIds.map { postId ->
            firestore.collection("posts").document(postId).getDocument().await().toPostModel()
        }.filterNotNull()
        Result.success(posts)
    } catch (e: Exception) {
        handleError("Error fetching posts by IDs", e)
    }

    actual suspend fun getLikedPosts(): Result<List<PostModel>> = try {
        getCurrentUserIdOrError().fold(
            onSuccess = { uid ->
                val userDocument = firestore.collection("users").document(uid).getDocument().await()
                val likedPostIds = userDocument.toUserModel()?.likedPosts ?: emptyList()
                val likedPosts = likedPostIds.map { postId ->
                    firestore.collection("posts").document(postId).getDocument().await().toPostModel()
                }.filterNotNull()
                Result.success(likedPosts)
            },
            onFailure = { error -> Result.failure(error) }
        )
    } catch (e: Exception) {
        handleError("Error fetching liked posts", e)
    }

    actual suspend fun removeLikedPost(postId: String): Result<Unit> = try {
        getCurrentUserIdOrError().fold(
            onSuccess = { uid ->
                val userDocRef = firestore.collection("users").document(uid)
                val postDocRef = firestore.collection("posts").document(postId)

                firestore.runTransaction { transaction, error ->
                    if (error != null) throw error

                    val userSnapshot = transaction.getDocument(userDocRef)
                    val likedPosts = (userSnapshot.get("likedPosts") as? List<String>) ?: emptyList()
                    val updatedLikedPosts = likedPosts.filterNot { it == postId }

                    val postSnapshot = transaction.getDocument(postDocRef)
                    val currentLikes = postSnapshot.getLong("likesCount") ?: 0L

                    transaction.updateData(mapOf("likedPosts" to updatedLikedPosts), userDocRef)
                    transaction.updateData(mapOf("likesCount" to (currentLikes - 1).coerceAtLeast(0)), postDocRef)
                    null
                }.await()

                Result.success(Unit)
            },
            onFailure = { error -> Result.failure(error) }
        )
    } catch (e: Exception) {
        handleError("Error removing liked post", e)
    }

    actual suspend fun getLikedSounds(): Result<List<String>> = try {
        getCurrentUserIdOrError().fold(
            onSuccess = { uid ->
                val userDoc = firestore.collection("users").document(uid).getDocument().await()
                val likedSounds = userDoc.get("likedSounds") as? List<String> ?: emptyList()
                Result.success(likedSounds)
            },
            onFailure = { error -> Result.failure(error) }
        )
    } catch (e: Exception) {
        handleError("Error fetching liked sounds", e)
    }

    private fun <T> handleError(errorMessage: String, e: Exception): Result<T> {
        NSLog("$errorMessage: ${e.localizedMessage}")
        return Result.failure(e)
    }
}

// Extension functions to map Firestore documents to UserModel and PostModel
private fun FIRDocumentSnapshot.toUserModel(): UserModel? {
    // Mapping logic to convert FIRDocumentSnapshot to UserModel
    return try {
        UserModel(
            id = this.documentID,
            username = this.getString("username") ?: "Unknown",
            avatarUrl = this.getString("avatarUrl"),
            label = this.getString("label"),
            likedPosts = this.get("likedPosts") as? List<String> ?: emptyList(),
            likedSounds = this.get("likedSounds") as? List<String> ?: emptyList()
        )
    } catch (e: Exception) {
        NSLog("Error mapping document ${this.documentID} to UserModel: ${e.localizedMessage}")
        null
    }
}

private fun FIRDocumentSnapshot.toPostModel(): PostModel? {
    // Mapping logic to convert FIRDocumentSnapshot to PostModel
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
