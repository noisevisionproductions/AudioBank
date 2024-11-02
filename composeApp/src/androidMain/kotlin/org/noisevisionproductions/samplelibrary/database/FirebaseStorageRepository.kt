package org.noisevisionproductions.samplelibrary.database

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldPath
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.storage.ListResult
import dev.gitlive.firebase.storage.StorageMetadata
import dev.gitlive.firebase.storage.StorageReference
import dev.gitlive.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.fileExistsAtPath
import platform.Foundation.NSUUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class FirebaseStorageRepository actual constructor() {
    private val storage = Firebase.storage
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
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
    ): Result<Pair<List<AudioMetadata>, String?>> {
        return try {
            val storageReference = storage.reference.child(SAMPLES_PATH)

            val foldersResult = fetchUserDirectories(storageReference, continuationToken)

            val folderDataList = fetchFolderData(foldersResult.prefixes)

            val firestoreMetadata = fetchFirestoreMetadata(folderDataList)

            val audioMetadataList = buildAudioMetadataList(folderDataList, firestoreMetadata)

            Result.success(Pair(audioMetadataList, foldersResult.pageToken))
        } catch (e: Exception) {
            println("Error listing files: $e")
            Result.failure(e)
        }
    }

    private suspend fun fetchUserDirectories(
        storageReference: StorageReference,
        continuationToken: String?
    ): ListResult {
        return if (continuationToken != null) {
            storageReference.list(MAX_RESULT_PER_PAGE, continuationToken)
        } else {
            storageReference.list(MAX_RESULT_PER_PAGE)
        }
    }

    private suspend fun fetchFolderData(
        prefixes: List<StorageReference>
    ): List<StorageFileInfo> = coroutineScope {
        prefixes.flatMap { userFolderRef ->
            val soundFolderRefs = userFolderRef.listAll().prefixes
            soundFolderRefs.mapNotNull { soundFolderRef ->
                async {
                    try {
                        val soundId = soundFolderRef.name
                        val fileListResult = soundFolderRef.list(1)
                        val fileRef = fileListResult.items.firstOrNull() ?: return@async null

                        StorageFileInfo(fileRef, soundId)
                    } catch (e: Exception) {
                        println("Error processing folder ${soundFolderRef.path}: $e")
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
            val query = firestore.collection(METADATA_COLLECTION)
                .whereIn(FieldPath.documentId(), firestoreDocIds)
            val querySnapshot = query.get()
            querySnapshot.documents.associateBy { it.id }
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
                println("Error building AudioMetadata for ${fileRef.path}: $e")
                null
            }
        }
    }

    private suspend fun getOrFetchUrl(soundId: String, fileRef: StorageReference): String {
        return urlCache.getOrPut(soundId) {
            fileRef.getDownloadUrl()
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
            fileName = document.get("file_name") as? String ?: "",
            duration = document.get("duration") as? String ?: "",
            bpm = document.get("bpm") as? String,
            tone = document.get("tone") as? String,
            tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            timestamp = document.get("timestamp") as? String ?: ""
        )
    }

    actual suspend fun uploadImage(filePath: String): Result<String> {
        return try {
            val fileManager = platform.Foundation.NSFileManager.defaultManager
            val fileExists = fileManager.fileExistsAtPath(filePath)
            if (!fileExists) throw Exception("File not found at path: $filePath")

            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            val storageRef = storage.reference.child("$AVATAR_PATH/$userId/avatar.jpg")
            val fileData = NSData.dataWithContentsOfFile(filePath)
                ?: throw Exception("Unable to read file data at path: $filePath")

            val uploadTask = storageRef.putData(fileData)
            uploadTask.await()

            val downloadUrl = storageRef.getDownloadUrl()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            println("Error uploading avatar: ${e.message}")
            Result.failure(e)
        }
    }

    actual suspend fun uploadSoundsToStorage(
        username: String,
        fileName: String,
        fileData: ByteArray,
        metadata: AudioMetadata,
        onProgress: (Float) -> Unit
    ): Result<AudioMetadata> {
        return try {
            val uniqueId = NSUUID.UUID().UUIDString
            val storageRef = storage.reference.child("$SAMPLES_PATH/$username/$uniqueId/$fileName")

            val uploadResult =
                uploadFileToStorage(username, uniqueId, fileName, fileData, metadata, onProgress)
            val downloadUrl = getDownloadUrl(storageRef)

            val resultMetadata = uploadResult.metadata
                ?: throw Exception("Upload succeeded but metadata is null")

            val completeMetadata = createCompleteMetadata(
                uniqueId, fileName, username, downloadUrl, metadata, resultMetadata
            )
            saveMetadataToFirestore(uniqueId, completeMetadata)

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
            println("Error uploading file: ${e.message}")
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
    ): UploadTask {
        val storageReference =
            storage.reference.child("$SAMPLES_PATH/$username/$uniqueId/$fileName")
        val storageMetadata = StorageMetadata(
            contentType = "audio/${fileName.substringAfterLast(".")}",
            customMetadata = mapOf(
                "bpm" to (metadata.bpm ?: ""),
                "tone" to (metadata.tone ?: ""),
                "tags" to metadata.tags.joinToString(","),
                "firestore_doc_id" to uniqueId
            )
        )

        val uploadTask = storageReference.putBytes(fileData, storageMetadata)
        uploadTask.onProgress { bytesTransferred, totalByteCount ->
            val progress = bytesTransferred.toFloat() / totalByteCount.toFloat()
            onProgress(progress)
        }
        uploadTask.await()
        return uploadTask
    }

    private suspend fun getDownloadUrl(reference: StorageReference): String {
        return reference.getDownloadUrl()
    }

    private fun createCompleteMetadata(
        uniqueId: String,
        fileName: String,
        username: String,
        downloadUrl: String,
        metadata: AudioMetadata,
        storageMetadata: StorageMetadata?
    ): Map<String, Any> {
        return mapOf(
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
        firestore.collection(METADATA_COLLECTION)
            .document(uniqueId)
            .set(metadata)
            .await()
    }

    actual suspend fun getUserSounds(
        username: String,
        limit: Int,
        lastDocumentId: String?
    ): Result<Pair<List<AudioMetadata>, String?>> {
        return try {
            val query = createUserSoundsQuery(username, limit, lastDocumentId)
            val querySnapshot = query.get()

            val userSounds = querySnapshot.documents.mapNotNull { document ->
                try {
                    createAudioMetadata(document.id, document.get("url") as? String ?: "", document)
                } catch (e: Exception) {
                    println("Error mapping document ${document.id}: $e")
                    null
                }
            }

            val lastVisible = querySnapshot.documents.lastOrNull()?.id

            Result.success(Pair(userSounds, lastVisible))
        } catch (e: Exception) {
            println("Error fetching user sounds: $e")
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
            baseQuery.startAfter(lastDoc)
        } else {
            baseQuery
        }
    }

    actual suspend fun updateSoundMetadata(
        soundId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        return try {
            firestore.collection(METADATA_COLLECTION)
                .document(soundId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating sound metadata: $e")
            Result.failure(e)
        }
    }

    actual suspend fun deleteUserSound(
        username: String,
        soundId: String,
        fileName: String
    ): Result<Unit> {
        return try {
            coroutineScope {
                launch { deleteFromStorage(username, soundId, fileName) }
                launch { deleteFromFirestore(soundId) }
            }

            urlCache.remove(soundId)
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting sound: $e")
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

    actual suspend fun getSoundMetadata(soundId: String): Result<AudioMetadata> {
        return try {
            val documentSnapshot = firestore.collection(METADATA_COLLECTION)
                .document(soundId)
                .get()

            if (!documentSnapshot.exists) {
                return Result.failure(Exception("Document not found for ID: $soundId"))
            }

            Result.success(
                createAudioMetadata(
                    documentSnapshot.id,
                    documentSnapshot.get("url") as? String ?: "",
                    documentSnapshot
                )
            )
        } catch (e: Exception) {
            println("Error fetching sound metadata: $e")
            Result.failure(e)
        }
    }

    actual suspend fun getSoundsMetadataByIds(soundIds: List<String>): Result<List<AudioMetadata>> {
        return try {
            if (soundIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val querySnapshot = firestore.collection(METADATA_COLLECTION)
                .whereIn(FieldPath.documentId(), soundIds)
                .get()

            val metadataList = querySnapshot.documents.mapNotNull { document ->
                try {
                    createAudioMetadata(
                        document.id,
                        document.get("url") as? String ?: "",
                        document
                    )
                } catch (e: Exception) {
                    println("Error mapping document ${document.id}: $e")
                    null
                }
            }

            Result.success(metadataList)
        } catch (e: Exception) {
            println("Error fetching sound metadata for IDs: $e")
            Result.failure(e)
        }
    }
}
