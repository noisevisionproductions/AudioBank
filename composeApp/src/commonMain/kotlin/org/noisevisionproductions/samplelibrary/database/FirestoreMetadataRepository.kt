package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

expect class FirestoreMetadataRepository() {
    suspend fun searchByTags(tags: List<String>): List<AudioMetadata>
    suspend fun updateMetadata(firestoreDocId: String, updates: Map<String, Any>): Boolean
    suspend fun getMetadataForFile(firestoreDocId: String): AudioMetadata?
}