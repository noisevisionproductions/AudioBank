package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import org.noisevisionproductions.samplelibrary.utils.dataClasses.AudioMetadata
import org.noisevisionproductions.samplelibrary.utils.decodeFileName

class MusicPlayerViewModel(
    private val musicPlayerService: MusicPlayerService
) : ViewModel() {
    data class PlayerState(
        val isPlaying: Boolean = false,
        val currentlyPlayingUrl: String? = null,
        val selectedFileName: String? = null,
        val progress: Float = 0f,
        val currentSongIndex: Int = 0
    )

    sealed class PlayerAction {
        data class PlayPause(val songUrl: String, val fileName: String, val index: Int) :
            PlayerAction()

        data object Next : PlayerAction()
        data object Previous : PlayerAction()
        data class Seek(val position: Float) : PlayerAction()
    }

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun handleAction(action: PlayerAction, currentPlaylist: List<AudioMetadata>) {
        when (action) {
            is PlayerAction.PlayPause -> handlePlayPause(
                action.songUrl,
                action.fileName,
                action.index
            )

            is PlayerAction.Next -> playNextSong(currentPlaylist)
            is PlayerAction.Previous -> playPreviousSong(currentPlaylist)
            is PlayerAction.Seek -> handleSeek(action.position)
        }
    }

    private fun handlePlayPause(songUrl: String, fileName: String, newIndex: Int) {
        viewModelScope.launch {
            val currentState = _playerState.value

            when {
                currentState.isPlaying && currentState.currentlyPlayingUrl == songUrl -> {
                    musicPlayerService.pauseAudio()
                    updatePlayingState(false)
                }

                currentState.currentlyPlayingUrl != null && currentState.currentlyPlayingUrl != songUrl -> {
                    musicPlayerService.stopAudio()
                    playNewSong(songUrl, fileName, newIndex)
                }

                currentState.currentlyPlayingUrl == songUrl && !currentState.isPlaying -> {
                    resumeCurrentSong()
                }

                else -> playNewSong(songUrl, fileName, newIndex)
            }
        }
    }

    private suspend fun playNewSong(songUrl: String, fileName: String, newIndex: Int) {
        musicPlayerService.playAudioFromUrl(
            audioUrl = songUrl,
            currentlyPlayingUrl = _playerState.value.currentlyPlayingUrl,
            onCompletion = {
                updatePlayingState(false)
                _playerState.update { it.copy(progress = 0f) }
            },
            onProgressUpdate = { newProgress ->
                _playerState.update { it.copy(progress = newProgress) }
            }
        )
        updatePlayingState(true, songUrl, fileName, newIndex)
    }

    private fun resumeCurrentSong() {
        musicPlayerService.resumeAudio { newProgress ->
            _playerState.update { it.copy(progress = newProgress) }
        }
        updatePlayingState(true)
    }

    private fun updatePlayingState(
        isPlaying: Boolean,
        url: String? = _playerState.value.currentlyPlayingUrl,
        fileName: String? = _playerState.value.selectedFileName,
        index: Int = _playerState.value.currentSongIndex
    ) {
        _playerState.update {
            it.copy(
                isPlaying = isPlaying,
                currentlyPlayingUrl = url,
                selectedFileName = fileName,
                currentSongIndex = index
            )
        }
    }

    private fun playNextSong(playlist: List<AudioMetadata>) {
        if (_playerState.value.isPlaying) {
            musicPlayerService.stopAudio()
        }
        viewModelScope.launch {
            val nextIndex = (_playerState.value.currentSongIndex + 1)
                .coerceIn(0, playlist.size - 1)

            playlist.getOrNull(nextIndex)?.let { nextSong ->
                nextSong.url?.let {
                    val decodedFileName = decodeFileName(nextSong.fileName)
                    handlePlayPause(nextSong.url, decodedFileName, nextIndex)
                }
            }
        }
    }

    private fun playPreviousSong(playlist: List<AudioMetadata>) {
        if (_playerState.value.isPlaying) {
            musicPlayerService.stopAudio()
        }
        viewModelScope.launch {
            val previousIndex = (_playerState.value.currentSongIndex - 1)
                .coerceIn(0, playlist.size - 1)

            playlist.getOrNull(previousIndex)?.let { previousSong ->
                previousSong.url?.let {
                    val decodedFileName = decodeFileName(previousSong.fileName)
                    handlePlayPause(previousSong.url, decodedFileName, previousIndex)
                }
            }
        }
    }

    private fun handleSeek(newProgress: Float) {
        viewModelScope.launch {
            musicPlayerService.seekTo(newProgress)
            _playerState.update { it.copy(progress = newProgress) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_playerState.value.isPlaying) {
            musicPlayerService.stopAudio()
        }
    }
}