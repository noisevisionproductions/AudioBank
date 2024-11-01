package org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import org.noisevisionproductions.samplelibrary.utils.decodeFileName
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

class MusicPlayerViewModel(
    private val musicPlayerService: MusicPlayerService
) : ViewModel() {
    data class PlayerState(
        val isPlaying: Boolean = false,
        val currentlyPlayingUrl: String? = null,
        val selectedFileName: String? = null,
        val progress: Float = 0f,
        val currentSongIndex: Int = 0,
        val isCompleted: Boolean = false,
        val bpm: String = "-",
        val tone: String = "-",
        val songId: String = "",
        val tags: List<String> = emptyList()
    )

    sealed class PlayerAction {
        data class PlayPause(
            val songUrl: String,
            val fileName: String,
            val index: Int,
            val bpm: String = "-",
            val tone: String = "-",
            val songId: String = "",
            val tags: List<String> = emptyList()
        ) : PlayerAction()

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
                action.index,
                action.bpm,
                action.tone,
                action.songId,
                action.tags
            )

            is PlayerAction.Next -> playNextSong(currentPlaylist)
            is PlayerAction.Previous -> playPreviousSong(currentPlaylist)
            is PlayerAction.Seek -> handleSeek(action.position)
        }
    }

    private fun handlePlayPause(
        songUrl: String,
        fileName: String,
        newIndex: Int,
        bpm: String,
        tone: String,
        songId: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val currentState = _playerState.value

            when {
                currentState.isPlaying && currentState.currentlyPlayingUrl == songUrl -> {
                    musicPlayerService.pauseAudio()
                    updatePlayingState(false)
                }

                currentState.currentlyPlayingUrl != null && currentState.currentlyPlayingUrl != songUrl -> {
                    musicPlayerService.stopAudio()
                    playNewSong(songUrl, fileName, newIndex, bpm, tone, songId, tags)
                }

                currentState.currentlyPlayingUrl == songUrl && !currentState.isPlaying -> {
                    if (currentState.isCompleted) {
                        // Preserve the metadata when replaying the completed song
                        playNewSong(
                            songUrl = songUrl,
                            fileName = currentState.selectedFileName ?: fileName,
                            newIndex = currentState.currentSongIndex,
                            bpm = currentState.bpm,
                            tone = currentState.tone,
                            songId = currentState.songId,
                            tags = currentState.tags
                        )
                    } else {
                        resumeCurrentSong()
                    }
                }

                else -> playNewSong(songUrl, fileName, newIndex, bpm, tone, songId, tags)
            }
        }
    }

    private suspend fun playNewSong(
        songUrl: String,
        fileName: String,
        newIndex: Int,
        bpm: String,
        tone: String,
        songId: String,
        tags: List<String>
    ) {
        musicPlayerService.playAudioFromUrl(
            audioUrl = songUrl,
            currentlyPlayingUrl = _playerState.value.currentlyPlayingUrl,
            onCompletion = {
                updatePlayingState(false, isCompleted = true)
                _playerState.update { it.copy(progress = 0f) }
            },
            onProgressUpdate = { newProgress ->
                _playerState.update { it.copy(progress = newProgress) }
            }
        )
        updatePlayingState(
            true,
            songUrl,
            fileName,
            newIndex,
            isCompleted = false,
            bpm = bpm,
            tone = tone,
            songId = songId,
            tags = tags
        )
    }

    private fun resumeCurrentSong() {
        musicPlayerService.resumeAudio { newProgress ->
            _playerState.update { it.copy(progress = newProgress) }
        }
        updatePlayingState(true, isCompleted = false)
    }

    private fun updatePlayingState(
        isPlaying: Boolean,
        url: String? = _playerState.value.currentlyPlayingUrl,
        fileName: String? = _playerState.value.selectedFileName,
        index: Int = _playerState.value.currentSongIndex,
        isCompleted: Boolean = _playerState.value.isCompleted,
        bpm: String = _playerState.value.bpm,
        tone: String = _playerState.value.tone,
        songId: String = _playerState.value.songId,
        tags: List<String> = _playerState.value.tags
    ) {
        _playerState.update {
            it.copy(
                isPlaying = isPlaying,
                currentlyPlayingUrl = url,
                selectedFileName = fileName,
                currentSongIndex = index,
                isCompleted = isCompleted,
                bpm = bpm,
                tone = tone,
                songId = songId,
                tags = tags
            )
        }
    }

    private fun playNextSong(playlist: List<AudioMetadata>) {
        if (_playerState.value.isPlaying) {
            musicPlayerService.stopAudio()
        }
        viewModelScope.launch {
            val nextIndex = (_playerState.value.currentSongIndex + 1) % playlist.size

            playlist.getOrNull(nextIndex)?.let { nextSong ->
                nextSong.url?.let { url ->
                    val decodedFileName = decodeFileName(nextSong.fileName)
                    val bpm = nextSong.bpm ?: "-"
                    val tone = nextSong.tone ?: "-"
                    val songId = nextSong.id ?: ""
                    val tags = nextSong.tags
                    handlePlayPause(url, decodedFileName, nextIndex, bpm, tone, songId, tags)
                }
            }
        }
    }

    private fun playPreviousSong(playlist: List<AudioMetadata>) {
        if (_playerState.value.isPlaying) {
            musicPlayerService.stopAudio()
        }
        viewModelScope.launch {
            val previousIndex =
                (_playerState.value.currentSongIndex - 1 + playlist.size) % playlist.size

            playlist.getOrNull(previousIndex)?.let { previousSong ->
                previousSong.url?.let { url ->
                    val decodedFileName = decodeFileName(previousSong.fileName)
                    val bpm = previousSong.bpm ?: "-"
                    val tone = previousSong.tone ?: "-"
                    val songId = previousSong.id ?: ""
                    val tags = previousSong.tags
                    handlePlayPause(url, decodedFileName, previousIndex, bpm, tone, songId, tags)
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