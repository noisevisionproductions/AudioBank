package org.noisevisionproductions.samplelibrary.database

interface CloudFirestore {
    suspend fun listFilesInBucket(bucketName: String): List<String>

    suspend fun getSampleFile(fileName: String): String

    suspend fun playAudioFromUrl(audioUrl: String)

    suspend fun getAudioDuration(audioUrl: String): Int
}

expect fun getCloudFirestore(): CloudFirestore
