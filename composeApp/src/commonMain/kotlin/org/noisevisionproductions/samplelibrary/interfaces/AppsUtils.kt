package org.noisevisionproductions.samplelibrary.interfaces

expect fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
)

expect fun showPostCreatedMessage(message: String)

expect fun getCurrentTimestamp(): String

expect fun formatTimeAgo(timestamp: String): String