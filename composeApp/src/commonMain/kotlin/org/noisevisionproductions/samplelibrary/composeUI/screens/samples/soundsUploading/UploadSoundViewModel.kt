package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement.SharedSoundEventsManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement.SoundEvent
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.UploadStatus
import org.noisevisionproductions.samplelibrary.utils.dataClasses.FileData
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

class UploadSoundViewModel(
    private val storageService: FirebaseStorageRepository,
    private val userRepository: UserRepository,
    private val sharedSoundViewModel: SharedSoundEventsManager
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
        userRepository.getCurrentUserId().fold(
            onSuccess = { userId ->
                userRepository.getUsernameById(userId).fold(
                    onSuccess = { username ->
                        _username.value = username
                    },
                    onFailure = { error ->
                        println("Error fetching username for user ID $userId: ${error.message}")
                        _username.value = null
                    }
                )
            },
            onFailure = { error ->
                println("Error fetching current user ID: ${error.message}")
                _username.value = null
            }
        )
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
                    tags = file.tags,
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
                    val savedMetadata = result.getOrNull()
                    if (savedMetadata != null) {
                        _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                            it[modifiedFileName] = UploadStatus.SUCCESS
                        }

                        sharedSoundViewModel.emitEvent(SoundEvent.SoundUploaded(savedMetadata))
                    } else {
                        throw Exception("Failed to get saved metadata")
                    }
                } else {
                    _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                        it[modifiedFileName] = UploadStatus.ERROR
                    }
                    throw Exception(result.exceptionOrNull()?.message ?: "Unknown error")
                }

            } catch (e: Exception) {
                _uploadStatuses.value = _uploadStatuses.value.toMutableMap().also {
                    it[modifiedFileName] = UploadStatus.ERROR
                }
                UserErrorInfo(
                    message = "Nie udało się zapisać dźwięków\n${e.message}",
                    actionType = UserErrorAction.OK,
                    errorId = "UPLOAD_SOUND_ERROR"
                )
                println(e)
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

    fun onFilesPicked(files: List<FileData>) {
        _selectedFiles.value = files.take(5)
    }

    fun removeFile(index: Int) {
        _selectedFiles.value = _selectedFiles.value.toMutableList().also {
            it.removeAt(index)
        }
    }

    fun clearSelectedFiles() {
        _selectedFiles.value = emptyList()
    }
}
