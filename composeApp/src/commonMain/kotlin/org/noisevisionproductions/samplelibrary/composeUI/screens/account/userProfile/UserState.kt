package org.noisevisionproductions.samplelibrary.composeUI.screens.account.userProfile

import org.noisevisionproductions.samplelibrary.utils.models.UserModel

sealed class UserState {
    data object Loading : UserState()
    data class Success(val user: UserModel) : UserState()
}