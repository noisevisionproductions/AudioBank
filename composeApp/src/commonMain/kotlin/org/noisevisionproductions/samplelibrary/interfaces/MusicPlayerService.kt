package org.noisevisionproductions.samplelibrary.interfaces

expect class MusicPlayerService() {
    suspend fun playAudioFromUrl(
        audioUrl: String,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    )

    suspend fun seekTo(newValue: Float)
    fun stopAudio()
    fun pauseAudio()
}