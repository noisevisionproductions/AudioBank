package org.noisevisionproductions.samplelibrary.interfaces

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

actual class MusicPlayerService {
    private var mediaPlayer: MediaPlayer? = null

    actual suspend fun playAudioFromUrl(
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

    actual suspend fun seekTo(newValue: Float) {
        mediaPlayer?.let { player ->
            val duration = player.duration
            val position = (newValue * duration).toInt()
            player.seekTo(position)
        }
    }

    actual fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    actual fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

}