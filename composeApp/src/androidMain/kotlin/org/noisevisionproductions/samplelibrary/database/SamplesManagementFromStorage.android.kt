package org.noisevisionproductions.samplelibrary.database


import android.media.MediaPlayer
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AndroidCloudFirestore : CloudFirestore {
    override suspend fun listFilesInBucket(bucketName: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                val storage = FirebaseStorage.getInstance()
                val storageReference = storage.reference.child(bucketName)
                val result: ListResult = storageReference.listAll().await()

                return@withContext result.items.map { it.downloadUrl.await().toString() }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext emptyList<String>()
            }
        }

    override suspend fun getSampleFile(fileName: String): String = withContext(Dispatchers.IO) {
        try {
            return@withContext fileName
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DEBUG", "Błąd pobierania pliku: ${e.message}")
            return@withContext ""
        }
    }

    override suspend fun playAudioFromUrl(audioUrl: String) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
                start()
            }
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error playing the file", "Error playing file " + e.message)
        }
    }

    override suspend fun getAudioDuration(audioUrl: String): Int = withContext(Dispatchers.IO) {
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
            }

            val duration = mediaPlayer.duration
            Log.e("BPM", "testestest audio nie istnieje: $duration")

            mediaPlayer.release()
            return@withContext duration
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error playing the file", "Error playing file " + e.message)
        }
    }
}

actual fun getCloudFirestore(): CloudFirestore {
    return AndroidCloudFirestore()
}