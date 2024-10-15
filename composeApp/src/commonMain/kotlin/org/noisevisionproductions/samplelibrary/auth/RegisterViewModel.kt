package org.noisevisionproductions.samplelibrary.auth

import kotlinx.coroutines.flow.StateFlow
import org.noisevisionproductions.samplelibrary.auth.validation.RegisterFormState

expect class RegisterViewModel(authService: AuthService) :
    BaseAuthViewModel {
    val formState: StateFlow<RegisterFormState>
    fun updateNickname(nickname: String)
    fun updateEmail(email: String)
    fun updatePassword(password: String)
    fun updateConfirmPassword(confirmPassword: String)
    fun performRegister(
        nickname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

}