package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_pause
import samplelibrary.composeapp.generated.resources.icon_play

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    songUrl: String,
    onPlayPauseClick: () -> Unit,
    iconColor: Color? = null
) {
    Image(
        painterResource(if (isPlaying && currentlyPlayingUrl == songUrl) Res.drawable.icon_pause else Res.drawable.icon_play),
        contentDescription = if (isPlaying && currentlyPlayingUrl == songUrl) "Pause" else "Play",
        modifier = Modifier
            .clickable { onPlayPauseClick() }
            .size(40.dp),
        colorFilter = iconColor?.let { ColorFilter.tint(it) }
    )
}

@Composable
fun MusicPlayerSlider(
    progress: Float,
    onValueChange: (Float) -> Unit,
    onSeek: (Float) -> Unit
) {
    Slider(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryBackgroundColor)
            .padding(horizontal = 50.dp, vertical = 20.dp),
        value = progress,
        onValueChange = { newValue ->
            onValueChange(newValue)
        },
        onValueChangeFinished = {
            onSeek(progress)
        },
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(
            thumbColor = colors.backgroundWhiteColor,
            activeTrackColor = Color(0xFF0F3F3F),
            inactiveTrackColor = Color(0xFF4C4C4C)
        ),
    )
}

