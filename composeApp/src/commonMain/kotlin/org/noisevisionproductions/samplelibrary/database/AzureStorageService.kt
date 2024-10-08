package org.noisevisionproductions.samplelibrary.database

expect class AzureStorageService() {
    suspend fun listFilesInBucket(
        bucketName: String,
        continuationToken: String?
    ): Pair<List<String>, String?>
}