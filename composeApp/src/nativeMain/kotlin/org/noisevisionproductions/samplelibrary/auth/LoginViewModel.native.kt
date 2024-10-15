package org.noisevisionproductions.samplelibrary.auth

import kotlinx.coroutines.flow.StateFlow
import org.noisevisionproductions.samplelibrary.auth.validation.RegisterFormState

actual class LoginViewModel actual constructor(authService: AuthService) :
    BaseAuthViewModel() {
    actual val formState: StateFlow<RegisterFormState>
        get() = TODO("Not yet implemented")

    actual fun updateEmail(email: String) {
    }

    actual fun updatePassword(password: String) {
    }

    actual fun performLogin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
    }
}