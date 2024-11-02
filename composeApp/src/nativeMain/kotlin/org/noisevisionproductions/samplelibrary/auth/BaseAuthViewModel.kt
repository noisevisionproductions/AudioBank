package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.noisevisionproductions.samplelibrary.errors.validation.ValidationResult

actual abstract class BaseAuthViewModel : ViewModel() {

    private val mainScope = MainScope()

    protected actual fun validateEmail(email: String): ValidationResult {
        val emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
        return when {
            email.isBlank() -> ValidationResult.Invalid("Adres e-mail nie może być pusty")
            !emailRegex.matches(email) -> ValidationResult.Invalid("Niepoprawny format adresu e-mail")
            else -> ValidationResult.Valid
        }
    }

    protected actual open fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Hasło nie może być puste")
            else -> ValidationResult.Valid
        }
    }

    override fun onCleared() {
        super.onCleared()
        mainScope.cancel()
    }
}