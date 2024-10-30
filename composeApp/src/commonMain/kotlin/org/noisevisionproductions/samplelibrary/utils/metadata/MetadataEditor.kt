package org.noisevisionproductions.samplelibrary.utils.metadata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.noisevisionproductions.samplelibrary.composeUI.CustomOutlinedTextField
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.DropdownSelector
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.TagsSelectorForFile
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel

@Composable
fun MetadataEditor(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    bpm: String,
    onBpmChange: (String) -> Unit,
    tone: String,
    onToneChange: (String) -> Unit,
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    showTagError: Boolean,
    uploadSoundViewModel: UploadSoundViewModel,
    fileId: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        CustomOutlinedTextField(
            value = fileName,
            onValueChange = onFileNameChange,
            label = "Edytuj nazwę pliku",
            imeAction = ImeAction.Done
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                DropdownSelector(
                    label = "BPM",
                    options = MetadataLists.bpmOptions,
                    selectedOption = bpm,
                    onOptionSelected = onBpmChange
                )
            }

            Box(
                modifier = Modifier.weight(1f)
            ) {
                DropdownSelector(
                    label = "Ton",
                    options = MetadataLists.toneOptions,
                    selectedOption = tone,
                    onOptionSelected = onToneChange
                )
            }

        }

        TagsSelectorForFile(
            selectedTags = tags,
            onTagsSelected = onTagsChange,
            fileId = fileId,
            showError = showTagError,
            uploadSoundViewModel = uploadSoundViewModel
        )
    }
}