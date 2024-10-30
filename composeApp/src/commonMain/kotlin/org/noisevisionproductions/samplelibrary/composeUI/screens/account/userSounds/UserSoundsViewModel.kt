package org.noisevisionproductions.samplelibrary.composeUI.screens.account.userSounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

class UserSoundsViewModel(
    private val storageRepository: FirebaseStorageRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _userSounds = MutableStateFlow<List<AudioMetadata>>(emptyList())
    val userSounds = _userSounds.asStateFlow()

    private val _favoriteSounds = MutableStateFlow<List<AudioMetadata>>(emptyList())
    val favoriteSounds = _favoriteSounds.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var lastDocumentId: String? = null
    private val _username = MutableStateFlow<String?>(null)

    init {
        loadUsername()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()
            userId?.username?.let { username ->
                _username.value = username
                loadUserSounds(username)
            }
        }
    }

    private fun loadUserSounds(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _userSounds.value = emptyList()
            try {
                val result = storageRepository.getUserSounds(username, 50, lastDocumentId)
                result.onSuccess { (sounds, lastId) ->
                    _userSounds.value = sounds
                    lastDocumentId = lastId
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Nie udało się załadować dźwięków\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "UPDATE_SOUND_METADATA_ERROR",
                    retryAction = { loadUserSounds(username) }
                )
                println(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadFavoriteSounds() {
        viewModelScope.launch {

        }
    }

    fun updateSoundMetadata(soundId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                storageRepository.updateSoundMetadata(soundId, updates)
                    .onSuccess {
                        _username.value?.let {
                            lastDocumentId = null
                            loadUserSounds(it)
                        }
                    }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Nie udało się zaktualizować danych - ${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "UPDATE_SOUND_METADATA_ERROR",
                    retryAction = { updateSoundMetadata(soundId, updates) }
                )
                println(e)
            }
        }
    }

    fun deleteSound(soundId: String, fileName: String) {
        viewModelScope.launch {
            try {
                _username.value?.let { username ->
                    storageRepository.deleteUserSound(username, soundId, fileName)
                        .onSuccess {
                            lastDocumentId = null
                            loadUserSounds(username)
                        }
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Nie udało się usunąć dźwięku - ${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "DELETE_SOUND_ERROR",
                    retryAction = { deleteSound(soundId, fileName) }
                )
                println(e)
            }
        }
    }
}