package org.noisevisionproductions.samplelibrary.composeUI.screens.auth

// In iosMain

import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertAction
import platform.UIKit.presentViewController
import platform.UIKit.dismissViewController
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModel
import org.noisevisionproductions.samplelibrary.auth.RegisterViewModelFactory
import org.noisevisionproductions.samplelibrary.composeUI.screens.SamplesActivity

actual class RegisterActivityBase : UIViewController() {

    private lateinit var authService: AuthService
    private lateinit var viewModel: RegisterViewModel

    override fun viewDidLoad() {
        super.viewDidLoad()
        authService = AuthService()

        viewModel = RegisterViewModelFactory(authService).create(RegisterViewModel::class.java)

        // Initialize the UI for the registration screen
        setupRegisterScreen()
    }

    private fun setupRegisterScreen() {
        // Implement the registration screen UI here or integrate with SwiftUI if desired
        // Call `performRegister` when the registration button is pressed
    }

    actual fun performRegister(
        nickname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModel.performRegister(
            nickname,
            email,
            password,
            confirmPassword,
            onSuccess = { userId ->
                navigateToMain(userId)
            },
            onFailure = { errorMessage ->
                displayError(errorMessage)
            })
    }

    actual fun navigateToLogin() {
        val loginViewController =
            LoginActivityBase() // Assuming there's a LoginActivityBase for iOS
        presentViewController(loginViewController, animated = true, completion = null)
    }

    actual fun navigateToMain(userId: String) {
        val mainViewController = SamplesActivity()
        mainViewController.modalPresentationStyle = UIModalPresentationStyle.FullScreen
        presentViewController(mainViewController, animated = true, completion = null)
    }

    private fun displayError(errorMessage: String) {
        val alert = UIAlertController.alertControllerWithTitle(
            title = "Registration Error",
            message = errorMessage,
            preferredStyle = UIAlertControllerStyleAlert
        )
        alert.addAction(
            UIAlertAction.actionWithTitle(
                "OK",
                style = UIAlertActionStyleDefault,
                handler = null
            )
        )
        presentViewController(alert, animated = true, completion = null)
    }
}
