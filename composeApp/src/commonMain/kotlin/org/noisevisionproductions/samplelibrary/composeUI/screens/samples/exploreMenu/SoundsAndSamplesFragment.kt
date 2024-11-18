package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.DropDownMenuWithItems
import org.noisevisionproductions.samplelibrary.composeUI.PropertiesMenu
import org.noisevisionproductions.samplelibrary.composeUI.RowWithSearchBar
import org.noisevisionproductions.samplelibrary.composeUI.ScrollingText
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.SoundScreenNavigation
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.MusicPlayerViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.PlayPauseButton
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadNewSound
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.interfaces.poppinsFontFamily
import org.noisevisionproductions.samplelibrary.utils.TagRepository
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata
import org.noisevisionproductions.samplelibrary.utils.decodeUrl
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart
import samplelibrary.composeapp.generated.resources.icon_heart_filled
import samplelibrary.composeapp.generated.resources.icon_properties_menu

@Composable
fun SoundNavigationHost(
    dynamicListViewModel: DynamicListViewModel,
    musicPlayerViewModel: MusicPlayerViewModel,
    filePicker: FilePicker,
    uploadSoundViewModel: UploadSoundViewModel
) {
    var currentScreen by remember { mutableStateOf<SoundScreenNavigation>(SoundScreenNavigation.SoundList) }

    when (currentScreen) {
        SoundScreenNavigation.SoundList -> {
            SoundListScreen(
                dynamicListViewModel = dynamicListViewModel,
                musicPlayerViewModel = musicPlayerViewModel,
                onNavigateToUpload = { currentScreen = SoundScreenNavigation.UploadSound }
            )
        }

        SoundScreenNavigation.UploadSound -> {
            UploadSoundScreen(
                uploadSoundViewModel = uploadSoundViewModel,
                filePicker = filePicker,
                onNavigateBack = {
                    uploadSoundViewModel.clearSelectedFiles()
                    currentScreen = SoundScreenNavigation.SoundList
                }
            )
        }
    }
}

@Composable
fun UploadSoundScreen(
    uploadSoundViewModel: UploadSoundViewModel,
    filePicker: FilePicker,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundWhiteColor)
    ) {
        CustomTopAppBar(
            title = "Dodaj nowy dźwięk",
            onNavigateBack = onNavigateBack
        )

        UploadNewSound(
            uploadSoundViewModel = uploadSoundViewModel,
            filePicker = filePicker
        )
    }
}

