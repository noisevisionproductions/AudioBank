package org.noisevisionproductions.samplelibrary.composeUI.screens.account

sealed class AccountScreenNavigation {
    data object AccountFragment : AccountScreenNavigation()
    data object AccountEditScreen : AccountScreenNavigation()
    data object UserSoundsScreen : AccountScreenNavigation()
}