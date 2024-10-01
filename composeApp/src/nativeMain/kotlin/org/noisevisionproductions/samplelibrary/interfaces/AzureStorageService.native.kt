package org.noisevisionproductions.samplelibrary.interfaces

actual class AzureStorageService {
    actual suspend fun listFilesInBucket(bucketName: String, continuationToken: String): Pair<List<String>, String?> {
        TODO("Not yet implemented")
    }
}