package org.noisevisionproductions.samplelibrary.database


import android.media.MediaPlayer
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AndroidCloudFirestore : CloudFirestore {
    private var mediaPlayer: MediaPlayer? = null

    override suspend fun listFilesInBucket(bucketName: String): List<String> {
        return withContext(Dispatchers.IO) {
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
    }

    override suspend fun playAudioFromUrl(
        audioUrl: String,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    ) {
        if (audioUrl.isBlank()) {
            Log.e("DEBUG", "Invalid audio URL: $audioUrl")
            return
        }
        try {
            if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()

                val duration = mediaPlayer?.duration ?: 0
                val coroutineScope = CoroutineScope(Dispatchers.Main)

                coroutineScope.launch {
                    while (mediaPlayer?.isPlaying == true) {
                        val currentPosition = mediaPlayer?.currentPosition ?: 0
                        val progress = currentPosition / duration.toFloat()
                        onProgressUpdate(progress)
                        delay(500)
                    }
                }

                mediaPlayer?.setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    onCompletion()
                }
            } else {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioUrl)
                    prepareAsync()
                    setOnPreparedListener {
                        start()
                        val duration = mediaPlayer?.duration ?: 0
                        val coroutineScope = CoroutineScope(Dispatchers.Main)

                        coroutineScope.launch {
                            while (mediaPlayer?.isPlaying == true) {
                                val currentPosition = mediaPlayer?.currentPosition ?: 0
                                val progress = currentPosition / duration.toFloat()
                                onProgressUpdate(progress)
                                delay(500)
                            }
                        }
                    }
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                        onCompletion()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error playing the file", "Error playing file " + e.message)
        }
    }

    override fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    override suspend fun getAudioDuration(audioUrl: String): Int = withContext(Dispatchers.IO) {
        if (audioUrl.isBlank()) {
            Log.e("DEBUG", "Invalid audio URL: $audioUrl")
            return@withContext 0
        }
        try {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepare()
            }

            val duration = mediaPlayer.duration

            mediaPlayer.release()
            return@withContext duration
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error playing the file", "Error playing file " + e.message)
        }
    }

    override suspend fun seekTo(newValue: Float) {
        mediaPlayer?.let { player ->
            val duration = player.duration
            val position = (newValue * duration).toInt()
            player.seekTo(position)
        }
    }
}

actual fun getCloudFirestore(): CloudFirestore {
    return AndroidCloudFirestore()
}