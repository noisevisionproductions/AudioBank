package org.noisevisionproductions.samplelibrary.auth


expect class AuthService() {
    suspend fun getCurrentUserId(): String?
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(username: String, email: String, password: String): Result<String>
}
