package org.noisevisionproductions.samplelibrary.database

import org.noisevisionproductions.samplelibrary.utils.models.UserModel

actual class UserRepository {
    actual suspend fun getCurrentUser(userId: String): Result<UserModel?> {
        TODO("Not yet implemented")
    }

    actual suspend fun getUsernameById(userId: String): String? {
        TODO("Not yet implemented")
    }

    actual suspend fun getUserLabelById(userId: String): String? {
        TODO("Not yet implemented")
    }
}