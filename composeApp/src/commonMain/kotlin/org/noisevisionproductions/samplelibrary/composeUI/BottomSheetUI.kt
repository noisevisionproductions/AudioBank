package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart
import samplelibrary.composeapp.generated.resources.icon_next
import samplelibrary.composeapp.generated.resources.icon_previous
import samplelibrary.composeapp.generated.resources.icon_properties_menu


@Composable
fun BottomSheetCustomUI(
    title: String,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    songUrl: String,
    onPlayPauseClick: () -> Unit,
    progress: Float,
    onSliderChange: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.backgroundWhiteColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryBackgroundColor)
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = colors.barColor,
                        thickness = 2.dp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        color = colors.barColor,
                        text = title,
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(8.dp)
                    )
                    Divider(
                        color = colors.barColor,
                        thickness = 2.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryBackgroundColor)
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                val iconSize = 50.dp
                // First text (ton)
                Text(
                    text = "ton",
                    modifier = Modifier.weight(1f),
                    color = colors.hintTextColorLight
                )

                Spacer(modifier = Modifier.weight(1f))

                // Second text (bpm)
                Text(
                    text = "bpm",
                    modifier = Modifier.weight(1f),
                    color = colors.hintTextColorLight
                )
                Spacer(modifier = Modifier.weight(1f))

                // Previous button with weight
                Image(
                    painter = painterResource(Res.drawable.icon_previous),
                    contentDescription = "Previous sound",
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onPreviousClick() }
                        .weight(1f),
                    colorFilter = ColorFilter.tint(colors.textColorMain)
                )

                // Spacer to push elements apart
                Spacer(modifier = Modifier.weight(1f))

                // Play/Pause button centered
                PlayPauseButton(
                    isPlaying = isPlaying,
                    currentlyPlayingUrl = currentlyPlayingUrl,
                    songUrl = songUrl,
                    onPlayPauseClick = { onPlayPauseClick() },
                    iconColor = colors.textColorMain
                )

                // Spacer to push elements apart
                Spacer(modifier = Modifier.weight(1f))

                // Next button with weight
                Image(
                    painter = painterResource(Res.drawable.icon_next),
                    contentDescription = "Next sound",
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { onNextClick() }
                        .weight(1f),
                    colorFilter = ColorFilter.tint(colors.textColorMain)
                )
                Spacer(modifier = Modifier.weight(1f))

                // Favorite button
                Image(
                    painter = painterResource(Res.drawable.icon_heart),
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .size(iconSize)
                        .clickable { }
                        .weight(1f),
                    colorFilter = ColorFilter.tint(colors.barColor)
                )
                Spacer(modifier = Modifier.weight(1f))

                // Properties menu button
                var expanded by remember { mutableStateOf(false) }

                Image(
                    painter = painterResource(Res.drawable.icon_properties_menu),
                    contentDescription = "Properties menu from bottom sheet",
                    colorFilter = ColorFilter.tint(colors.propertiesColorBottomSheet),
                    modifier = Modifier
                        .clickable { expanded = true }
                        .weight(1f)
                )

                PropertiesMenu(
                    fileUrl = songUrl,
                    fileName = title,
                    expanded = expanded,
                    onDismiss = { expanded = false },
                    onOptionSelected = { option ->
                        println("Selected option: $option")
                    },
                    alignRight = true
                )
            }

            Row {
                MusicPlayerSlider(
                    progress = progress,
                    onValueChange = onSliderChange,
                    onSeek = onSeek
                )
            }
        }
    }
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

