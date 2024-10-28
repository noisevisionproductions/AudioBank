package org.noisevisionproductions.samplelibrary.errors.validation

data class RegisterFormState(
    val nickname: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nicknameError: ValidationResult = ValidationResult.Valid,
    val emailError: ValidationResult = ValidationResult.Valid,
    val passwordError: ValidationResult = ValidationResult.Valid,
    val confirmPasswordError: ValidationResult = ValidationResult.Valid
)
