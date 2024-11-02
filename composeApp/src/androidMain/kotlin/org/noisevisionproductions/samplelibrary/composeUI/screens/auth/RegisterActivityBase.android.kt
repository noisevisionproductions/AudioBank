package org.noisevisionproductions.samplelibrary.composeUI.screens.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModel
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModelFactory
import org.noisevisionproductions.samplelibrary.composeUI.screens.SamplesActivity
import org.noisevisionproductions.samplelibrary.composeUI.screens.loginAndRegister.RegisterScreen

actual class RegisterActivityBase : AppCompatActivity() {
    private lateinit var authService: AuthService
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthService()

        viewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(authService)
        )[RegisterViewModel::class.java]

        setContent {
            RegisterScreen(
                viewModel = viewModel,
                onRegisterClick = { nickname, email, password, confirmPassword ->
                    performRegister(
                        nickname,
                        email,
                        password,
                        confirmPassword,
                        ::navigateToMain
                    ) { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                onLoginActivityClick = { navigateToLogin() }
            )
        }
    }

    actual fun performRegister(
        nickname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModel.performRegister(nickname, email, password, confirmPassword, onSuccess, onFailure)
    }

    actual fun navigateToLogin() {
        val intent = Intent(this, LoginActivityBase::class.java)
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