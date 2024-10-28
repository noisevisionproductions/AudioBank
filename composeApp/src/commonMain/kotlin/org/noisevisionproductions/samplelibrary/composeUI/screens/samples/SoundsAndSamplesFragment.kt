package org.noisevisionproductions.samplelibrary.composeUI.screens.samples

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.DropDownMenuWithItems
import org.noisevisionproductions.samplelibrary.composeUI.PropertiesMenu
import org.noisevisionproductions.samplelibrary.composeUI.RowWithSearchBar
import org.noisevisionproductions.samplelibrary.composeUI.ScrollingText
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.MusicPlayerViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.PlayPauseButton
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadNewSound
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker
import org.noisevisionproductions.samplelibrary.utils.TagRepository
import org.noisevisionproductions.samplelibrary.utils.dataClasses.AudioMetadata
import org.noisevisionproductions.samplelibrary.utils.decodeUrl
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
                onNavigateBack = { currentScreen = SoundScreenNavigation.SoundList }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = "Nazwa",
            fontSize = 15.sp,
            color = colors.hintTextColorLight,
            modifier = Modifier.weight(3f),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Czas",
            fontSize = 15.sp,
            color = colors.hintTextColorLight,
            modifier = Modifier.weight(1f)
        )
        /* Text(
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
         )*/
        Box(
            modifier = Modifier.weight(1f)
        ) {}
        Box(
            modifier = Modifier.weight(1f)
        ) {}
    }
}

@Composable
fun SampleListItem(
    fileName: String,
    songUrl: String,
    duration: String,
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
            modifier = Modifier.weight(3f)
        )
        Text(
            text = duration,
            modifier = Modifier.weight(1f)
        )
        /*   Text(
               text = "ton",
               modifier = Modifier.weight(1f)
           )
           Text(
               text = "bpm",
               modifier = Modifier.weight(1f)
           )*/

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
fun SampleListContent(
    isLoading: Boolean,
    fileList: List<AudioMetadata>,
    isPlaying: Boolean,
    currentlyPlayingUrl: String?,
    onPlayPauseClick: (String, String) -> Unit,
    onLoadMoreFiles: () -> Unit,
    onSearchTextChanged: (String) -> Unit,
    listState: LazyListState,
    onNavigateToUpload: () -> Unit
) {

    val tags = TagRepository.getTagsFromJsonFile()

    val filters: @Composable () -> Unit = {
        DropDownMenuWithItems(
            label = "Instrumenty",
            options = listOf("Gitara", "Pianino", "Perkusja"),
            onItemSelected = { }
        )
        DropDownMenuWithItems(
            label = "Sortowanie",
            options = listOf("Najnowsze", "Najstarsze"),
            onItemSelected = { sortingOption ->

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
            onSearchTextChanged = onSearchTextChanged,
            onChangeContent = onNavigateToUpload,
            filters = filters,
            tags = tags
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            MainContentWithSounds(
                isLoading = isLoading,
                fileList = fileList,
                isPlaying = isPlaying,
                currentlyPlayingUrl = currentlyPlayingUrl,
                onPlayPauseClick = onPlayPauseClick,
                onLoadMoreFiles = onLoadMoreFiles,
                listState = listState
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
    onPlayPauseClick: (String, String) -> Unit,
    onLoadMoreFiles: () -> Unit,
    listState: LazyListState
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
                items(
                    fileList.size,
                    key = { index -> (fileList[index].url ?: "") + index }
                ) { index ->
                    val fileMetadata = fileList[index]
                    val fileName = decodeUrl(fileMetadata.url ?: "")
                    val songUrl = fileMetadata.url ?: ""
                    val duration = fileMetadata.duration

                    if (songUrl.isNotEmpty()) {
                        if (duration != null) {
                            SampleListItem(
                                fileName = fileName,
                                songUrl = songUrl,
                                duration = duration,
                                isPlaying = (isPlaying && currentlyPlayingUrl == songUrl),
                                currentlyPlayingUrl = currentlyPlayingUrl,
                                onPlayPauseClick = onPlayPauseClick
                            )
                        }
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
                songUrl = playerState.currentlyPlayingUrl ?: "",
                onPlayPauseClick = {
                    musicPlayerViewModel.handleAction(
                        MusicPlayerViewModel.PlayerAction.PlayPause(
                            playerState.currentlyPlayingUrl ?: "",
                            playerState.selectedFileName ?: "",
                            playerState.currentSongIndex
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
                }
            )
        },
        sheetPeekHeight = 70.dp
    ) {
        SampleListContent(
            isLoading = uiState.isLoading,
            fileList = uiState.filteredFileList,
            isPlaying = playerState.isPlaying,
            currentlyPlayingUrl = playerState.currentlyPlayingUrl,
            onPlayPauseClick = { songUrl, fileName ->
                musicPlayerViewModel.handleAction(
                    MusicPlayerViewModel.PlayerAction.PlayPause(
                        songUrl,
                        fileName,
                        uiState.filteredFileList.indexOfFirst { it.url == songUrl }
                    ),
                    uiState.filteredFileList
                )
            },
            onLoadMoreFiles = {
                dynamicListViewModel.handleAction(DynamicListViewModel.ListAction.LoadMore)
            },
            onSearchTextChanged = { query ->
                dynamicListViewModel.handleAction(DynamicListViewModel.ListAction.Search(query))
            },
            listState = listState,
            onNavigateToUpload = onNavigateToUpload
        )
    }
}

