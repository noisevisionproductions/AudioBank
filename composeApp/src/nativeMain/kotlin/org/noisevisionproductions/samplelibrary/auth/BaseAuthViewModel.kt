package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.ViewModel
import org.noisevisionproductions.samplelibrary.errors.validation.ValidationResult

actual abstract class BaseAuthViewModel : ViewModel() {
    protected actual fun validateEmail(email: String): ValidationResult {
        TODO("Not yet implemented")
    }

    protected actual fun validatePassword(password: String): ValidationResult {
        TODO("Not yet implemented")
    }

}