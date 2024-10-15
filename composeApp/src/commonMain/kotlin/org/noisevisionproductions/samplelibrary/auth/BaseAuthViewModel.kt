package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.ViewModel
import org.noisevisionproductions.samplelibrary.auth.validation.ValidationResult

expect abstract class BaseAuthViewModel : ViewModel {
    protected fun validateEmail(email: String): ValidationResult
    protected fun validatePassword(password: String): ValidationResult

}