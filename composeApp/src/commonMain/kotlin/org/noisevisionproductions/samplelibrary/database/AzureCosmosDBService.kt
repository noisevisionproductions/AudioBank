package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.FileMetadata

expect class AzureCosmosDBService() {
    suspend fun getSynchronizedData(continuationToken: String?): Pair<List<FileMetadata>, String?>
}