package org.noisevisionproductions.samplelibrary.utils.files

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

actual class AvatarPickerRepositoryImpl actual constructor(
    private val filePicker: FilePicker
) : AvatarPickerRepository {

    actual override suspend fun pickAvatar(): String? =
        suspendCancellableCoroutine { continuation ->
            filePicker.pickFiles("image/*", allowMultiple = false) { files ->
                // Extract the file path from the first FileData or return null if not available
                continuation.resume(files.firstOrNull()?.filePath)
            }
        }

    actual override suspend fun filePathToByteArray(filePath: String): ByteArray? {
        return try {
            File(filePath).readBytes()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

