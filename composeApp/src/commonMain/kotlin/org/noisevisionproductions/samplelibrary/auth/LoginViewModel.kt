package org.noisevisionproductions.samplelibrary.auth

import kotlinx.coroutines.flow.StateFlow
import org.noisevisionproductions.samplelibrary.auth.validation.LoginFormState

expect class LoginViewModel(authService: AuthService) : BaseAuthViewModel {
    val formState: StateFlow<LoginFormState>
    fun updateEmail(email: String)
    fun updatePassword(password: String)
    fun performLogin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )
}