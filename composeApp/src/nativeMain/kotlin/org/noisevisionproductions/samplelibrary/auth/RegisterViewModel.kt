package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import org.noisevisionproductions.samplelibrary.auth.validation.RegisterFormState

actual class RegisterViewModel actual constructor(authService: AuthService) :
    ViewModel() {
    actual val formState: StateFlow<RegisterFormState>
        get() = TODO("Not yet implemented")

    actual fun updateNickname(nickname: String) {
    }

    actual fun performRegister(
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
    }
}