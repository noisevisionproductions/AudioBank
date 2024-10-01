package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.interfaces.AzureStorageService
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart
import samplelibrary.composeapp.generated.resources.icon_heart_filled
import samplelibrary.composeapp.generated.resources.icon_pause
import samplelibrary.composeapp.generated.resources.icon_play
import samplelibrary.composeapp.generated.resources.icon_properties_menu

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DynamicListWithSamples(directoryPath: String) {
    val azureStorageService = AzureStorageService()
    val musicPlayerService = MusicPlayerService()

    var continuationToken by remember { mutableStateOf<String?>(null) }
    var fileList by remember { mutableStateOf<List<String>>(emptyList()) }
    var filteredFileList by remember { mutableStateOf<List<String>>(emptyList()) }
    var songsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var filteredSongsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var durations by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    var isPlaying by remember { mutableStateOf(false) }
    var currentlyPlayingUrl by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(0.5f) }
    var currentSongIndex by remember { mutableStateOf(0) }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var noMoreFilesToLoad by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // When activity / fragment is destroyed then it stops playing sound
    DisposableEffect(Unit) {
        onDispose {
            if (isPlaying) {
                musicPlayerService.stopAudio()
            }
        }
    }
    // Function to handle play/pause action
    val onPlayPauseClick: (String, String) -> Unit = { songUrl, fileName ->
        coroutineScope.launch {
            currentSongIndex = songsList.indexOf(
                songUrl
            )
            if (isPlaying && currentlyPlayingUrl == songUrl) {
                musicPlayerService.pauseAudio()
                isPlaying = false
                currentlyPlayingUrl = null
            } else {
                if (currentlyPlayingUrl != null && currentlyPlayingUrl != songUrl) {
                    musicPlayerService.stopAudio()
                }

                musicPlayerService.playAudioFromUrl(
                    audioUrl = songUrl, onCompletion = {
                        isPlaying = false
                        progress = 0f
                    }, onProgressUpdate = { newProgress ->
                        progress = newProgress
                    })
                isPlaying = true
                currentlyPlayingUrl = songUrl
                selectedFileName = fileName
            }
        }
    }

    val playNextSongClick = {
        currentSongIndex = playNextSong(
            currentSongIndex = currentSongIndex,
            songsList = songsList,
            fileList = fileList,
            onPlayPauseClick = onPlayPauseClick
        )
    }

    val playPreviousSongClick = {
        currentSongIndex = playPreviousSong(
            currentSongIndex = currentSongIndex,
            songsList = songsList,
            fileList = fileList,
            onPlayPauseClick = onPlayPauseClick
        )
    }

    val onSeek: (Float) -> Unit = { newProgress ->
        coroutineScope.launch {
            musicPlayerService.seekTo(newProgress)
        }
    }

    fun applySearchFilter(query: String) {
        if (query.isNotEmpty()) {
            filteredFileList = fileList.filter { it.contains(query, ignoreCase = true) }
            filteredSongsList = songsList.filter { song ->
                val fileName = song.substringAfterLast("/")
                fileName.contains(query, ignoreCase = true)
            }
        } else {
            filteredFileList = fileList
            filteredSongsList = songsList
        }
    }

    val onSearchTextChanged: (String) -> Unit = { query ->
        searchQuery = query
        applySearchFilter(query)
    }

    fun loadMoreFiles() {
        if (noMoreFilesToLoad || isLoading) {
            return
        }
        coroutineScope.launch {
            isLoading = true
            try {
                val (newFiles, newToken) = azureStorageService.listFilesInBucket(
                    directoryPath, continuationToken ?: ""
                )

                fileList = fileList + newFiles.map { it.substringAfterLast("/") }
                songsList = songsList + newFiles
                continuationToken = newToken

                if (continuationToken == null) {
                    noMoreFilesToLoad = true
                }

                if (searchQuery.isEmpty()) {
                    filteredFileList = fileList
                    filteredSongsList = songsList
                }
            } catch (e: Exception) {
                println("Error fetching files: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadMoreFiles()
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                if (!isLoading && index >= fileList.size - 1 && continuationToken != null) {
                    loadMoreFiles()
                }
            }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            applySearchFilter(searchQuery)
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetCustomUI(
                title = selectedFileName ?: "No File Selected",
                isPlaying = isPlaying,
                currentlyPlayingUrl = currentlyPlayingUrl,
                songUrl = currentlyPlayingUrl ?: "",
                onPlayPauseClick = {
                    if (isPlaying) {
                        // Stop the audio when it's playing
                        musicPlayerService.pauseAudio()
                        isPlaying = false
                    } else if (currentlyPlayingUrl != null) {
                        // Resume the last played song
                        coroutineScope.launch {
                            musicPlayerService.playAudioFromUrl(
                                audioUrl = currentlyPlayingUrl!!,
                                onCompletion = {
                                    isPlaying = false
                                    progress = 0f
                                },
                                onProgressUpdate = { newProgress ->
                                    progress = newProgress
                                }
                            )
                            isPlaying = true
                        }
                    }
                },
                progress = progress,
                onSliderChange = { newProgress ->
                    progress = newProgress
                },
                onSeek = onSeek,
                onNextClick = playNextSongClick,
                onPreviousClick = playPreviousSongClick
            )
        },
        sheetPeekHeight = 70.dp,
        content = {
            SampleListContent(
                isLoading,
                fileList = filteredFileList,
                songsList = filteredSongsList,
                durations = durations,
                isPlaying = isPlaying,
                currentlyPlayingUrl = currentlyPlayingUrl,
                onPlayPauseClick = onPlayPauseClick,
                onLoadMoreFiles = ::loadMoreFiles,
                onSearchTextChanged = onSearchTextChanged,
                listState = listState
            )
        },
        sheetElevation = 0.dp // Removing shadow that appears automatically along with bottom sheet
    )
}

