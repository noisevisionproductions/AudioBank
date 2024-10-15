package org.noisevisionproductions.samplelibrary.auth

actual class AuthService {
    actual suspend fun signIn(
        email: String,
        password: String
    ): Result<String> {
        TODO("Not yet implemented")
    }

    actual suspend fun getCurrentUserId(): String? {
        TODO("Not yet implemented")
    }

    actual suspend fun signUp(
        username: String,
        email: String,
        password: String
    ): Result<String> {
        TODO("Not yet implemented")
    }
}