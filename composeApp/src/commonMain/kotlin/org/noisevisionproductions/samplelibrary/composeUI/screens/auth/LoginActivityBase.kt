package org.noisevisionproductions.samplelibrary.composeUI.screens.auth

expect class LoginActivityBase {
    fun performLogin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun navigateToRegistration()
    fun navigateToMain(userId: String)
}