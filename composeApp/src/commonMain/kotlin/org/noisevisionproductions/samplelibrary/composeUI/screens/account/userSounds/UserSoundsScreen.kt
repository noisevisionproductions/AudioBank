package org.noisevisionproductions.samplelibrary.composeUI.screens.account.userSounds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.CustomAlertDialog
import org.noisevisionproductions.samplelibrary.composeUI.CustomTopAppBar
import org.noisevisionproductions.samplelibrary.composeUI.ScrollingText
import org.noisevisionproductions.samplelibrary.composeUI.screens.colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata
import org.noisevisionproductions.samplelibrary.utils.metadata.MetadataEditor

@Composable
fun UserSoundsScreen(
    userSoundsViewModel: UserSoundsViewModel,
    onNavigateBack: () -> Unit,
    uploadSoundViewModel: UploadSoundViewModel
) {
    val userSounds by userSoundsViewModel.userSounds.collectAsState()
    val favoriteSounds by userSoundsViewModel.favoriteSounds.collectAsState()
    val isLoading by userSoundsViewModel.isLoading.collectAsState()

    var selectedSound by remember { mutableStateOf<AudioMetadata?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundColorForNewContent)
    ) {
        CustomTopAppBar(
            title = "Zarządzanie dźwiękami",
            onNavigateBack = onNavigateBack,
            onBackPressed = {
                onNavigateBack()
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "Twoje dźwięki",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (userSounds.isEmpty()) {
                    item {
                        Text(
                            text = "Nie dodałeś jeszcze dźwięków",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(userSounds) { sound ->
                        SoundCard(
                            sound = sound,
                            onEdit = {
                                selectedSound = sound
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedSound = sound
                                showDeleteDialog = true
                            }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Ulubione",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (favoriteSounds.isEmpty()) {
                    item {
                        Text(
                            text = "Nie polubiłeś żadnych dźwięków",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(favoriteSounds) { sound ->
                        SoundCard(
                            sound = sound,
                            showActions = false
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog && selectedSound != null) {
        EditSoundDialog(
            sound = selectedSound!!,
            onDismiss = { showEditDialog = false },
            onSave = { updates ->
                userSoundsViewModel.updateSoundMetadata(selectedSound!!.id!!, updates)
                showEditDialog = false
            },
            uploadSoundViewModel = uploadSoundViewModel
        )
    }

    if (showDeleteDialog && selectedSound != null) {
        CustomAlertDialog(
            onConfirm = {
                userSoundsViewModel.deleteSound(selectedSound!!.id!!, selectedSound!!.fileName)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false },
            title = "Usuń dźwięk",
            contentQuestion = "Czy na pewno chcesz usunąć ten dźwięk?"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundCard(
    sound: AudioMetadata,
    showActions: Boolean = true,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrollingText(
                    fileName = sound.fileName,
                    modifier = Modifier.weight(1f),
                )

                if (showActions) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, "Edytuj")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Usuń")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                sound.bpm?.let {
                    Chip(text = "$it BPM")
                    Spacer(modifier = Modifier.width(8.dp))
                }
                sound.tone?.let {
                    Chip(text = it)
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            if (sound.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sound.tags.forEach { tag ->
                        Chip(text = tag)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditSoundDialog(
    sound: AudioMetadata,
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit,
    uploadSoundViewModel: UploadSoundViewModel
) {
    var fileName by remember { mutableStateOf(sound.fileName) }
    var bpm by remember { mutableStateOf(sound.bpm ?: "") }
    var tone by remember { mutableStateOf(sound.tone ?: "") }
    var tags by remember { mutableStateOf(sound.tags) }

    val tagValidationErrors by uploadSoundViewModel.tagValidationErrors.collectAsState()
    val showTagError = tagValidationErrors.contains(0)

    AlertDialog(
        onDismissRequest = {},
        title = { Text("Edytuj dźwięk") },
        text = {
            MetadataEditor(
                fileName = fileName,
                onFileNameChange = { fileName = it },
                bpm = bpm,
                onBpmChange = { bpm = it },
                tone = tone,
                onToneChange = { tone = it },
                tags = tags,
                onTagsChange = { tags = it },
                showTagError = showTagError,
                uploadSoundViewModel = uploadSoundViewModel,
                fileId = 0,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updates = mutableMapOf(
                        "file_name" to fileName,
                        "bpm" to bpm,
                        "tone" to tone,
                        "tags" to tags
                    )
                    onSave(updates)
                }
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
private fun Chip(text: String) {
    Surface(
        modifier = Modifier.height(24.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.primaryBackgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.subtitle1,
            color = colors.backgroundWhiteColor
        )
    }
}