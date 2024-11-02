package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

actual class FirebaseStorageRepository {
    actual suspend fun listFilesWithMetadata(
        directoryPath: String,
        continuationToken: String?
    ): Result<Pair<List<AudioMetadata>, String?>> {
        TODO("Not yet implemented")
    }

    actual suspend fun uploadImage(filePath: String): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun uploadSoundsToStorage(
        username: String,
        fileName: String,
        fileData: ByteArray,
        metadata: AudioMetadata,
        onProgress: (Float) -> Unit
    ): Result<AudioMetadata> {
        TODO("Not yet implemented")
    }

    actual suspend fun getUserSounds(
        username: String,
        limit: Int,
        lastDocumentId: String?
    ): Result<Pair<List<AudioMetadata>, String?>> {
        TODO("Not yet implemented")
    }

    actual suspend fun updateSoundMetadata(
        soundId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun deleteUserSound(
        username: String,
        soundId: String,
        fileName: String
    ): Result<Unit> {
        TODO("Not yet implemented")
    }

    actual suspend fun getSoundsMetadataByIds(soundIds: List<String>): Result<List<AudioMetadata>> {
        TODO("Not yet implemented")
    }

    actual suspend fun getSoundMetadata(soundId: String): Result<AudioMetadata> {
        TODO("Not yet implemented")
    }

}