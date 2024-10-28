package org.noisevisionproductions.samplelibrary.composeUI.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.LoginViewModel
import org.noisevisionproductions.samplelibrary.auth.LoginViewModelFactory
import org.noisevisionproductions.samplelibrary.composeUI.screens.loginAndRegister.LoginScreen

class LoginActivity : AppCompatActivity() {
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
                    // Dopiero po zakończeniu inicjalizacji uruchom logowanie
                    viewModel.performLogin(
                        email = email,
                        password = password,
                        onSuccess = { userId ->
                            Log.d("LoginActivity", "Zalogowano użytkownika z ID: $userId")
                            val intent = Intent(this@LoginActivity, SamplesActivity::class.java)
                            intent.putExtra("accessToken", userId)
                            startActivity(intent)
                            finish()
                        },
                        onFailure = { errorMessage ->
                            Log.e("LoginActivity", "Logowanie nie powiodło się: $errorMessage")
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onRegistrationActivityClick = {
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onForgotPasswordClick = {
                    // Logika odzyskiwania hasła
                }
            )
        }
    }
}