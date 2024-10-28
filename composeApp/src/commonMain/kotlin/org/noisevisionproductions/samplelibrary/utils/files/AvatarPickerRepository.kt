package org.noisevisionproductions.samplelibrary.utils.files

interface AvatarPickerRepository {
    suspend fun pickAvatar(): String?
    suspend fun filePathToByteArray(filePath: String): ByteArray?
}