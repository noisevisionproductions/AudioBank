package org.noisevisionproductions.samplelibrary.auth.validation

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}