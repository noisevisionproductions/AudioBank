package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata
import samplelibrary.composeapp.generated.resources.Res

expect class FirebaseStorageRepository() {
    suspend fun listFilesWithMetadata(
        directoryPath: String,
        continuationToken: String?
    ): Result<Pair<List<AudioMetadata>, String?>>

    suspend fun uploadImage(filePath: String): Result<String>

    suspend fun uploadSoundsToStorage(
        username: String,
        fileName: String,
        fileData: ByteArray,
        metadata: AudioMetadata,
        onProgress: (Float) -> Unit
    ): Result<AudioMetadata>

    suspend fun getUserSounds(
        username: String,
        limit: Int = 50,
        lastDocumentId: String? = null
    ): Result<Pair<List<AudioMetadata>, String?>>

    suspend fun updateSoundMetadata(
        soundId: String,
        updates: Map<String, Any>
    ): Result<Unit>

    suspend fun deleteUserSound(username: String, soundId: String, fileName: String): Result<Unit>
    suspend fun getSoundsMetadataByIds(soundIds: List<String>): Result<List<AudioMetadata>>
    suspend fun getSoundMetadata(soundId: String): Result<AudioMetadata>
}