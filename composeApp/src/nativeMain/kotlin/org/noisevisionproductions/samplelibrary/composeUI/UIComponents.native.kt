package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformAvatarImage(avatarPath: String) {
}

@Composable
actual fun PropertiesMenu(
    fileUrl: String,
    fileName: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    alignRight: Boolean,
    modifier: Modifier
) {
}