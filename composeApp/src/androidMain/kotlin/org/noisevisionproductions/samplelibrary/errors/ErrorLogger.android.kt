package org.noisevisionproductions.samplelibrary.errors

actual class ErrorLogger actual constructor() {
    actual fun logError(error: AppError) {
        println("Android Error: ${error.message}")
        error.technicalDetails?.let { println("Technical details: $it") }
    }
}