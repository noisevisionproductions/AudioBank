package org.noisevisionproductions.samplelibrary.interfaces

import androidx.compose.ui.text.font.FontFamily

expect fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
)

expect fun showPostCreatedMessage(message: String)

expect fun getCurrentTimestamp(): String

expect fun formatTimeAgo(timestamp: String): String

expect fun poppinsFontFamily(): FontFamily