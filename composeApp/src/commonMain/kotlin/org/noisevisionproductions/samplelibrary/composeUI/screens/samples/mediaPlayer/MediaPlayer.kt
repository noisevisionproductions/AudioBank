package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.PropertiesMenu
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart
import samplelibrary.composeapp.generated.resources.icon_heart_filled
import samplelibrary.composeapp.generated.resources.icon_pause
import samplelibrary.composeapp.generated.resources.icon_play
import samplelibrary.composeapp.generated.resources.icon_properties_menu

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    songUrl: String,
    onPlayPauseClick: () -> Unit,
    iconColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Image(
        painterResource(if (isPlaying && currentlyPlayingUrl == songUrl) Res.drawable.icon_pause else Res.drawable.icon_play),
        contentDescription = if (isPlaying && currentlyPlayingUrl == songUrl) "Pause" else "Play",
        modifier = modifier
            .clickable { onPlayPauseClick() }
            .size(40.dp),
        colorFilter = iconColor?.let { ColorFilter.tint(it) }
    )
}

@Composable
fun MusicPlayerSlider(
    progress: Float,
    onValueChange: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    songId: String = "",
    isLiked: Boolean = false,
    onLikeClick: (String) -> Unit = {},
    songUrl: String = "",
    title: String = ""
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { if (songId.isNotEmpty()) onLikeClick(songId) }) {
            Image(
                painter = painterResource(
                    if (isLiked) Res.drawable.icon_heart_filled
                    else Res.drawable.icon_heart
                ),
                contentDescription = if (isLiked) "Unlike" else "Like",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(colors.barColor)
            )
        }

        Slider(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            value = progress,
            onValueChange = onValueChange,
            onValueChangeFinished = { onSeek(progress) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = colors.backgroundWhiteColor,
                activeTrackColor = Color(0xFF0F3F3F),
                inactiveTrackColor = Color(0xFF4C4C4C)
            )
        )

        var expanded by remember { mutableStateOf(false) }
        IconButton(onClick = { expanded = true }) {
            Image(
                painter = painterResource(Res.drawable.icon_properties_menu),
                contentDescription = "Properties",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(colors.propertiesColorBottomSheet)
            )
        }

        PropertiesMenu(
            fileUrl = songUrl,
            fileName = title,
            expanded = expanded,
            onDismiss = { expanded = false },
            onOptionSelected = { option -> println("Selected option: $option") },
            alignRight = true
        )
    }
}