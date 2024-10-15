package org.noisevisionproductions.samplelibrary.auth

import org.noisevisionproductions.samplelibrary.utils.models.UserModel

expect class AuthService() {
    suspend fun getCurrentUserId(): String?
    suspend fun getUserData(): Result<UserModel?>
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(username: String, email: String, password: String): Result<String>
}
