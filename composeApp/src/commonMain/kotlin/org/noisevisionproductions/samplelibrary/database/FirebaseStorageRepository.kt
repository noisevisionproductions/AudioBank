package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.dataClasses.AudioMetadata

expect class FirebaseStorageRepository() {
    suspend fun listFilesWithMetadata(
        directoryPath: String,
        continuationToken: String?
    ): Pair<List<AudioMetadata>, String?>

    suspend fun uploadSoundsToStorage(
        username: String,
        fileName: String,
        fileData: ByteArray,
        metadata: AudioMetadata,
        onProgress: (Float) -> Unit
    ): Result<String>

    suspend fun uploadImage(filePath: String): String
}