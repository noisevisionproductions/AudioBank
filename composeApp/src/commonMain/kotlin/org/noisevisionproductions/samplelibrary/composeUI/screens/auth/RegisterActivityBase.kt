package org.noisevisionproductions.samplelibrary.composeUI.screens.auth

expect class RegisterActivityBase {
    fun performRegister(
        nickname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun navigateToLogin()
    fun navigateToMain(userId: String)
}
