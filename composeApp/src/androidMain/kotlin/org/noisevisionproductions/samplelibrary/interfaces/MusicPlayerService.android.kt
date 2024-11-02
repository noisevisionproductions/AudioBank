package org.noisevisionproductions.samplelibrary.interfaces

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class MusicPlayerService {
    private var mediaPlayer: MediaPlayer? = null
    private var isPreparing = false
    private var isPausedRequested = false

    actual suspend fun playAudioFromUrl(
        audioUrl: String,
        currentlyPlayingUrl: String?,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    ) {
        if (audioUrl.isBlank()) {
            Log.e("DEBUG", "Invalid audio URL: $audioUrl")
            return
        }
        try {

            if (mediaPlayer != null && currentlyPlayingUrl == audioUrl && mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                startProgressTracking(onProgressUpdate)
                return
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                isPreparing = true
                prepareAsync()

                setOnPreparedListener { player ->
                    isPreparing = false
                    if (isPausedRequested) {
                        player.pause()
                        isPausedRequested = false
                    } else {
                        player.start()
                        startProgressTracking(onProgressUpdate)
                    }
                }

                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                    onCompletion()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Error playing the file", "Error playing file " + e.message)
            isPreparing = false
        }
    }

    actual fun resumeAudio(onProgressUpdate: (Float) -> Unit) {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                startProgressTracking(onProgressUpdate)
            }
        }
    }

    actual fun pauseAudio() {
        if (isPreparing) {
            isPausedRequested = true
        } else if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    actual fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPreparing = false
        isPausedRequested = false
    }

    actual suspend fun seekTo(newValue: Float) {
        mediaPlayer?.let { player ->
            val duration = player.duration
            val position = (newValue * duration).toInt()
            player.seekTo(position)
        }
    }

    private fun startProgressTracking(onProgressUpdate: (Float) -> Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            while (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                val currentPosition = try {
                    mediaPlayer?.currentPosition ?: 0
                } catch (e: IllegalStateException) {
                    0
                }
                val duration = mediaPlayer?.duration ?: 1
                val progress = currentPosition / duration.toFloat()
                onProgressUpdate(progress)
                delay(500)
            }
        }
    }
}