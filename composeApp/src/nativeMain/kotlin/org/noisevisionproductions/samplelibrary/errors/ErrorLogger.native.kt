package org.noisevisionproductions.samplelibrary.errors

actual class ErrorLogger actual constructor() {
    actual fun logError(error: AppError) {
        // Tutaj możesz użyć systemu logowania iOS
        println("iOS Error: ${error.message}")
        error.technicalDetails?.let { println("Technical details: $it") }
    }
}