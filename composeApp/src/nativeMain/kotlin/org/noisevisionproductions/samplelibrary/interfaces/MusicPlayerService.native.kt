package org.noisevisionproductions.samplelibrary.interfaces

// In iosMain

import platform.AVFoundation.*
import platform.Foundation.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

actual class MusicPlayerService {
    private var player: AVPlayer? = null
    private var isPreparing = false
    private var isPausedRequested = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    actual suspend fun playAudioFromUrl(
        audioUrl: String,
        currentlyPlayingUrl: String?,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    ) {
        if (audioUrl.isBlank()) {
            NSLog("Invalid audio URL: $audioUrl")
            return
        }

        // If the same URL is paused, resume playback
        if (player?.currentItem?.asset?.isPlayable == true && currentlyPlayingUrl == audioUrl) {
            player?.play()
            startProgressTracking(onProgressUpdate)
            return
        }

        // Stop current playback
        stopAudio()

        // Initialize and configure AVPlayer
        isPreparing = true
        val url = NSURL(string = audioUrl) ?: return
        val playerItem = AVPlayerItem(URL = url)
        player = AVPlayer(playerItem)

        player?.apply {
            addObserverForProgress(onProgressUpdate)
            isPreparing = false
            if (isPausedRequested) {
                pause()
                isPausedRequested = false
            } else {
                play()
                startProgressTracking(onProgressUpdate)
            }

            // Completion listener
            playerItem.addObserver(
                this,
                forKeyPath = "status",
                options = NSKeyValueObservingOptionNew,
                context = null
            )
            addPeriodicTimeObserverForInterval(
                CMTimeMakeWithSeconds(1.0, NSEC_PER_SEC.toLong()),
                dispatch_get_main_queue()
            ) {
                if (it == duration) {
                    stopAudio()
                    onCompletion()
                }
            }
        }
    }

    actual fun resumeAudio(onProgressUpdate: (Float) -> Unit) {
        player?.let {
            if (it.timeControlStatus != AVPlayerTimeControlStatusPlaying) {
                it.play()
                startProgressTracking(onProgressUpdate)
            }
        }
    }

    actual fun pauseAudio() {
        if (isPreparing) {
            isPausedRequested = true
        } else {
            player?.pause()
        }
    }

    actual fun stopAudio() {
        player?.pause()
        player = null
        isPreparing = false
        isPausedRequested = false
    }

    actual suspend fun seekTo(newValue: Float) {
        player?.let {
            val duration = it.currentItem?.duration?.seconds ?: 1.0
            val position = newValue * duration
            it.seekToTime(CMTimeMakeWithSeconds(position, NSEC_PER_SEC.toLong()))
        }
    }

    private fun startProgressTracking(onProgressUpdate: (Float) -> Unit) {
        coroutineScope.launch {
            while (player != null && player?.timeControlStatus == AVPlayerTimeControlStatusPlaying) {
                val currentTime = player?.currentTime()?.seconds ?: 0.0
                val duration = player?.currentItem?.duration?.seconds ?: 1.0
                val progress = (currentTime / duration).toFloat()
                onProgressUpdate(progress)
                delay(500)
            }
        }
    }

    private fun AVPlayer.addObserverForProgress(onProgressUpdate: (Float) -> Unit) {
        addPeriodicTimeObserverForInterval(
            CMTimeMakeWithSeconds(0.5, NSEC_PER_SEC.toLong()),
            dispatch_get_main_queue()
        ) {
            val duration = currentItem?.duration?.seconds ?: 1.0
            val currentTime = currentTime().seconds
            val progress = (currentTime / duration).toFloat()
            onProgressUpdate(progress)
        }
    }
}
