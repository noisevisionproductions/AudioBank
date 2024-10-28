package org.noisevisionproductions.samplelibrary.errors.validation

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationResult = ValidationResult.Valid,
    val passwordError: ValidationResult = ValidationResult.Valid
)
