package org.noisevisionproductions.samplelibrary.database

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

actual class FirebaseStorageRepository actual constructor() {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val urlCache = ConcurrentHashMap<String, String>()

    companion object {
        private const val MAX_RESULT_PER_PAGE = 50
        private const val SAMPLES_PATH = "samples"
        private const val AVATAR_PATH = "avatars"
        private const val METADATA_COLLECTION = "audio_metadata"
    }

    private data class StorageFileInfo(
        val fileRef: StorageReference,
        val soundId: String
    )

    actual suspend fun listFilesWithMetadata(
        directoryPath: String,
        continuationToken: String?
    ): Result<Pair<List<AudioMetadata>, String?>> = withContext(Dispatchers.IO) {
        try {
            val storageReference = storage.reference.child(SAMPLES_PATH)

            val foldersResult = fetchUserDirectories(storageReference, continuationToken)

            val folderDataList = fetchFolderData(foldersResult.prefixes)

            val firestoreMetadata = fetchFirestoreMetadata(folderDataList)

            val audioMetadataList = buildAudioMetadataList(folderDataList, firestoreMetadata)

            Result.success(Pair(audioMetadataList, foldersResult.pageToken))
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error listing files", e)
            Result.failure(e)
        }
    }

    private suspend fun fetchUserDirectories(
        storageReference: StorageReference,
        continuationToken: String?
    ): ListResult {
        return if (continuationToken != null) {
            storageReference.list(MAX_RESULT_PER_PAGE, continuationToken).await()
        } else {
            storageReference.list(MAX_RESULT_PER_PAGE).await()
        }
    }

    private suspend fun fetchFolderData(
        prefixes: List<StorageReference>
    ): List<StorageFileInfo> = coroutineScope {
        prefixes.flatMap { userFolderRef ->
            userFolderRef.listAll()
                .await()
                .prefixes
                .mapNotNull { soundFolderRef ->
                    async {
                        try {
                            val soundId = soundFolderRef.name
                            val fileRef = soundFolderRef.list(1)
                                .await()
                                .items
                                .firstOrNull() ?: return@async null

                            StorageFileInfo(fileRef, soundId)
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

    private suspend fun fetchFirestoreMetadata(
        folderDataList: List<StorageFileInfo>
    ): Map<String, DocumentSnapshot> {
        val firestoreDocIds = folderDataList.map { it.soundId }.distinct()

        return if (firestoreDocIds.isNotEmpty()) {
            firestore.collection(METADATA_COLLECTION)
                .whereIn(FieldPath.documentId(), firestoreDocIds)
                .get()
                .await()
                .associateBy { it.id }
        } else {
            emptyMap()
        }
    }

    private suspend fun buildAudioMetadataList(
        folderDataList: List<StorageFileInfo>,
        firestoreMetadata: Map<String, DocumentSnapshot>
    ): List<AudioMetadata> {
        return folderDataList.mapNotNull { (fileRef, soundId) ->
            try {
                val firestoreDoc = firestoreMetadata[soundId] ?: return@mapNotNull null
                val url = getOrFetchUrl(soundId, fileRef)

                createAudioMetadata(soundId, url, firestoreDoc)
            } catch (e: Exception) {
                Log.e(
                    "FirebaseStorageService",
                    "Error building AudioMetadata for ${fileRef.path}",
                    e
                )
                null
            }
        }
    }

    private suspend fun getOrFetchUrl(soundId: String, fileRef: StorageReference): String {
        return urlCache.getOrPut(soundId) {
            fileRef.downloadUrl.await().toString()
        }
    }

    private fun createAudioMetadata(
        soundId: String,
        url: String,
        document: DocumentSnapshot
    ): AudioMetadata {
        return AudioMetadata(
            id = soundId,
            url = url,
            fileName = document.getString("file_name") ?: "",
            duration = document.getString("duration") ?: "",
            bpm = document.getString("bpm"),
            tone = document.getString("tone"),
            tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            timestamp = document.getString("timestamp") ?: ""
        )
    }

    actual suspend fun uploadImage(filePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath).takeIf { it.exists() }
                ?: throw FileNotFoundException("File not found at path: $filePath")

            val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw SecurityException("User not logged in")

            val storageRef = storage.reference.child("$AVATAR_PATH/$userId/avatar.jpg")
            val uploadTask = storageRef.putFile(Uri.fromFile(file))
            uploadTask.await()

            val downloadUrl = storageRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("FirebaseStorageRepository", "Error uploading avatar: ${e.message}", e)
            Result.failure(e)
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
            val storageRef = storage.reference.child("$SAMPLES_PATH/$username/$uniqueId/$fileName")

            val uploadResult =
                uploadFileToStorage(username, uniqueId, fileName, fileData, metadata, onProgress)
            val downloadUrl =
                getDownloadUrl(storageRef)

            val resultMetadata = uploadResult.metadata
                ?: throw IllegalStateException("Upload succeeded but metadata is null")

            val completeMetadata = createCompleteMetadata(
                uniqueId, fileName, username, downloadUrl, metadata, resultMetadata
            )
            saveMetadataToFirestore(uniqueId, completeMetadata)

            // Create and return final audio metadata
            Result.success(
                createFinalAudioMetadata(
                    uniqueId,
                    fileName,
                    downloadUrl,
                    metadata,
                    resultMetadata
                )
            )
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error uploading file: ${e.message}", e)
            Result.failure(e)
        }
    }


    private suspend fun uploadFileToStorage(
        username: String,
        uniqueId: String,
        fileName: String,
        fileData: ByteArray,
        metadata: AudioMetadata,
        onProgress: (Float) -> Unit
    ): UploadTask.TaskSnapshot {
        val storageReference =
            storage.reference.child("$SAMPLES_PATH/$username/$uniqueId/$fileName")
        val storageMetadata = createStorageMetadata(fileName, metadata, uniqueId)

        return suspendCancellableCoroutine { continuation ->
            val task = storageReference.putBytes(fileData, storageMetadata)

            task.addOnProgressListener { taskSnapshot ->
                val progress =
                    taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
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
    }

    private fun createStorageMetadata(
        fileName: String,
        metadata: AudioMetadata,
        uniqueId: String
    ): StorageMetadata {
        return StorageMetadata.Builder()
            .setContentType("audio/${fileName.substringAfterLast(".")}")
            .setCustomMetadata("bpm", metadata.bpm ?: "")
            .setCustomMetadata("tone", metadata.tone ?: "")
            .setCustomMetadata("tags", metadata.tags.joinToString(","))
            .setCustomMetadata("firestore_doc_id", uniqueId)
            .build()
    }

    private suspend fun getDownloadUrl(reference: StorageReference): String {
        return suspendCoroutine { continuation ->
            reference.downloadUrl
                .addOnSuccessListener { url -> continuation.resume(url.toString()) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    private fun createCompleteMetadata(
        uniqueId: String,
        fileName: String,
        username: String,
        downloadUrl: String,
        metadata: AudioMetadata,
        storageMetadata: StorageMetadata?
    ): Map<String, Any> {
        return hashMapOf(
            "id" to uniqueId,
            "file_name" to fileName,
            "username" to username,
            "file_extension" to fileName.substringAfterLast("."),
            "duration" to (metadata.duration ?: ""),
            "url" to downloadUrl,
            "timestamp" to System.currentTimeMillis().toString(),
            "bpm" to (metadata.bpm ?: ""),
            "tone" to (metadata.tone ?: ""),
            "tags" to metadata.tags,
            "content_type" to (storageMetadata?.contentType ?: "")
        )
    }

    private fun createFinalAudioMetadata(
        uniqueId: String,
        fileName: String,
        downloadUrl: String,
        metadata: AudioMetadata,
        storageMetadata: StorageMetadata?
    ): AudioMetadata {
        return AudioMetadata(
            id = uniqueId,
            fileName = fileName,
            fileExtension = fileName.substringAfterLast("."),
            duration = metadata.duration ?: "",
            url = downloadUrl,
            timestamp = System.currentTimeMillis().toString(),
            contentType = storageMetadata?.contentType ?: "",
            bpm = metadata.bpm ?: "",
            tone = metadata.tone ?: "",
            tags = metadata.tags,
            isLiked = false
        )
    }

    private suspend fun saveMetadataToFirestore(uniqueId: String, metadata: Map<String, Any>) {
        suspendCoroutine { continuation ->
            firestore.collection(METADATA_COLLECTION)
                .document(uniqueId)
                .set(metadata)
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    actual suspend fun getUserSounds(
        username: String,
        limit: Int,
        lastDocumentId: String?
    ): Result<Pair<List<AudioMetadata>, String?>> = withContext(Dispatchers.IO) {
        try {
            val query = createUserSoundsQuery(username, limit, lastDocumentId)
            val querySnapshot = query.get().await()

            val userSounds = querySnapshot.documents.mapNotNull { document ->
                try {
                    createAudioMetadata(document.id, document.getString("url") ?: "", document)
                } catch (e: Exception) {
                    Log.e("FirebaseStorageService", "Error mapping document ${document.id}", e)
                    null
                }
            }

            val lastVisible = querySnapshot.documents.lastOrNull()?.id

            Result.success(Pair(userSounds, lastVisible))
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error fetching user sounds", e)
            Result.failure(e)
        }
    }

    private suspend fun createUserSoundsQuery(
        username: String,
        limit: Int,
        lastDocumentId: String?
    ): Query {
        val baseQuery = firestore.collection(METADATA_COLLECTION)
            .whereEqualTo("username", username)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())

        return if (lastDocumentId != null) {
            val lastDoc = firestore.collection(METADATA_COLLECTION)
                .document(lastDocumentId)
                .get()
                .await()
            baseQuery.startAfter(lastDoc)
        } else {
            baseQuery
        }
    }

    actual suspend fun updateSoundMetadata(
        soundId: String,
        updates: Map<String, Any>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection(METADATA_COLLECTION)
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
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            coroutineScope {
                launch { deleteFromStorage(username, soundId, fileName) }
                launch { deleteFromFirestore(soundId) }
            }

            urlCache.remove(soundId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseStorageService", "Error deleting sound", e)
            Result.failure(e)
        }
    }

    private suspend fun deleteFromStorage(username: String, soundId: String, fileName: String) {
        storage.reference.child("$SAMPLES_PATH/$username/$soundId/$fileName")
            .delete()
            .await()
    }

    private suspend fun deleteFromFirestore(soundId: String) {
        firestore.collection(METADATA_COLLECTION)
            .document(soundId)
            .delete()
            .await()
    }

    actual suspend fun getSoundMetadata(soundId: String): Result<AudioMetadata> =
        withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = firestore.collection(METADATA_COLLECTION)
                    .document(soundId)
                    .get()
                    .await()

                if (!documentSnapshot.exists()) {
                    return@withContext Result.failure(NoSuchElementException("Document not found for ID: $soundId"))
                }

                Result.success(
                    createAudioMetadata(
                        documentSnapshot.id,
                        documentSnapshot.getString("url") ?: "",
                        documentSnapshot
                    )
                )
            } catch (e: Exception) {
                Log.e("FirebaseStorageService", "Error fetching sound metadata", e)
                Result.failure(e)
            }
        }


    actual suspend fun getSoundsMetadataByIds(soundIds: List<String>): Result<List<AudioMetadata>> =
        withContext(Dispatchers.IO) {
            try {
                if (soundIds.isEmpty()) {
                    return@withContext Result.success(emptyList())
                }

                val querySnapshot = firestore.collection(METADATA_COLLECTION)
                    .whereIn(FieldPath.documentId(), soundIds)
                    .get()
                    .await()

                val metadataList = querySnapshot.documents.mapNotNull { document ->
                    try {
                        createAudioMetadata(
                            document.id,
                            document.getString("url") ?: "",
                            document
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