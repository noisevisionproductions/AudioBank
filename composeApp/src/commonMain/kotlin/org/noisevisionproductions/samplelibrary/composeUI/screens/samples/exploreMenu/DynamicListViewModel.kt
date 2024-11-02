package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import org.noisevisionproductions.samplelibrary.database.LikeRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.decodeFileName
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

class DynamicListViewModel(
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository,
    private val sharedSoundEventsManager: SharedSoundEventsManager
) : ViewModel() {
    private var directoryPath: String by mutableStateOf("")

    data class UiState(
        val fileListWithMetadata: List<AudioMetadata> = emptyList(),
        val filteredFileList: List<AudioMetadata> = emptyList(),
        val searchQuery: String = "",
        val selectedTags: Set<String> = emptySet(),
        val sortOption: SortOption = SortOption.NEWEST,
        val isLoading: Boolean = false,
        val noMoreFilesToLoad: Boolean = false,
        val continuationToken: String? = null,
        val likedSounds: Set<String> = emptySet()
    )

    enum class SortOption {
        NEWEST,
        OLDEST
    }

    sealed class ListAction {
        data class Search(val query: String) : ListAction()
        data class SetSortOption(val option: SortOption) : ListAction()
        data object ClearTags : ListAction()
        data object LoadMore : ListAction()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val cachedFiles = mutableMapOf<String, List<AudioMetadata>>()
    private var cachedLikedSounds: Set<String> = emptySet()

    init {
        loadLikedSounds()
        observeSoundEvents()
    }

    private fun observeSoundEvents() {
        viewModelScope.launch {
            sharedSoundEventsManager.soundEvents.collect { event ->
                when (event) {
                    is SoundEvent.SoundLiked -> {
                        updateSoundLikeStatus(event.soundId, true)
                    }

                    is SoundEvent.SoundUnliked -> {
                        updateSoundLikeStatus(event.soundId, false)
                    }

                    is SoundEvent.SoundDeleted -> {
                        removeDeletedSound(event.soundId)
                    }

                    is SoundEvent.SoundMetadataUpdated -> {
                        updateSoundInList(event.soundId, event.updates)
                    }

                    is SoundEvent.SoundUploaded -> {
                        addNewSoundToList(event.sound)
                    }
                }
            }
        }
    }

    private fun updateSoundLikeStatus(soundId: String, isLiked: Boolean) {
        _uiState.update { state ->
            val updatedList = state.fileListWithMetadata.map { metadata ->
                if (metadata.id == soundId) metadata.copy(isLiked = isLiked)
                else metadata
            }
            val newLikedSounds = if (isLiked) {
                state.likedSounds + soundId
            } else {
                state.likedSounds - soundId
            }
            state.copy(
                fileListWithMetadata = updatedList,
                likedSounds = newLikedSounds
            )
        }
    }

    private fun removeDeletedSound(soundId: String) {
        _uiState.update { state ->
            val updatedFileList = state.fileListWithMetadata.filter { it.id != soundId }
            val updatedFilteredList = state.filteredFileList.filter { it.id != soundId }
            state.copy(
                fileListWithMetadata = updatedFileList,
                filteredFileList = updatedFilteredList
            )
        }

        cachedFiles.forEach { (key, fileList) ->
            cachedFiles[key] = fileList.filter { it.id != soundId }
        }
    }

    private fun updateSoundInList(soundId: String, updates: Map<String, Any>) {
        _uiState.update { state ->
            val updatedList = state.fileListWithMetadata.map { metadata ->
                if (metadata.id == soundId) {
                    metadata.copy(
                        fileName = updates["fileName"] as? String ?: metadata.fileName,
                        duration = updates["duration"] as? String ?: metadata.duration,
                        url = updates["url"] as? String ?: metadata.url,
                        timestamp = updates["timestamp"] as? String ?: metadata.timestamp,
                        bpm = updates["bpm"] as? String ?: metadata.bpm,
                        tone = updates["tone"] as? String ?: metadata.tone,
                        tags = (updates["tags"] as? List<*>)?.mapNotNull { it as? String }
                            ?: metadata.tags,
                        isLiked = updates["isLiked"] as? Boolean ?: metadata.isLiked
                    )
                } else metadata
            }

            val updatedFilteredList = applyFiltersAndSort(
                updatedList,
                state.searchQuery,
                state.selectedTags,
                state.sortOption
            )
            state.copy(
                fileListWithMetadata = updatedList,
                filteredFileList = updatedFilteredList
            )
        }
    }

    private fun addNewSoundToList(sound: AudioMetadata) {
        _uiState.update { state ->
            val updatedList = listOf(sound) + state.fileListWithMetadata
            val updatedFilteredList = applyFiltersAndSort(
                updatedList,
                state.searchQuery,
                state.selectedTags,
                state.sortOption
            )
            state.copy(
                fileListWithMetadata = updatedList,
                filteredFileList = updatedFilteredList
            )
        }
    }

    private fun applyFiltersAndSort(
        list: List<AudioMetadata>,
        query: String,
        tags: Set<String>,
        sortOption: SortOption
    ): List<AudioMetadata> {
        var result = list

        if (query.isNotEmpty()) {
            result = result.filter {
                it.fileName.contains(query, ignoreCase = true) ||
                        it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }

        if (tags.isNotEmpty()) {
            result = result.filter { metadata ->
                tags.all { tag -> metadata.tags.contains(tag) }
            }
        }

        result = when (sortOption) {
            SortOption.NEWEST -> result.sortedByDescending { it.timestamp }
            SortOption.OLDEST -> result.sortedBy { it.timestamp }
        }

        return result
    }

    fun handleAction(action: ListAction) {
        when (action) {
            is ListAction.Search -> handleSearch(action.query)
            is ListAction.SetSortOption -> handleSortOption(action.option)
            is ListAction.ClearTags -> clearTags()
            is ListAction.LoadMore -> loadMoreFiles()
        }
    }

    fun updateDirectoryPath(newDirectoryPath: String) {
        if (directoryPath != newDirectoryPath) {
            directoryPath = newDirectoryPath
            _uiState.update {
                it.copy(
                    fileListWithMetadata = emptyList(),
                    filteredFileList = emptyList(),
                    continuationToken = null,
                    noMoreFilesToLoad = false
                )
            }
            loadMoreFiles()
        }
    }

    private fun loadMoreFiles() {
        if (cachedFiles.containsKey(directoryPath)) {
            _uiState.update {
                it.copy(
                    fileListWithMetadata = cachedFiles[directoryPath] ?: emptyList(),
                )
            }
            return
        }
        if (_uiState.value.noMoreFilesToLoad || _uiState.value.isLoading) {
            println("Skipping load - noMoreFiles: ${_uiState.value.noMoreFilesToLoad}, isLoading: ${_uiState.value.isLoading}")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = firebaseStorageRepository.listFilesWithMetadata(
                directoryPath = directoryPath,
                continuationToken = _uiState.value.continuationToken ?: ""
            )

            result.onSuccess { (newFiles, newToken) ->
                // Update favorite status and filter the files as needed
                updateFileList(newFiles)

                _uiState.update { currentState ->
                    val updatedFileList = (currentState.fileListWithMetadata + newFiles)
                        .distinctBy { decodeFileName(it.fileName.trim()) }

                    val updatedFilteredList = if (currentState.searchQuery.isEmpty()) {
                        updatedFileList
                    } else {
                        updatedFileList.filter {
                            decodeFileName(it.fileName.trim())
                                .contains(currentState.searchQuery, ignoreCase = true)
                        }
                    }

                    currentState.copy(
                        fileListWithMetadata = updatedFileList,
                        filteredFileList = updatedFilteredList,
                        continuationToken = newToken,
                        noMoreFilesToLoad = newFiles.isEmpty() || newToken == null,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                UserErrorInfo(
                    message = "Error loading sounds\n${error.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "LOADING_MORE_FILES_ERROR",
                    retryAction = { loadMoreFiles() }
                )
                println(error)
            }
        }
    }

    private fun handleSortOption(option: SortOption) {
        _uiState.update { currentState ->
            currentState.copy(sortOption = option).also {
                applyFilters(it)
            }
        }
    }

    private fun handleSearch(query: String) {
        _uiState.update { currentState ->
            currentState.copy(searchQuery = query).also {
                applyFilters(it)
            }
        }
    }

    fun toggleTag(tag: String) {
        _uiState.update { currentState ->
            val updatedTags = currentState.selectedTags.toMutableSet()
            if (updatedTags.contains(tag)) {
                updatedTags.remove(tag)
            } else {
                updatedTags.add(tag)
            }
            currentState.copy(selectedTags = updatedTags).also {
                applyFilters(it)
            }
        }
    }

    fun clearTags() {
        _uiState.update { currentState ->
            currentState.copy(selectedTags = emptySet()).also {
                applyFilters(it)
            }
        }
    }

    private fun applyFilters(state: UiState) {
        var filteredList = state.fileListWithMetadata.filter { metadata ->
            val matchesSearch = if (state.searchQuery.isNotEmpty()) {
                decodeFileName(metadata.fileName.trim()).contains(
                    state.searchQuery,
                    ignoreCase = true
                )
            } else true

            val matchesTags = if (state.selectedTags.isNotEmpty()) {
                state.selectedTags.all { tag -> metadata.tags.contains(tag) }
            } else true

            matchesSearch && matchesTags
        }

        filteredList = when (state.sortOption) {
            SortOption.NEWEST -> filteredList.sortedByDescending { it.timestamp }
            SortOption.OLDEST -> filteredList.sortedBy { it.timestamp }
        }

        _uiState.update { it.copy(filteredFileList = filteredList) }
    }

    private fun loadLikedSounds() {
        viewModelScope.launch {
            userRepository.getLikedSounds().fold(
                onSuccess = { likedSounds ->
                    val likedSoundsSet = likedSounds.toSet()
                    cachedLikedSounds = likedSoundsSet
                    _uiState.update { it.copy(likedSounds = likedSoundsSet) }
                    if (_uiState.value.fileListWithMetadata.isNotEmpty()) {
                        updateFilesLikeStatus(likedSoundsSet)
                    }
                },
                onFailure = { e ->
                    UserErrorInfo(
                        message = "Błąd podczas ładowania polubień dźwięków - ${e.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "LOAD_LIKED_SOUNDS_ERROR",
                        retryAction = { loadLikedSounds() }
                    )
                    println("Error loading liked sounds: ${e.message}")
                }
            )
        }
    }

    private fun updateFilesLikeStatus(likedSounds: Set<String>) {
        val updatedList = _uiState.value.fileListWithMetadata.map { metadata ->
            metadata.copy(isLiked = metadata.id in likedSounds)
        }
        _uiState.update { it.copy(fileListWithMetadata = updatedList) }
    }

    fun toggleSoundLike(soundId: String) {
        viewModelScope.launch {
            val result = likeRepository.toggleSoundLike(soundId)

            result.fold(
                onSuccess = {
                    val isCurrentlyLiked = soundId in _uiState.value.likedSounds
                    val event = if (isCurrentlyLiked) {
                        SoundEvent.SoundUnliked(soundId)
                    } else {
                        SoundEvent.SoundLiked(soundId)
                    }
                    sharedSoundEventsManager.emitEvent(event)

                    // Update local state
                    val currentLikedSounds = _uiState.value.likedSounds
                    val newLikedSounds = if (isCurrentlyLiked) {
                        currentLikedSounds - soundId
                    } else {
                        currentLikedSounds + soundId
                    }

                    cachedLikedSounds = newLikedSounds
                    _uiState.update { it.copy(likedSounds = newLikedSounds) }
                    updateFilesLikeStatus(newLikedSounds)
                },
                onFailure = { exception ->
                    UserErrorInfo(
                        message = "Błąd podczas zarządzaniem polubienia - ${exception.message}",
                        actionType = UserErrorAction.OK,
                        errorId = "TOGGLE_SOUND_LIKE_ERROR",
                    )
                    println(exception)
                }
            )
        }
    }

    private fun updateFileList(newFiles: List<AudioMetadata>) {
        viewModelScope.launch {
            val updatedFiles = newFiles.map { metadata ->
                metadata.copy(isLiked = metadata.id in cachedLikedSounds)
            }
            _uiState.update {
                it.copy(
                    fileListWithMetadata = updatedFiles,
                    likedSounds = cachedLikedSounds
                )
            }
        }
    }
}
