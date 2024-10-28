package org.noisevisionproductions.samplelibrary.errors.validation

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}