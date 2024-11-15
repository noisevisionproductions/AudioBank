package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors
import org.noisevisionproductions.samplelibrary.composeUI.PropertiesMenu
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.MusicPlayerSlider
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.PlayPauseButton
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.interfaces.poppinsFontFamily
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart
import samplelibrary.composeapp.generated.resources.icon_heart_filled
import samplelibrary.composeapp.generated.resources.icon_next
import samplelibrary.composeapp.generated.resources.icon_previous
import samplelibrary.composeapp.generated.resources.icon_properties_menu

@Composable
fun BottomSheetCustomUI(
    title: String,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    songId: String,
    songUrl: String,
    bpm: String,
    tone: String,
    tags: List<String>,
    isLiked: Boolean,
    onPlayPauseClick: () -> Unit,
    progress: Float,
    onSliderChange: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onLikeClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
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
                        color = CustomColors.primary100,
                        thickness = 2.dp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = title,
                        style = TextStyle(
                            fontFamily = poppinsFontFamily(),
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        ),
                        color = CustomColors.primary100,
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(4f),
                        textAlign = TextAlign.Center
                    )
                    Divider(
                        color = CustomColors.primary100,
                        thickness = 2.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryBackgroundColor)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = when (tags.size) {
                    1 -> Arrangement.Center
                    2 -> Arrangement.spacedBy(100.dp, Alignment.CenterHorizontally)
                    else -> Arrangement.SpaceEvenly
                }
            ) {
                tags.forEach { tag ->
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryBackgroundColor)
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Tone
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(48.dp)
                ) {
                    Text(
                        text = tone,
                        style = MaterialTheme.typography.body1,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "TON",
                        style = MaterialTheme.typography.body2,
                        fontSize = 10.sp
                    )
                }

                // BPM
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(48.dp)
                ) {
                    Text(
                        text = bpm,
                        style = MaterialTheme.typography.body1,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "BPM",
                        style = MaterialTheme.typography.body2,
                        fontSize = 10.sp
                    )
                }

                // Playback controls group
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.icon_previous),
                        contentDescription = "Previous sound",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onPreviousClick() },
                        colorFilter = ColorFilter.tint(colors.textColorMain)
                    )

                    PlayPauseButton(
                        isPlaying = isPlaying,
                        currentlyPlayingUrl = currentlyPlayingUrl,
                        songUrl = songUrl,
                        onPlayPauseClick = {
                            if (songUrl.isNotEmpty()) {
                                onPlayPauseClick()
                            }
                        },
                        iconColor = colors.textColorMain
                    )

                    Image(
                        painter = painterResource(Res.drawable.icon_next),
                        contentDescription = "Next sound",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onNextClick() },
                        colorFilter = ColorFilter.tint(colors.textColorMain)
                    )
                }

                // Heart icon
                Image(
                    painter = painterResource(if (isLiked) Res.drawable.icon_heart_filled else Res.drawable.icon_heart),
                    contentDescription = if (isLiked) "Unfavorite" else "Favorite",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(enabled = songId.isNotEmpty()) { onLikeClick(songId) },
                    colorFilter = ColorFilter.tint(colors.barColor)
                )

                // Properties menu
                var expanded by remember { mutableStateOf(false) }
                Image(
                    painter = painterResource(Res.drawable.icon_properties_menu),
                    contentDescription = "Properties menu from bottom sheet",
                    colorFilter = ColorFilter.tint(colors.propertiesColorBottomSheet),
                    modifier = Modifier
                        .clickable { expanded = true }
                        .size(40.dp)
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