package org.noisevisionproductions.samplelibrary.utils

expect class LocalStorageRepositoryImpl() {
    suspend fun saveAvatarUrl(url: String)
    suspend fun getAvatarUrl(): String?
    suspend fun saveAvatarImage(filePath: String)
    suspend fun clearCache()
}