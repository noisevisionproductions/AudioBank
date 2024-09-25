package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.runtime.Composable

@Composable
actual fun PropertiesMenu(
    fileUrl: String,
    fileName: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    alignRight: Boolean
) {
}