package org.noisevisionproductions.samplelibrary.composeUI.screens.samples

sealed class SoundScreenNavigation {
    data object SoundList : SoundScreenNavigation()
    data object UploadSound : SoundScreenNavigation()
}