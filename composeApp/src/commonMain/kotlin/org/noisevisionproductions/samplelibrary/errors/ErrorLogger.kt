package org.noisevisionproductions.samplelibrary.errors

expect class ErrorLogger() {
    fun logError(error: AppError)
}