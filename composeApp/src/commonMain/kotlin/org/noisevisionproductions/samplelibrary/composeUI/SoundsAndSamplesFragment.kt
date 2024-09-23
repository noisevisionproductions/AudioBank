package org.noisevisionproductions.samplelibrary.composeUI


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.database.getCloudFirestore
import org.noisevisionproductions.samplelibrary.interfaces.getBpmFromAudioFile
import org.noisevisionproductions.samplelibrary.interfaces.getPitchFromAudioFile
import org.noisevisionproductions.samplelibrary.interfaces.hzToMusicalNoteFrequency
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_heart
import samplelibrary.composeapp.generated.resources.icon_play
import samplelibrary.composeapp.generated.resources.icon_properties_menu

@Composable
fun FiltersWindow(isExpanded: Boolean, expandedHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(expandedHeight)
            .background(colors.backgroundGrayColor)
            .pointerInput(isExpanded) {
                if (!isExpanded) {
                    detectTapGestures {}
                }
            }
    ) {
        if (isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DropDownMenuWithItems("Instrumenty")
                DropDownMenuWithItems("Rodzaje")
                DropDownMenuWithItems("Ton")
                DropDownMenuWithItems("BPM")
            }
        }
    }
}

@Composable
fun DropDownMenuWithItems(label: String) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf(label) }

    Column {
        Button(onClick = { expanded = !expanded }) {
            Text(selectedItem)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 1"
                expanded = false
            }) {
                Text("Opcja 1")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 2"
                expanded = false
            }) {
                Text("Opcja 2")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 3"
                expanded = false
            }) {
                Text("Opcja 3")
            }
            DropdownMenuItem(onClick = {
                selectedItem = "Opcja 4"
                expanded = false
            }) {
                Text("Opcja 4")
            }
        }
    }
}

@Composable
fun DynamicListWithSamples() {
    val awsS3Config = getCloudFirestore()
    var fileList by remember { mutableStateOf<List<String>>(emptyList()) }
    var songsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var durations by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var pitches by remember { mutableStateOf<Map<String, Float?>>(emptyMap()) }
    var bpmList by remember { mutableStateOf<Map<String, Float?>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val namesOfSamples = awsS3Config.listFilesInBucket("samples/rimshots")
                fileList = namesOfSamples.map { it.substringAfterLast("/") }

                songsList = namesOfSamples.map {
                    awsS3Config.getSampleFile(it)
                }

                songsList.forEach { songUrl ->
                    val duration = awsS3Config.getAudioDuration(songUrl)
                    durations = durations + (songUrl to duration)
                }

                songsList.forEach { songUrl ->
                    val pitch = getPitchFromAudioFile(songUrl)
                    pitches = pitches + (songUrl to pitch)
                }

                songsList.forEach { songUrl ->
                    val bpm = getBpmFromAudioFile(songUrl)
                    bpmList = bpmList + (songUrl to bpm)
                }
            } catch (e: Exception) {
                println("Error fetching files: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colors.backgroundGrayColor)
    ) {
        RowWithSearchBar(labelText = "Dźwięki",
            placeholderText = "Wyszukaj dźwięki",
            onSearchTextChanged = { searchQuery ->
                // TODO obslużyć wyszukiwarkę
            })

        // fragment z kontentem dzwieków
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp))
                .background(colors.backgroundWhiteColor)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nazwa",
                        fontSize = 15.sp,
                        color = colors.hintTextColorLight
                    )
                    Text(
                        text = "Czas",
                        fontSize = 15.sp,
                        color = colors.hintTextColorLight
                    )
                    Text(
                        text = "Ton",
                        fontSize = 15.sp,
                        color = colors.hintTextColorLight
                    )
                    Text(
                        text = "BPM",
                        fontSize = 15.sp,
                        color = colors.hintTextColorLight
                    )
                }

                Divider(
                    color = colors.textColorMain,
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(fileList.size) { index ->
                        val fileUrl = fileList[index]
                        val decodedUrl = fileUrl
                            .replace("%20", " ")
                            .replace("%2F", "/")

                        val fileName = decodedUrl.substringAfterLast("/").substringBefore("?")
                        val songUrl = songsList.getOrNull(index) ?: ""

                        val durationMs = durations[songUrl] ?: 0
                        val durationSeconds = durationMs / 1000

                        val pitch = pitches[songUrl] ?: 0F

                        val bpm = bpmList[songUrl] ?: 0F

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Image(
                                painterResource(Res.drawable.icon_play),
                                contentDescription = "Play",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        coroutineScope.launch {
                                            awsS3Config.playAudioFromUrl(songUrl)
                                        }
                                    }
                            )

                            Text(
                                text = fileName,
                                modifier = Modifier
                                    .weight(1f)
                            )
                            Text(
                                text = "${durationSeconds}s",
                                modifier = Modifier
                                    .weight(1f)
                            )
                            Text(
                                text = hzToMusicalNoteFrequency(pitch),
                                modifier = Modifier
                                    .weight(1f)
                            )
                            Text(
                                text = "$bpm",
                                modifier = Modifier
                                    .weight(1f)
                            )

                            Image(
                                painterResource(Res.drawable.icon_heart), null,
                                modifier = Modifier
                                    .weight(1f)
                            )
                            Image(
                                painterResource(Res.drawable.icon_properties_menu), null,
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }

                        Divider(
                            color = colors.dividerLightGray,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetConfiguration() {
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Expanded)
    )
    val coroutineScope = rememberCoroutineScope()

    UniversalBottomSheet(
        scaffoldState = scaffoldState,
        title = "Fragment one",
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryBackgroundColor)
                    .padding(16.dp)
            ) {
                Text(text = "text test test")
            }
        },
        onSliderChange = { progress ->

        }
    )
}