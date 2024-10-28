package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.dataClasses.AudioMetadata

actual class FirestoreMetadataRepository {
    actual suspend fun getSynchronizedData(): List<AudioMetadata> {
        TODO("Not yet implemented")
    }
}