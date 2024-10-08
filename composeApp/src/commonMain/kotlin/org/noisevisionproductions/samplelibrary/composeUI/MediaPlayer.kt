package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.FileMetadata
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

fun playNextSong(
    currentSongIndex: Int,
    fileListWithMetadata: List<FileMetadata>,
    onPlayPauseClick: (String, String) -> Unit
): Int {
    if (fileListWithMetadata.isNotEmpty()) {
        val nextIndex = (currentSongIndex + 1) % fileListWithMetadata.size
        val nextSong = fileListWithMetadata[nextIndex]
        onPlayPauseClick(nextSong.url, nextSong.fileName)
        return nextIndex
    }
    return currentSongIndex
}

fun playPreviousSong(
    currentSongIndex: Int,
    fileListWithMetadata: List<FileMetadata>,
    onPlayPauseClick: (String, String) -> Unit
): Int {
    if (fileListWithMetadata.isNotEmpty()) {
        val prevIndex =
            if (currentSongIndex - 1 < 0) fileListWithMetadata.lastIndex else currentSongIndex - 1
        val prevSong = fileListWithMetadata[prevIndex]
        onPlayPauseClick(prevSong.url, prevSong.fileName)
        return prevIndex
    }
    return currentSongIndex
}
