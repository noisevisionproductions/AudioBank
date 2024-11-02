package org.noisevisionproductions.samplelibrary.composeUI.screens.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.LoginViewModel
import org.noisevisionproductions.samplelibrary.auth.LoginViewModelFactory
import org.noisevisionproductions.samplelibrary.composeUI.screens.SamplesActivity
import org.noisevisionproductions.samplelibrary.composeUI.screens.loginAndRegister.LoginScreen

actual class LoginActivityBase : AppCompatActivity() {

    private lateinit var authService: AuthService
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthService()

        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(authService)
        )[LoginViewModel::class.java]

        setContent {
            LoginScreen(
                viewModel = viewModel,
                onLoginClick = { email, password ->
                    performLogin(email, password, ::navigateToMain) { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                onRegistrationActivityClick = { navigateToRegistration() },
            )
        }
    }

    actual fun performLogin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModel.performLogin(email, password, onSuccess, onFailure)
    }

    actual fun navigateToRegistration() {
        val intent = Intent(this, RegisterActivityBase::class.java)
        startActivity(intent)
        finish()
    }

    actual fun navigateToMain(userId: String) {
        val intent = Intent(this, SamplesActivity::class.java).apply {
            putExtra("accessToken", userId)
        }
        startActivity(intent)
        finish()
    }

}