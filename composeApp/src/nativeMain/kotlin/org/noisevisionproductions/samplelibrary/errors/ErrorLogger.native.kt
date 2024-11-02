package org.noisevisionproductions.samplelibrary.errors

actual class ErrorLogger actual constructor() {
    actual fun logError(error: AppError) {
        println("iOS Error: ${error.message}")
        error.technicalDetails?.let { println("Technical details: $it") }
    }
}