@Composable
fun SampleListHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryBackgroundColorLight)
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nazwa",
                style = TextStyle(
                    fontFamily = poppinsFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = colors.textColorMain,
                modifier = Modifier.weight(2f),
                textAlign = TextAlign.Start
            )
            Text(
                text = "Czas",
                style = TextStyle(
                    fontFamily = poppinsFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = colors.textColorMain,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = "Ton",
                style = TextStyle(
                    fontFamily = poppinsFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = colors.textColorMain,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Text(
                text = "BPM",
                style = TextStyle(
                    fontFamily = poppinsFontFamily(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = colors.textColorMain,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.width(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SoundListScreen(
    dynamicListViewModel: DynamicListViewModel,
    musicPlayerViewModel: MusicPlayerViewModel,
    onNavigateToUpload: () -> Unit
) {
    val uiState by dynamicListViewModel.uiState.collectAsState()
    val playerState by musicPlayerViewModel.playerState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val listState = rememberLazyListState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            BottomSheetCustomUI(
                title = playerState.selectedFileName ?: "No File Selected",
                isPlaying = playerState.isPlaying,
                currentlyPlayingUrl = playerState.currentlyPlayingUrl,
                songId = playerState.songId,
                songUrl = playerState.currentlyPlayingUrl ?: "",
                bpm = playerState.bpm,
                tone = playerState.tone,
                tags = playerState.tags,
                isLiked = uiState.likedSounds.contains(playerState.songId),
                onPlayPauseClick = {
                    musicPlayerViewModel.handleAction(
                        MusicPlayerViewModel.PlayerAction.PlayPause(
                            playerState.currentlyPlayingUrl ?: "",
                            playerState.selectedFileName ?: "",
                            playerState.currentSongIndex,
                            playerState.bpm,
                            playerState.tone,
                        ),
                        uiState.filteredFileList
                    )
                },
                progress = playerState.progress,
                onSliderChange = { newProgress ->
                    musicPlayerViewModel.handleAction(
                        MusicPlayerViewModel.PlayerAction.Seek(newProgress),
                        uiState.filteredFileList
                    )
                },
                onSeek = { position ->
                    musicPlayerViewModel.handleAction(
                        MusicPlayerViewModel.PlayerAction.Seek(position),
                        uiState.filteredFileList
                    )
                },
                onNextClick = {
                    musicPlayerViewModel.handleAction(
                        MusicPlayerViewModel.PlayerAction.Next,
                        uiState.filteredFileList
                    )
                },
                onPreviousClick = {
                    musicPlayerViewModel.handleAction(
                        MusicPlayerViewModel.PlayerAction.Previous,
                        uiState.filteredFileList
                    )
                },
                onLikeClick = { songId ->
                    dynamicListViewModel.toggleSoundLike(songId)
                }
            )
        },
        sheetPeekHeight = 70.dp,
        sheetBackgroundColor = Color.Transparent,
        backgroundColor = Color.Transparent,
        sheetElevation = 0.dp,
    ) {
        SampleListContent(
            uiState = uiState,
            isPlaying = playerState.isPlaying,
            currentlyPlayingUrl = playerState.currentlyPlayingUrl,
            onPlayPauseClick = { songUrl, fileName, bpm, tone, songId, tags ->
                musicPlayerViewModel.handleAction(
                    MusicPlayerViewModel.PlayerAction.PlayPause(
                        songUrl,
                        fileName,
                        uiState.filteredFileList.indexOfFirst { it.url == songUrl },
                        bpm = bpm,
                        tone = tone,
                        songId = songId,
                        tags = tags
                    ),
                    uiState.filteredFileList,
                )
            },
            onLoadMoreFiles = {
                dynamicListViewModel.handleAction(DynamicListViewModel.ListAction.LoadMore)
            },
            listState = listState,
            onNavigateToUpload = onNavigateToUpload,
            dynamicListViewModel = dynamicListViewModel
        )
    }
}

@Composable
fun SampleListContent(
    uiState: DynamicListViewModel.UiState,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    onPlayPauseClick: (String, String, String, String, String, List<String>) -> Unit,
    onLoadMoreFiles: () -> Unit,
    listState: LazyListState,
    onNavigateToUpload: () -> Unit,
    dynamicListViewModel: DynamicListViewModel
) {

    val tags = TagRepository.getTagsFromJsonFile()

    val filters: @Composable () -> Unit = {
        DropDownMenuWithItems(
            defaultLabel = "Sortowanie",
            options = listOf("Najnowsze", "Najstarsze"),
            onItemSelected = { sortingOption ->
                dynamicListViewModel.handleAction(
                    DynamicListViewModel.ListAction.SetSortOption(
                        when (sortingOption) {
                            "Najnowsze" -> DynamicListViewModel.SortOption.NEWEST
                            "Najstarsze" -> DynamicListViewModel.SortOption.OLDEST
                            else -> DynamicListViewModel.SortOption.NEWEST
                        }
                    )
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundGrayColor)
    ) {
        RowWithSearchBar(
            placeholderText = "Wyszukaj dźwięki",
            onSearchTextChanged = { query ->
                dynamicListViewModel.handleAction(DynamicListViewModel.ListAction.Search(query))
            },
            onChangeContent = onNavigateToUpload,
            filters = filters,
            tags = tags,
            selectedTags = uiState.selectedTags,
            onTagSelected = { tag -> dynamicListViewModel.toggleTag(tag) },
            onResetTags = { dynamicListViewModel.clearTags() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            MainContentWithSounds(
                isLoading = uiState.isLoading,
                fileList = uiState.filteredFileList,
                isPlaying = isPlaying,
                currentlyPlayingUrl = currentlyPlayingUrl,
                onPlayPauseClick = onPlayPauseClick,
                onLoadMoreFiles = onLoadMoreFiles,
                listState = listState,
                dynamicListViewModel = dynamicListViewModel,
                likedSounds = uiState.likedSounds
            )
        }
    }
}

@Composable
fun MainContentWithSounds(
    isLoading: Boolean,
    fileList: List<AudioMetadata>,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    onPlayPauseClick: (String, String, String, String, String, List<String>) -> Unit,
    onLoadMoreFiles: () -> Unit,
    listState: LazyListState,
    dynamicListViewModel: DynamicListViewModel,
    likedSounds: Set<String>
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
            ) {
                items(
                    fileList.size,
                    key = { index -> (fileList[index].url ?: "") + index }
                ) { index ->
                    val fileMetadata = fileList[index]
                    val songUrl = fileMetadata.url ?: ""
                    val isLiked = likedSounds.contains(fileMetadata.id)

                    SampleListItem(
                        soundId = fileMetadata.id ?: "",
                        fileName = decodeUrl(fileMetadata.url ?: ""),
                        songId = fileMetadata.id ?: "",
                        tags = fileMetadata.tags,
                        songUrl = fileMetadata.url ?: "",
                        tone = if (fileMetadata.tone.isNullOrEmpty()) "-" else fileMetadata.tone,
                        bpm = if (fileMetadata.bpm.isNullOrEmpty()) "-" else fileMetadata.bpm,
                        duration = if (fileMetadata.duration.isNullOrEmpty()) "-" else fileMetadata.duration,
                        isPlaying = (isPlaying && currentlyPlayingUrl == songUrl),
                        currentlyPlayingUrl = currentlyPlayingUrl,
                        onPlayPauseClick = onPlayPauseClick,
                        isLiked = isLiked,
                        onLikeClick = { soundId ->
                            dynamicListViewModel.toggleSoundLike(soundId)
                        }
                    )

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

@Composable
fun SampleListItem(
    soundId: String,
    fileName: String,
    tags: List<String>,
    songId: String,
    songUrl: String,
    tone: String,
    bpm: String,
    duration: String,
    isPlaying: Boolean,
    isLiked: Boolean,
    currentlyPlayingUrl: String?,
    onPlayPauseClick: (String, String, String, String, String, List<String>) -> Unit,
    onLikeClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (currentlyPlayingUrl == songUrl) colors.primaryBackgroundColorLight.copy(
                    alpha = 0.3f
                ) else Color.Transparent
            )
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(40.dp)) {
            PlayPauseButton(
                isPlaying = (isPlaying && currentlyPlayingUrl == songUrl),
                currentlyPlayingUrl = currentlyPlayingUrl,
                songUrl = songUrl,
                onPlayPauseClick = {
                    onPlayPauseClick(songUrl, fileName, bpm, tone, songId, tags)
                },
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(3f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = fileName,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (tags.isNotEmpty()) {
                Text(
                    text = tags.joinToString(", "),
                    style = TextStyle(fontSize = 12.sp),
                    color = colors.hintTextColorMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = duration,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body2

        )
        Text(
            text = tone,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body2
        )
        Text(
            text = bpm,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            style = MaterialTheme.typography.body2
        )

        Row(
            modifier = Modifier.width(80.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onLikeClick(soundId) },
                modifier = Modifier.size(32.dp)
            ) {
                Image(
                    painter = painterResource(
                        if (isLiked) Res.drawable.icon_heart_filled
                        else Res.drawable.icon_heart
                    ),
                    contentDescription = if (isLiked) "Unfavorite" else "Favorite",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(if (isLiked) colors.barColor else colors.textColorMain)
                )
            }

            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.icon_properties_menu),
                    contentDescription = "Properties menu",
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(colors.textColorMain)
                )
            }
        }

        PropertiesMenu(
            fileUrl = songUrl,
            fileName = fileName,
            expanded = expanded,
            onDismiss = { expanded = false },
            onOptionSelected = { option -> println("Selected option: $option") },
            alignRight = true
        )
    }

    Divider(
        color = colors.dividerLightGray,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}