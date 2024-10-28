package org.noisevisionproductions.samplelibrary.composeUI.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.loginAndRegister.RegisterScreen
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModelFactory

class RegisterActivity : AppCompatActivity() {
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
                    viewModel.performRegister(
                        nickname = nickname,
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        onSuccess = { userId ->
                            Log.d("RegisterActivity", "Użytkownik zarejestrowany z ID: $userId")
                            val intent = Intent(this@RegisterActivity, SamplesActivity::class.java)
                            intent.putExtra("accessToken", userId)
                            startActivity(intent)
                            finish()
                        },
                        onFailure = { errorMessage ->
                            Log.e("RegisterActivity", "Rejestracja nie powiodła się: $errorMessage")
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onLoginActivityClick = {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onRegulationsClick = {

                },
                onPrivacyPolicyClick = {

                }
            )
        }
    }
}