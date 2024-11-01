package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.noisevisionproductions.samplelibrary.composeUI.CreateErrorMessage
import org.noisevisionproductions.samplelibrary.composeUI.CustomColors
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.utils.TagRepository
import org.noisevisionproductions.samplelibrary.utils.UploadStatus
import org.noisevisionproductions.samplelibrary.utils.dataClasses.FileData
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker
import org.noisevisionproductions.samplelibrary.utils.metadata.MetadataEditor

@Composable
fun UploadNewSound(
    uploadSoundViewModel: UploadSoundViewModel,
    filePicker: FilePicker
) {
    val username by uploadSoundViewModel.username.collectAsState()
    val selectedFiles by uploadSoundViewModel.selectedFiles.collectAsState()
    val uploadProgress by uploadSoundViewModel.uploadProgress.collectAsState()
    val uploadStatuses by uploadSoundViewModel.uploadStatuses.collectAsState()
    val isPickerLoading = remember { mutableStateOf(false) }
    val selectedFileIndex = remember { mutableStateOf(0) }
    var showCompletionMessage by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFiles) {
        if (selectedFiles.isEmpty()) {
            selectedFileIndex.value = 0
        } else if (selectedFileIndex.value >= selectedFiles.size) {
            selectedFileIndex.value = selectedFiles.size - 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColorForNewContent)
            .padding(16.dp)
    ) {
        if (selectedFiles.isEmpty()) {
            // Centered message and file picker button when no files are selected
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dodaj pliki, aby rozpocząć (maksymalnie 5 plików)",
                    style = MaterialTheme.typography.body1,
                    textAlign = TextAlign.Center
                )

                ButtonForChoosingFile(
                    onClick = {
                        isPickerLoading.value = true
                        filePicker.pickFiles("audio/*", allowMultiple = true) { files ->
                            if (files.isNotEmpty()) {
                                uploadSoundViewModel.onFilesPicked(files)
                                isPickerLoading.value = false
                            }
                        }
                    },
                    isLoading = isPickerLoading.value
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Ta aplikacja automatycznie pobiera czas trwania dźwięku, " +
                            "ale może to zająć do 1 minuty, zanim będzie widoczny.",
                    style = MaterialTheme.typography.body1,
                    color = CustomColors.black60,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (selectedFiles.isNotEmpty() && selectedFileIndex.value < selectedFiles.size) {
                    FileEditorView(
                        uploadSoundViewModel = uploadSoundViewModel,
                        file = selectedFiles[selectedFileIndex.value],
                        index = selectedFileIndex.value,
                        username = username ?: "Nieznany użytkownik",
                        onFileNameChange = { idx, newName ->
                            uploadSoundViewModel.updateFileName(idx, newName)
                        },
                        onBpmChange = { newBpm ->
                            uploadSoundViewModel.updateFileBpm(selectedFileIndex.value, newBpm)
                        },
                        onToneChange = { newTone ->
                            uploadSoundViewModel.updateFileTone(selectedFileIndex.value, newTone)
                        },
                        onTagsChange = { newTags ->
                            uploadSoundViewModel.updateFileTags(selectedFileIndex.value, newTags)
                        },
                        onRemoveFile = {
                            uploadSoundViewModel.removeFile(selectedFileIndex.value)
                        }
                    )
                }

                // Navigation circles with upload progress/status below
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val dotsToDisplay = minOf(selectedFiles.size, 5)
                    (0 until dotsToDisplay).forEach { index ->
                        val isSelected = index == selectedFileIndex.value
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colors.primary else Color.Gray
                                )
                                .clickable(enabled = index < selectedFiles.size) {
                                    selectedFileIndex.value = index
                                }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedFiles.forEachIndexed { index, file ->
                        val fileName = "${username ?: "Nieznany użytkownik"} - ${file.name}"
                        val fileProgress = uploadProgress[fileName] ?: 0f
                        val fileStatus = uploadStatuses[fileName] ?: UploadStatus.IDLE

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File number indicator
                            Text(
                                text = "${index + 1}.",
                                style = MaterialTheme.typography.body2,
                                modifier = Modifier.width(24.dp)
                            )

                            // Progress bar with filename
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.caption,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                UploadProgressIndicator(
                                    progress = fileProgress,
                                    status = fileStatus,
                                )
                            }
                        }
                    }
                }
            }

            // Upload button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val isUploading = uploadStatuses.values.any { it == UploadStatus.IN_PROGRESS }

                ButtonForSoundUploading(
                    onClick = {
                        selectedFiles.forEachIndexed { index, file ->
                            uploadSoundViewModel.validateTags(index, file.tags)
                        }

                        if (uploadSoundViewModel.tagValidationErrors.value.isEmpty()) {
                            uploadSoundViewModel.uploadFiles()
                            showCompletionMessage = true
                        }
                    },
                    enabled = !isUploading
                )
            }
        }

        // Completion message overlay
        if (showCompletionMessage) {
            val allUploadsComplete = uploadStatuses.values.all {
                it == UploadStatus.SUCCESS || it == UploadStatus.ERROR
            }
            val allUploadsSuccessful = uploadStatuses.values.all { it == UploadStatus.SUCCESS }

            if (allUploadsComplete) {
                LaunchedEffect(Unit) {
                    delay(2000)
                    showCompletionMessage = false
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (allUploadsSuccessful)
                            "Wszystkie pliki zostały pomyślnie przesłane!"
                        else "Niektórych plików nie udało się przesłać. Spróbuj ponownie.",
                        color = colors.backgroundWhiteColor,
                        style = MaterialTheme.typography.h6,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UploadProgressIndicator(
    progress: Float,
    status: UploadStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.backgroundGrayColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        when (status) {
                            UploadStatus.IN_PROGRESS -> MaterialTheme.colors.primary
                            UploadStatus.SUCCESS -> Color(0xFF4CAF50)
                            UploadStatus.ERROR -> Color(0xFFE57373)
                            UploadStatus.IDLE -> Color.LightGray
                        }
                    )
            )

        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(16.dp)
        ) {
            when (status) {
                UploadStatus.IN_PROGRESS -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.body2
                    )
                }

                UploadStatus.SUCCESS -> {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Sukces",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Zakończono",
                        style = MaterialTheme.typography.caption,
                        color = Color(0xFF4CAF50)
                    )
                }

                UploadStatus.ERROR -> {
                    Text(
                        text = "Błąd",
                        style = MaterialTheme.typography.caption,
                        color = Color(0xFFE57373)
                    )
                }

                UploadStatus.IDLE -> {
                    Text(
                        text = "Gotowy",
                        style = MaterialTheme.typography.caption,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun FileEditorView(
    file: FileData,
    index: Int,
    username: String,
    onFileNameChange: (Int, String) -> Unit,
    onBpmChange: (String) -> Unit,
    onToneChange: (String) -> Unit,
    onTagsChange: (List<String>) -> Unit,
    onRemoveFile: () -> Unit,
    uploadSoundViewModel: UploadSoundViewModel
) {
    val tagValidationErrors by uploadSoundViewModel.tagValidationErrors.collectAsState()
    val showTagError = tagValidationErrors.contains(index)

    MetadataEditor(
        fileName = "$username - ${file.name}",
        onFileNameChange = { newName -> onFileNameChange(index, newName) },
        bpm = file.bpmValue ?: "",
        onBpmChange = onBpmChange,
        tone = file.toneValue ?: "",
        onToneChange = onToneChange,
        tags = file.tags,
        onTagsChange = onTagsChange,
        showTagError = showTagError,
        uploadSoundViewModel = uploadSoundViewModel,
        fileId = index
    )

    // Additional UI elements like the "Remove File" button
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onRemoveFile) {
            Text(
                text = "Usuń plik",
                color = colors.primaryBackgroundColor,
                style = MaterialTheme.typography.button,
                fontSize = 19.sp
            )
        }
    }

    Divider(modifier = Modifier.padding(vertical = 8.dp))
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = selectedOption.ifEmpty { "Wybierz $label\n(opcjonalnie)" },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        if (expanded) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    ) {
                        Text(text = option)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagsMultiSelectMenu(
    allTags: List<String>,
    selectedTags: List<String>,
    onTagSelected: (String) -> Unit,
    onTagDeselected: (String) -> Unit,
    showError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = if (selectedTags.isEmpty()) "Wybierz tagi" else selectedTags.joinToString(", "),
            onValueChange = {},
            readOnly = true,
            label = { Text("Tagi") },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable { expanded = true },
            isError = showError
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                DropdownMenuItem(onClick = {
                    if (isSelected) {
                        onTagDeselected(tag)
                    } else {
                        onTagSelected(tag)
                    }
                }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = tag)
                    }
                }
            }
        }
    }
    if (showError) {
        CreateErrorMessage("Wybierz przynajmniej jeden tag")
    }
}

