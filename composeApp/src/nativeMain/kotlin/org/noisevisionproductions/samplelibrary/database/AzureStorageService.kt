package org.noisevisionproductions.samplelibrary.database

actual class AzureStorageService {
    actual suspend fun listFilesInBucket(bucketName: String, continuationToken: String?): Pair<List<String>, String?> {
        TODO("Not yet implemented")
    }
}