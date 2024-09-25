package org.noisevisionproductions.samplelibrary.database

interface CloudFirestore {
    suspend fun listFilesInBucket(bucketName: String): List<String>

    suspend fun playAudioFromUrl(
        audioUrl: String,
        onCompletion: () -> Unit,
        onProgressUpdate: (Float) -> Unit
    )

    suspend fun getAudioDuration(audioUrl: String): Int

    suspend fun seekTo(newValue: Float)

    fun stopAudio()
    fun pauseAudio()
}

expect fun getCloudFirestore(): CloudFirestore