@Composable
fun TagsSelectorForFile(
    selectedTags: List<String>,
    onTagsSelected: (List<String>) -> Unit,
    uploadSoundViewModel: UploadSoundViewModel,
    fileId: Int,
    showError: Boolean
) {
    val allTags = remember {
        TagRepository.getTagsFromJsonFile()
    }
    var currentSelectedTags by remember(fileId) { mutableStateOf(selectedTags) }

    val onTagSelected: (String) -> Unit = { tag ->
        currentSelectedTags = currentSelectedTags + tag
        onTagsSelected(currentSelectedTags)
        uploadSoundViewModel.validateTags(fileId, currentSelectedTags)
    }

    val onTagDeselected: (String) -> Unit = { tag ->
        currentSelectedTags = currentSelectedTags - tag
        onTagsSelected(currentSelectedTags)
        uploadSoundViewModel.validateTags(fileId, currentSelectedTags)
    }

    TagsMultiSelectMenu(
        allTags = allTags,
        selectedTags = currentSelectedTags,
        onTagSelected = onTagSelected,
        onTagDeselected = onTagDeselected,
        showError = showError
    )
}

@Composable
fun ButtonForChoosingFile(
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colors.primaryBackgroundColor
        ),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .padding(bottom = 10.dp)

            .height(50.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = colors.backgroundWhiteColor
            )
        } else {
            Text(
                "Wybierz pliki",
                style = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    color = colors.backgroundWhiteColor
                )
            )
        }
    }
}

@Composable
fun ButtonForSoundUploading(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = Modifier
            .size(100.dp, 50.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colors.primaryBackgroundColor
        ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Text(
            "Wyślij",
            style = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                color = colors.backgroundWhiteColor
            )
        )
    }
}