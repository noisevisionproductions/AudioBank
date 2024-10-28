package org.noisevisionproductions.samplelibrary.composeUI.screens.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.PlatformAvatarImage
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors

object AvatarManager {
    private /*const*/ val DEFAULT_AVATAR_SIZE = 120.dp

    @Composable
    fun UserAvatar(
        avatarUrl: String?,
        size: Dp = DEFAULT_AVATAR_SIZE,
        onClick: (() -> Unit)? = null,
        showEditButton: Boolean = false
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                PlatformAvatarImage(avatarUrl)
            } else {
                Image(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(colors.primaryBackgroundColor)
                )
            }

            if (showEditButton) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Change Avatar",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(
                            color = MaterialTheme.colors.primary,
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    tint = MaterialTheme.colors.onPrimary
                )
            }
        }
    }
}