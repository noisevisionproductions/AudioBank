package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.ViewModel
import org.noisevisionproductions.samplelibrary.errors.validation.ValidationResult

actual abstract class BaseAuthViewModel : ViewModel() {

    protected actual fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid("Adres e-mail nie może być pusty")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches() -> ValidationResult.Invalid("Niepoprawny format adresu e-mail")

            else -> ValidationResult.Valid
        }
    }

    protected actual open fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Hasło nie może być puste")
            else -> ValidationResult.Valid
        }
    }
}