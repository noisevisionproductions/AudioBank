package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.utils.UploadStatus
import org.noisevisionproductions.samplelibrary.utils.dataClasses.AudioMetadata
import org.noisevisionproductions.samplelibrary.utils.dataClasses.FileData

class UploadSoundViewModel(
    private val storageService: FirebaseStorageRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> = _username.asStateFlow()

    private val _tagValidationErrors = MutableStateFlow<Set<Int>>(emptySet())
    val tagValidationErrors: StateFlow<Set<Int>> = _tagValidationErrors.asStateFlow()

    private val _selectedFiles = MutableStateFlow<List<FileData>>(emptyList())
    val selectedFiles = _selectedFiles.asStateFlow()

    private val _uploadProgress =
        MutableStateFlow<Map<String, Float>>(emptyMap())
    val uploadProgress = _uploadProgress.asStateFlow()

    private val _uploadStatuses =
        MutableStateFlow<Map<String, UploadStatus>>(emptyMap())
    val uploadStatuses = _uploadStatuses.asStateFlow()

    init {
        fetchUsername()
    }

    private fun fetchUsername() = viewModelScope.launch {
        val userId = userRepository.getCurrentUserId()
        _username.value = userId?.let { userRepository.getUsernameById(it) }
    }

    fun uploadFiles() = viewModelScope.launch {
        selectedFiles.value.forEachIndexed { index, _ ->
            validateTags(index, getTagsForFile(index))
        }

        if (_tagValidationErrors.value.isNotEmpty()) {
            return@launch
        }

        val usernamePrefix = _username.value ?: "Nieznany użytkownik"

        selectedFiles.value.forEach { file ->
            val modifiedFileName = "$usernamePrefix - ${file.name}"

            try {
                _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                    it[modifiedFileName] = UploadStatus.IN_PROGRESS
                }

                val metadata = AudioMetadata(
                    fileName = modifiedFileName,
                    bpm = file.bpmValue,
                    tone = file.toneValue,
                    tags = file.tags
                )

                val result = storageService.uploadSoundsToStorage(
                    username = usernamePrefix,
                    fileName = modifiedFileName,
                    fileData = file.fileData,
                    metadata = metadata,
                    onProgress = { progress ->
                        _uploadProgress.value = _uploadProgress.value.toMutableMap().also {
                            it[modifiedFileName] = progress
                        }
                    }
                )

                if (result.isSuccess) {
                    _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                        it[modifiedFileName] = UploadStatus.SUCCESS
                    }
                } else {
                    _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                        it[modifiedFileName] = UploadStatus.ERROR
                    }
                }

            } catch (e: Exception) {
                _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                    it[modifiedFileName] = UploadStatus.ERROR
                }
            }

        }
    }

    fun updateFileName(index: Int, newName: String) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().apply {
            getOrNull(index)?.let { file ->
                set(index, file.copy(name = newName))
            }
        }
    }

    fun updateFileBpm(index: Int, newBpm: String) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().apply {
            getOrNull(index)?.let { file ->
                set(index, file.copy(bpmValue = newBpm))
            }
        }
    }

    fun updateFileTone(index: Int, newTone: String) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().apply {
            getOrNull(index)?.let { file ->
                set(index, file.copy(toneValue = newTone))
            }
        }
    }

    fun updateFileTags(index: Int, newTags: List<String>) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().apply {
            getOrNull(index)?.let { file ->
                set(index, file.copy(tags = newTags))
            }
        }
    }

    fun validateTags(fileIndex: Int, tags: List<String>) {
        if (tags.isEmpty()) {
            _tagValidationErrors.update { it + fileIndex }
        } else {
            _tagValidationErrors.update { it - fileIndex }
        }
    }

    private fun getTagsForFile(index: Int): List<String> {
        return _selectedFiles.value.getOrNull(index)?.tags ?: emptyList()
    }

    // Add new files (called after picking files)
    fun onFilesPicked(files: List<FileData>) {
        _selectedFiles.value = files.take(5)
    }

    // Remove file at specific index
    fun removeFile(index: Int) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().also {
            it.removeAt(index)
        }
    }
}