@Composable
fun SampleListItem(
    fileName: String,
    songUrl: String,
    durationSeconds: Int,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    onPlayPauseClick: (String, String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayPauseButton(
            isPlaying = (isPlaying && currentlyPlayingUrl == songUrl),
            currentlyPlayingUrl = currentlyPlayingUrl,
            songUrl = songUrl,
            onPlayPauseClick = { onPlayPauseClick(songUrl, fileName) }
        )
        ScrollingText(
            fileName = fileName,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = displayFormattedDuration(durationSeconds),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "ton",
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "bpm",
            modifier = Modifier.weight(1f)
        )

        var isFavorite by remember { mutableStateOf(false) }
        Image(
            painterResource(if (isFavorite) Res.drawable.icon_heart_filled else Res.drawable.icon_heart),
            contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
            modifier = Modifier.weight(1f)
                .size(40.dp)
                .clickable {
                    isFavorite = !isFavorite
                }
        )

        var expanded by remember { mutableStateOf(false) }

        Image(
            painterResource(Res.drawable.icon_properties_menu),
            contentDescription = "Properties menu",
            modifier = Modifier
                .weight(1f)
                .size(40.dp)
                .clickable { expanded = true }
        )
        PropertiesMenu(
            fileUrl = songUrl,
            fileName = fileName,
            expanded = expanded,
            onDismiss = { expanded = false },
            onOptionSelected = { option ->
                println("Selected option: $option")
            },
            alignRight = true
        )
    }
}

@Composable
fun SampleListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp),
        ) {}
        Text(
            text = "Nazwa",
            fontSize = 15.sp,
            color = colors.hintTextColorLight,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Czas",
            fontSize = 15.sp,
            color = colors.hintTextColorLight,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Ton",
            fontSize = 15.sp,
            color = colors.hintTextColorLight,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "BPM",
            fontSize = 15.sp,
            color = colors.hintTextColorLight,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier.weight(1f)
        ) {}
        Box(
            modifier = Modifier.weight(1f)
        ) {}
    }
}

@Composable
fun SampleListContent(
    isLoading: Boolean,
    fileList: List<String>,
    songsList: List<String>,
    durations: Map<String, Int>,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    onPlayPauseClick: (String, String) -> Unit,
    onLoadMoreFiles: () -> Unit,
    onSearchTextChanged: (String) -> Unit,
    listState: LazyListState
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundGrayColor)
    ) {
        RowWithSearchBar(
            labelText = "Dźwięki",
            placeholderText = "Wyszukaj dźwięki",
            onSearchTextChanged = onSearchTextChanged
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            Column {
                SampleListHeader()

                Divider(
                    color = colors.textColorMain,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                if (fileList.isEmpty() && isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = colors.barColor)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Ładowanie dzwięków...",
                                color = colors.hintTextColorLight
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(fileList.size, key = { index -> fileList[index] + index }) { index ->
                            val fileUrl = fileList[index]
                            val fileName = decodeFirestoreUrl(fileUrl)

                            val songUrl = songsList.getOrNull(index) ?: ""
                            val durationSeconds = durations[songUrl] ?: 0

                            if (songUrl.isNotEmpty()) {
                                SampleListItem(
                                    fileName = fileName,
                                    songUrl = songUrl,
                                    durationSeconds = durationSeconds,
                                    isPlaying = (isPlaying && currentlyPlayingUrl == songUrl),
                                    currentlyPlayingUrl = currentlyPlayingUrl,
                                    onPlayPauseClick = onPlayPauseClick
                                )
                            }

                            Divider(
                                color = colors.dividerLightGray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = colors.barColor,
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .width(8.dp)
                                    )
                                    Text(
                                        text = "Ładowanie więcej dzwięków...",
                                        color = colors.hintTextColorLight
                                    )
                                }
                            }
                        }
                        item {
                            LaunchedEffect(Unit) {
                                onLoadMoreFiles()
                            }
                        }
                    }
                }
            }
        }
    }
}

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
    songsList: List<String>,
    fileList: List<String>,
    onPlayPauseClick: (String, String) -> Unit
): Int {
    if (songsList.isNotEmpty()) {
        val nextIndex = (currentSongIndex + 1) % songsList.size
        val nextSongUrl = songsList[nextIndex]
        val nextFileName = decodeFirestoreUrl(fileList[nextIndex])
        onPlayPauseClick(nextSongUrl, nextFileName)
        return nextIndex
    }
    return currentSongIndex
}

fun playPreviousSong(
    currentSongIndex: Int,
    songsList: List<String>,
    fileList: List<String>,
    onPlayPauseClick: (String, String) -> Unit
): Int {
    if (songsList.isNotEmpty()) {
        val prevIndex = if (currentSongIndex - 1 < 0) songsList.lastIndex else currentSongIndex - 1
        val prevSongUrl = songsList[prevIndex]
        val prevFileName = decodeFirestoreUrl(fileList[prevIndex])
        onPlayPauseClick(prevSongUrl, prevFileName)
        return prevIndex
    }
    return currentSongIndex
}


