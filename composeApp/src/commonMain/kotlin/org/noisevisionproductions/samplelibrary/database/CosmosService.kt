package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.FileMetadata

expect class CosmosService() {
    suspend fun getSynchronizedData(): List<FileMetadata>
}