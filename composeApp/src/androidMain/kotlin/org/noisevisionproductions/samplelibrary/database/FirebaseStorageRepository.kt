package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata
import java.io.File
import java.io.FileNotFoundException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

actual class FirebaseStorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val maxResultsPerPage = 50

    // Cache dla URL-i
    private val urlCache = ConcurrentHashMap<String, String>()

    actual suspend fun listFilesWithMetadata(
        directoryPath: String,
        continuationToken: String?
    ): Pair<List<AudioMetadata>, String?> {
        return try {
            val storageReference = storage.reference.child(directoryPath)
            Log.d("FirebaseStorageService", "Listing files in directory: $directoryPath")

            // Fetch folders
            val foldersResult = if (continuationToken != null) {
                storageReference.list(maxResultsPerPage, continuationToken).await()
            } else {
                storageReference.list(maxResultsPerPage).await()
            }

            Log.d("FirebaseStorageService", "Found ${foldersResult.prefixes.size} folders")

            // Parallel fetching of file metadata
            val folderDataList = coroutineScope {
                foldersResult.prefixes.map { folderRef ->
                    async {
                        try {
                            val filesInFolder = folderRef.list(1).await()
                            val fileRef = filesInFolder.items.firstOrNull() ?: return@async null

                            // Fetch file metadata to get 'firestore_doc_id'
                            val metadata = fileRef.metadata.await()
                            val firestoreDocId = metadata.getCustomMetadata("firestore_doc_id")

                            firestoreDocId?.let { Pair(fileRef, it) }
                        } catch (e: Exception) {
                            Log.e(
                                "FirebaseStorageService",
                                "Error processing folder ${folderRef.path}",
                                e
                            )
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            // Collect all Firestore Doc IDs
            val firestoreDocIds = folderDataList.map { it.second }.distinct()

            // Fetch all Firestore documents in one batch
            val firestoreMetadata = firestore.collection("audio_metadata")
                .whereIn(FieldPath.documentId(), firestoreDocIds)
                .get()
                .await()
                .associateBy { it.id }

            Log.d("FirebaseStorageService", "Fetched ${firestoreMetadata.size} metadata records")

            // Build AudioMetadata objects
            val audioMetadataList = folderDataList.mapNotNull { (fileRef, firestoreDocId) ->
                try {
                    val firestoreDoc = firestoreMetadata[firestoreDocId]
                    val url = urlCache.getOrPut(firestoreDocId) {
                        fileRef.downloadUrl.await().toString()
                    }

                    if (firestoreDoc != null) {
                        AudioMetadata(
                            url = url,
                            fileName = firestoreDoc.getString("file_name") ?: "",
                            duration = firestoreDoc.getString("duration") ?: "",
                            bpm = firestoreDoc.getString("bpm"),
                            tone = firestoreDoc.getString("tone"),
                            tags = (firestoreDoc.get("tags") as? List<*>)?.mapNotNull { it as? String }
                                ?: emptyList()
                        )
                    } else {
                        // Fallback if Firestore document is not found
                        AudioMetadata(
                            url = url,
                            fileName = fileRef.name,
                            duration = ""
                        )
                    }
                } catch (e: Exception) {
                    Log.e(
                        "FirebaseStorageService",
                        "Error building AudioMetadata for ${fileRef.path}",
                        e
                    )
                    null
                }
            }

            Log.d(
                "FirebaseStorageService",
                "Returning ${audioMetadataList.size} audio metadata items"
            )
            Pair(audioMetadataList, foldersResult.pageToken)
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error listing files", e)
            e.printStackTrace()
            Pair(emptyList(), null)
        }
    }

    actual suspend fun uploadImage(filePath: String): String = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                throw FileNotFoundException("File not found at path: $filePath")
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw Exception("User not logged in")

            val storageRef = storage.reference.child("avatars/$userId/avatar.jpg")
            val downloadUrl = storageRef.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseStorageRepository", "Error uploading avatar: ${e.message}", e)
            throw e
        }
    }

    actual suspend fun uploadSoundsToStorage(
        username: String,
        fileName: String,
        fileData: ByteArray,
        metadata: AudioMetadata,
        onProgress: (Float) -> Unit
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        try {
            val uniqueId = UUID.randomUUID().toString()
            val storageReference = storage.reference.child("samples/$username/$uniqueId/$fileName")

            val storageMetadata = StorageMetadata.Builder()
                .setContentType("audio/${fileName.substringAfterLast(".")}")
                .setCustomMetadata("bpm", metadata.bpm ?: "")
                .setCustomMetadata("tone", metadata.tone ?: "")
                .setCustomMetadata("tags", metadata.tags.joinToString(","))
                .setCustomMetadata("firestore_doc_id", uniqueId)
                .build()

            val uploadTask = storageReference.putBytes(fileData, storageMetadata)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() /
                        taskSnapshot.totalByteCount.toFloat()
                onProgress(progress)
            }

            // Handle the upload and Firestore operations sequentially
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageReference.downloadUrl
            }.addOnSuccessListener { downloadUrl ->
                // Create Firestore document
                val firestoreMetadata = hashMapOf(
                    "file_name" to fileName,
                    "username" to username,
                    "file_extension" to fileName.substringAfterLast("."),
                    "duration" to metadata.duration,
                    "url" to downloadUrl.toString(),
                    "bpm" to (metadata.bpm ?: ""),
                    "tone" to (metadata.tone ?: ""),
                    "tags" to metadata.tags,
                    "timestamp" to System.currentTimeMillis().toString(),
                    "content_type" to storageMetadata.contentType
                )

                firestore.collection("audio_metadata")
                    .document(uniqueId)
                    .set(firestoreMetadata)
                    .addOnSuccessListener {
                        continuation.resume(Result.success(downloadUrl.toString()))
                    }
                    .addOnFailureListener { e ->
                        continuation.resume(Result.failure(e))
                    }
            }.addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }

            continuation.invokeOnCancellation {
                uploadTask.cancel()
            }
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error uploading file: ${e.message}", e)
            continuation.resume(Result.failure(e))
        }
    }

    actual suspend fun getUserSounds(
        username: String,
        limit: Int,
        lastDocumentId: String?
    ): Result<Pair<List<AudioMetadata>, String?>> {
        return try {
            val query = firestore.collection("audio_metadata")
                .whereEqualTo("username", username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())

            val finalQuery = lastDocumentId?.let { lastId ->
                val lastDoc = firestore.collection("audio_metadata")
                    .document(lastId)
                    .get()
                    .await()
                query.startAfter(lastDoc)
            } ?: query

            val querySnapshot = finalQuery.get().await()

            val userSounds = querySnapshot.documents.mapNotNull { document ->
                try {
                    AudioMetadata(
                        id = document.id,
                        fileName = document.getString("file_name") ?: "",
                        url = document.getString("url") ?: "",
                        duration = document.getString("duration") ?: "",
                        bpm = document.getString("bpm"),
                        tone = document.getString("tone"),
                        tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList(),
                        timestamp = document.getString("timestamp") ?: ""
                    )
                } catch (e: Exception) {
                    Log.e("FirebaseStorageService", "Error mapping document ${document.id}", e)
                    null
                }
            }

            val lastVisible = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last().id
            } else null

            Result.success(Pair(userSounds, lastVisible))
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error fetching user sounds", e)
            Result.failure(e)
        }
    }

    actual suspend fun updateSoundMetadata(
        soundId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection("audio_metadata")
                .document(soundId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error updating sound metadata", e)
            Result.failure(e)
        }
    }

    actual suspend fun deleteUserSound(
        username: String,
        soundId: String,
        fileName: String
    ): Result<Unit> {
        return try {
            val storagePath = "samples/$username/$soundId/$fileName"

            storage.reference.child(storagePath)
                .delete()
                .await()

            firestore.collection("audio_metadata")
                .document(soundId)
                .delete()
                .await()

            urlCache.remove(soundId)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error deleting sound", e)
            Result.failure(e)
        }
    }
}