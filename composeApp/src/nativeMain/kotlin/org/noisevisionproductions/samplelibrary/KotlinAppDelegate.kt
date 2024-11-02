package org.noisevisionproductions.samplelibrary

import org.noisevisionproductions.samplelibrary.auth.AuthService

class KotlinAppDelegate {

    lateinit var authService: AuthService

    fun setup() {
        authService = AuthService()

        if (authService.isUserLoggedIn()) {
            navigateToMainMenu()
        } else {
            navigateToLogin()
        }
    }

    private fun navigateToMainMenu() {
        println("Navigating to Main Menu")
    }

    private fun navigateToLogin() {
        println("Navigating to Login")
    }
}
