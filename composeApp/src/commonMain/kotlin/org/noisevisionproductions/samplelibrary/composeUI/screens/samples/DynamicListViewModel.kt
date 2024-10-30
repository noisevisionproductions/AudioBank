package org.noisevisionproductions.samplelibrary.composeUI.screens.samples

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
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata
import org.noisevisionproductions.samplelibrary.utils.decodeFileName

class DynamicListViewModel(
    private val firebaseStorageRepository: FirebaseStorageRepository,
) : ViewModel() {
    private var directoryPath: String by mutableStateOf("")

    data class UiState(
        val fileListWithMetadata: List<AudioMetadata> = emptyList(),
        val filteredFileList: List<AudioMetadata> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = false,
        val noMoreFilesToLoad: Boolean = false,
        val continuationToken: String? = null
    )

    sealed class ListAction {
        data class Search(val query: String) : ListAction()
        data object LoadMore : ListAction()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val cachedFiles = mutableMapOf<String, List<AudioMetadata>>()

    fun handleAction(action: ListAction) {
        when (action) {
            is ListAction.Search -> handleSearch(action.query)
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
            try {
                _uiState.update { it.copy(isLoading = true) }

                val (newFiles, newToken) = firebaseStorageRepository.listFilesWithMetadata(
                    directoryPath = directoryPath,
                    continuationToken = _uiState.value.continuationToken ?: ""
                )

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
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                UserErrorInfo(
                    message = "Błąd podczas ładowania dźwięków\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "LOADING_MORE_FILES_ERROR",
                    retryAction = { loadMoreFiles() }
                )
                println(e)
            }
        }
    }

    private fun handleSearch(query: String) {
        _uiState.update { currentState ->
            val trimmedQuery = query.trim()
            val filteredList = if (trimmedQuery.isNotEmpty()) {
                currentState.fileListWithMetadata.filter {
                    decodeFileName(it.fileName.trim()).contains(trimmedQuery, ignoreCase = true)
                }
            } else {
                currentState.fileListWithMetadata
            }
            currentState.copy(
                searchQuery = query,
                filteredFileList = filteredList
            )
        }
    }
}
