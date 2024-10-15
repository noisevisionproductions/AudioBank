package org.noisevisionproductions.samplelibrary.interfaces

actual fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
) {
}

actual fun showPostCreatedMessage(message: String) {
}

actual fun getCurrentTimestamp(): String {
    TODO("Not yet implemented")
}