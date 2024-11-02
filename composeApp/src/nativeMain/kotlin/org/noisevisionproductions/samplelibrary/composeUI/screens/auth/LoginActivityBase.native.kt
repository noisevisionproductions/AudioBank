package org.noisevisionproductions.samplelibrary.composeUI.screens.auth


import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewController
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertAction
import platform.UIKit.presentViewController
import platform.UIKit.dismissViewController
import org.noisevisionproductions.samplelibrary.LoginViewModel
import org.noisevisionproductions.samplelibrary.LoginViewModelFactory
import org.noisevisionproductions.samplelibrary.SamplesActivity
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.LoginViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.SamplesActivity

actual class LoginActivityBase : UIViewController() {

    private lateinit var authService: AuthService
    private lateinit var viewModel: LoginViewModel

    override fun viewDidLoad() {
        super.viewDidLoad()
        authService = AuthService()

        viewModel = LoginViewModelFactory(authService).create(LoginViewModel::class.java)

        // Since setContent is Android-specific, initialize the UI components directly
        setupLoginScreen()
    }

    private fun setupLoginScreen() {
        // Implement the login screen UI here or integrate with SwiftUI if desired
        // Call `performLogin` when login button is pressed
    }

    actual fun performLogin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModel.performLogin(email, password, onSuccess = { userId ->
            navigateToMain(userId)
        }, onFailure = { errorMessage ->
            displayError(errorMessage)
        })
    }

    actual fun navigateToRegistration() {
        val registrationViewController =
            RegisterActivityBase() // Assuming there's a RegisterActivityBase for iOS
        presentViewController(registrationViewController, animated = true, completion = null)
    }

    actual fun navigateToMain(userId: String) {
        val mainViewController = SamplesActivity()
        mainViewController.modalPresentationStyle = UIModalPresentationStyle.FullScreen
        presentViewController(mainViewController, animated = true, completion = null)
    }

    private fun displayError(errorMessage: String) {
        val alert = UIAlertController.alertControllerWithTitle(
            title = "Login Error",
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