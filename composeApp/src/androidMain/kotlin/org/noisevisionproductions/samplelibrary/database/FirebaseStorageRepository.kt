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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

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
            val storageReference = storage.reference.child("samples")
            Log.d("FirebaseStorageService", "Listing files in directory: $directoryPath")

            // Fetch user directories under "samples"
            val foldersResult = if (continuationToken != null) {
                storageReference.list(maxResultsPerPage, continuationToken).await()
            } else {
                storageReference.list(maxResultsPerPage).await()
            }

            Log.d("FirebaseStorageService", "Found ${foldersResult.prefixes.size} user folders")

            // Parallel fetching of file metadata
            val folderDataList = coroutineScope {
                foldersResult.prefixes.flatMap { userFolderRef ->
                    userFolderRef.listAll().await().prefixes.mapNotNull { soundFolderRef ->
                        async {
                            try {
                                val soundId = soundFolderRef.name  // Use folder name as soundId
                                val fileRef = soundFolderRef.list(1).await().items.firstOrNull()
                                    ?: return@async null

                                Pair(fileRef, soundId)
                            } catch (e: Exception) {
                                Log.e(
                                    "FirebaseStorageService",
                                    "Error processing folder ${soundFolderRef.path}",
                                    e
                                )
                                null
                            }
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            // Collect Firestore Doc IDs from folder names directly
            val firestoreDocIds = folderDataList.map { it.second }.distinct()

            // Fetch metadata in batch from Firestore
            val firestoreMetadata = if (firestoreDocIds.isNotEmpty()) {
                firestore.collection("audio_metadata")
                    .whereIn(FieldPath.documentId(), firestoreDocIds)
                    .get()
                    .await()
                    .associateBy { it.id }
            } else {
                emptyMap()
            }
            Log.d("FirebaseStorageService", "Fetched ${firestoreMetadata.size} metadata records")

            // Build AudioMetadata objects
            val audioMetadataList = folderDataList.mapNotNull { (fileRef, soundId) ->
                try {
                    val firestoreDoc = firestoreMetadata[soundId]
                    val url = urlCache.getOrPut(soundId) {
                        fileRef.downloadUrl.await().toString()
                    }

                    firestoreDoc?.let {
                        AudioMetadata(
                            id = soundId,
                            url = url,
                            fileName = it.getString("file_name") ?: "",
                            duration = it.getString("duration") ?: "",
                            bpm = it.getString("bpm"),
                            tone = it.getString("tone"),
                            tags = (it.get("tags") as? List<*>)?.mapNotNull { tag -> tag as? String }
                                ?: emptyList()
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
    ): Result<AudioMetadata> = withContext(Dispatchers.IO) {
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

            // Upload file and get upload task result
            val uploadTask = suspendCancellableCoroutine { continuation ->
                val task = storageReference.putBytes(fileData, storageMetadata)

                task.addOnProgressListener { taskSnapshot ->
                    val progress = taskSnapshot.bytesTransferred.toFloat() /
                            taskSnapshot.totalByteCount.toFloat()
                    onProgress(progress)
                }

                task.addOnSuccessListener {
                    continuation.resume(it)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }

                continuation.invokeOnCancellation {
                    task.cancel()
                }
            }

            // Get download URL
            val downloadUrl = suspendCoroutine<String> { continuation ->
                storageReference.downloadUrl
                    .addOnSuccessListener { url ->
                        continuation.resume(url.toString())
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }

            // Create Firestore document with complete metadata
            val timestamp = System.currentTimeMillis().toString()
            val firestoreMetadata = hashMapOf(
                "id" to uniqueId,
                "file_name" to fileName,
                "username" to username,
                "file_extension" to fileName.substringAfterLast("."),
                "duration" to (metadata.duration ?: ""),
                "url" to downloadUrl,
                "timestamp" to timestamp,
                "bpm" to (metadata.bpm ?: ""),
                "tone" to (metadata.tone ?: ""),
                "tags" to metadata.tags,
                "content_type" to storageMetadata.contentType
            )

            // Save to Firestore
            suspendCoroutine { continuation ->
                firestore.collection("audio_metadata")
                    .document(uniqueId)
                    .set(firestoreMetadata)
                    .addOnSuccessListener {
                        continuation.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
            }

            // Return complete AudioMetadata object
            Result.success(
                AudioMetadata(
                    id = uniqueId,
                    fileName = fileName,
                    fileExtension = fileName.substringAfterLast("."),
                    duration = metadata.duration ?: "",
                    url = downloadUrl,
                    timestamp = timestamp,
                    contentType = storageMetadata.contentType ?: "",
                    bpm = metadata.bpm ?: "",
                    tone = metadata.tone ?: "",
                    tags = metadata.tags,
                    isLiked = false
                )
            )

        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error uploading file: ${e.message}", e)
            Result.failure(e)
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

    actual suspend fun getSoundMetadata(soundId: String): AudioMetadata {
        val documentSnapshot = firestore.collection("audio_metadata")
            .document(soundId)
            .get()
            .await()

        if (documentSnapshot.exists()) {
            return AudioMetadata(
                id = documentSnapshot.id,
                fileName = documentSnapshot.getString("file_name") ?: "",
                url = documentSnapshot.getString("url") ?: "",
                duration = documentSnapshot.getString("duration") ?: "",
                bpm = documentSnapshot.getString("bpm"),
                tone = documentSnapshot.getString("tone"),
                tags = (documentSnapshot.get("tags") as? List<*>)?.mapNotNull { it as? String }
                    ?: emptyList(),
                timestamp = documentSnapshot.getString("timestamp") ?: ""
            )
        } else {
            throw Exception("Document not found for ID: $soundId")
        }
    }

    actual suspend fun getSoundsMetadataByIds(soundIds: List<String>): Result<List<AudioMetadata>> {
        return try {
            if (soundIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val querySnapshot = firestore.collection("audio_metadata")
                .whereIn(FieldPath.documentId(), soundIds)
                .get()
                .await()

            val metadataList = querySnapshot.documents.mapNotNull { document ->
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

            Result.success(metadataList)
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error fetching sound metadata for IDs", e)
            Result.failure(e)
        }
    }

}