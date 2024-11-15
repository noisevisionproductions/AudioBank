package org.noisevisionproductions.samplelibrary.utils.files

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.UIKit.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class AvatarPickerRepositoryImpl actual constructor(
    private val filePicker: FilePicker
) : AvatarPickerRepository {

    actual override suspend fun pickAvatar(): String? =
        suspendCancellableCoroutine { continuation ->
            filePicker.pickImage { result ->
                result?.let { continuation.resume(it) } ?: continuation.resume(null)
            }
        }

    actual override suspend fun filePathToByteArray(filePath: String): ByteArray? {
        return try {
            val fileUrl = NSURL.fileURLWithPath(filePath)
            val data = NSData.dataWithContentsOfURL(fileUrl)
            data?.toByteArray()
        } catch (e: Exception) {
            NSLog("Error reading file to byte array: ${e.localizedMessage}")
            null
        }
    }
}

// Utility function to convert NSData to ByteArray
fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    memcpy(bytes.refTo(0), this.bytes, this.length)
    return bytes
}
