package org.noisevisionproductions.samplelibrary.utils.files

actual class AvatarPickerRepositoryImpl actual constructor(filePicker: FilePicker) :
    AvatarPickerRepository {
    actual override suspend fun pickAvatar(): String? {
        TODO("Not yet implemented")
    }

    actual override suspend fun filePathToByteArray(filePath: String): ByteArray? {
        TODO("Not yet implemented")
    }
}