package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.noisevisionproductions.samplelibrary.MainActivity
import org.noisevisionproductions.samplelibrary.interfaces.checkAndRequestStoragePermission

@Composable
actual fun PropertiesMenu(
    fileUrl: String,
    fileName: String,
    expanded: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit,
    alignRight: Boolean
) {
    val activity = LocalContext.current as? MainActivity

    Box(
        modifier = Modifier
            .wrapContentSize(if (alignRight) Alignment.TopEnd else Alignment.TopStart)
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onDismiss() },
            modifier = Modifier
        ) {
            DropdownMenuItem(onClick = {
                onOptionSelected("Zaimportuj")
                onDismiss()

                // Call the permission function with the passed activity context
                if (activity != null) {
                    // Trigger the side effects in a non-Composable context
                    checkAndRequestStoragePermission(fileUrl, fileName, activity)
                }
            }) {
                Text("Zaimportuj do FL Studio")
            }
        }
    }
}