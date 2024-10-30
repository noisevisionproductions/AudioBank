package org.noisevisionproductions.samplelibrary.database

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

actual class FirestoreMetadataRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Dodatkowe metody do zarządzania metadanymi
    actual suspend fun searchByTags(tags: List<String>): List<AudioMetadata> {
        return try {
            val snapshot = firestore.collection("audio_metadata")
                .whereArrayContainsAny("tags", tags)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    AudioMetadata(
                        url = document.getString("url") ?: return@mapNotNull null,
                        fileName = document.getString("file_name") ?: return@mapNotNull null,
                        duration = document.getString("duration") ?: "",
                        bpm = document.getString("bpm"),
                        tone = document.getString("tone"),
                        tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String }
                            ?: emptyList())

                } catch (e: Exception) {
                    Log.e("FirestoreMetadataService", "Error mapping document: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreMetadataService", "Error searching by tags: ${e.message}", e)
            emptyList()
        }
    }

    actual suspend fun updateMetadata(
        firestoreDocId: String,
        updates: Map<String, Any>
    ): Boolean {
        return try {
            firestore.collection("audio_metadata")
                .document(firestoreDocId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreMetadataService", "Error updating metadata: ${e.message}", e)
            false
        }
    }

    actual suspend fun getMetadataForFile(firestoreDocId: String): AudioMetadata? {
        return try {
            val document = firestore.collection("audio_metadata")
                .document(firestoreDocId)
                .get()
                .await()

            if (document.exists()) {
                AudioMetadata(
                    url = document.getString("url") ?: return null,
                    fileName = document.getString("file_name") ?: return null,
                    duration = document.getString("duration") ?: "",
                    bpm = document.getString("bpm"),
                    tone = document.getString("tone"),
                    tags = (document.get("tags") as? List<*>)?.mapNotNull { it as? String }
                        ?: emptyList())
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreMetadataService", "Error getting metadata: ${e.message}", e)
            null
        }
    }
}