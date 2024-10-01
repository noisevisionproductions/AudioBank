package org.noisevisionproductions.samplelibrary.interfaces

actual class MusicPlayerService {
    actual suspend fun playAudioFromUrl(
        audioUrl: String,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    ) {
    }

    actual suspend fun seekTo(newValue: Float) {
    }

    actual fun stopAudio() {
    }

    actual fun pauseAudio() {
    }

}