package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
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
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.backgroundWhiteColor)
                .padding(16.dp)
        ) {
            // Header with drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = colors.backgroundDarkGrayColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            // Title
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = poppinsFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                ),
                color = colors.textColorMain,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Tags
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .background(
                                    color = colors.primaryBackgroundColorLight,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                                style = TextStyle(fontSize = 12.sp),
                                color = colors.textColorMain
                            )
                        }
                    }
                }
            }

            // Music info and controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tone & BPM
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        InfoItem(label = "TON", value = tone)
                        InfoItem(label = "BPM", value = bpm)
                    }
                }
            }

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onPreviousClick() }) {
                    Image(
                        painter = painterResource(Res.drawable.icon_previous),
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(colors.textColorMain)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(colors.primaryBackgroundColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PlayPauseButton(
                        isPlaying = isPlaying,
                        currentlyPlayingUrl = currentlyPlayingUrl,
                        songUrl = songUrl,
                        onPlayPauseClick = onPlayPauseClick,
                        iconColor = colors.backgroundWhiteColor
                    )
                }

                IconButton(onClick = { onNextClick() }) {
                    Image(
                        painter = painterResource(Res.drawable.icon_next),
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(colors.textColorMain)
                    )
                }
            }

            // Progress slider
            MusicPlayerSlider(
                progress = progress,
                onValueChange = onSliderChange,
                onSeek = onSeek,
                songId = songId,
                isLiked = isLiked,
                onLikeClick = onLikeClick,
                songUrl = songUrl,
                title = title,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colors.textColorMain
        )
        Text(
            text = label,
            style = TextStyle(fontSize = 12.sp),
            color = colors.hintTextColorMedium
        )
    }
}