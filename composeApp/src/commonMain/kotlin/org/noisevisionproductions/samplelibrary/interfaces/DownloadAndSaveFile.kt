package org.noisevisionproductions.samplelibrary.interfaces

expect fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
)
