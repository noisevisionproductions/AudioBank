package org.noisevisionproductions.samplelibrary.interfaces

expect class AzureStorageService() {
    suspend fun listFilesInBucket(bucketName: String, continuationToken: String): Pair<List<String>, String?>
}