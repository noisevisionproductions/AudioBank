package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.noisevisionproductions.samplelibrary.MainActivity
import org.noisevisionproductions.samplelibrary.interfaces.checkAndRequestStoragePermission

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
    val activity = LocalContext.current as? MainActivity

    Box(
        modifier = modifier
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

                if (activity != null) {
                    checkAndRequestStoragePermission(fileUrl, fileName, activity)
                }
            }) {
                Text("Zaimportuj do FL Studio")
            }
        }
    }
}

@Composable
actual fun PlatformAvatarImage(avatarPath: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(avatarPath)
            .crossfade(true)
            .build(),
        contentDescription = "User Avatar",
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .border(2.dp, MaterialTheme.colors.primary, CircleShape),
        contentScale = ContentScale.Crop
    )
}