package org.noisevisionproductions.samplelibrary.interfaces

expect class MusicPlayerService() {
    suspend fun playAudioFromUrl(
        audioUrl: String,
        currentlyPlayingUrl: String?,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    )

    suspend fun seekTo(newValue: Float)
    fun resumeAudio(onProgressUpdate: (Float) -> Unit)
    fun stopAudio()
    fun pauseAudio()
}