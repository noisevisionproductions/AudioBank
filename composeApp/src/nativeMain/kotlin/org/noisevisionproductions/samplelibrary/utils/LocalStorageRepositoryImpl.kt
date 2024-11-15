package org.noisevisionproductions.samplelibrary.utils

import kotlinx.coroutines.CoroutineScope

actual class LocalStorageRepositoryImpl actual constructor(
    scope: CoroutineScope
) : LocalStorageRepository {
    override suspend fun saveAvatarUrl(url: String) {
        TODO("Not yet implemented")
    }

    override suspend fun getAvatarUrl(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun saveAvatarImage(imageBytes: ByteArray) {
        TODO("Not yet implemented")
    }

    override suspend fun getAvatarImage(): ByteArray? {
        TODO("Not yet implemented")
    }

    override suspend fun clearCache() {
        TODO("Not yet implemented")
    }

}