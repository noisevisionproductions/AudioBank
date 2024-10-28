package org.noisevisionproductions.samplelibrary.utils.files


expect class AvatarPickerRepositoryImpl(
    filePicker: FilePicker
) : AvatarPickerRepository {
    override suspend fun pickAvatar(): String?
    override suspend fun filePathToByteArray(filePath: String): ByteArray?
}
