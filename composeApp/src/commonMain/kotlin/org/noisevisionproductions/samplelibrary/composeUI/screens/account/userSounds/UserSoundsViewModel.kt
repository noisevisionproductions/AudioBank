package org.noisevisionproductions.samplelibrary.composeUI.screens.account.userSounds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement.SharedSoundEventsManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement.SoundEvent
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.metadata.AudioMetadata

class UserSoundsViewModel(
    private val storageRepository: FirebaseStorageRepository,
    private val userRepository: UserRepository,
    private val sharedSoundEventsManager: SharedSoundEventsManager
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
        observeSoundEvents()
    }


    private fun loadUsername() {
        viewModelScope.launch {
            val userId = userRepository.getCurrentUser()
            userId?.username?.let { username ->
                _username.value = username
                loadUserSounds(username)
                loadFavoriteSounds()
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
            _isLoading.value = true
            try {
                val likedSoundIds = userRepository.getLikedSounds()
                if (likedSoundIds.isNotEmpty()) {
                    val result = storageRepository.getSoundsMetadataByIds(likedSoundIds)
                    result.onSuccess { sounds ->
                        _favoriteSounds.value = sounds
                    }
                } else {
                    _favoriteSounds.value = emptyList()
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Błąd podczas ładowania ulubionych dźwięków\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "LOAD_FAVORITE_SOUNDS_ERROR",
                    retryAction = { loadFavoriteSounds() }
                )
                println(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeSoundEvents() {
        viewModelScope.launch {
            sharedSoundEventsManager.soundEvents.collect { event ->
                when (event) {
                    is SoundEvent.SoundLiked -> {
                        val likedSound = _userSounds.value.find { it.id == event.soundId }
                        likedSound?.let {
                            _favoriteSounds.value += it
                        }
                    }

                    is SoundEvent.SoundUnliked -> {
                        _favoriteSounds.value =
                            _favoriteSounds.value.filter { it.id != event.soundId }
                    }

                    is SoundEvent.SoundDeleted -> {
                        _userSounds.value = _userSounds.value.filter { it.id != event.soundId }
                        _favoriteSounds.value =
                            _favoriteSounds.value.filter { it.id != event.soundId }
                    }

                    is SoundEvent.SoundMetadataUpdated -> {
                        updateLocalSoundMetadata(event.soundId)
                    }

                    is SoundEvent.SoundUploaded -> {
                        _userSounds.value = listOf(event.sound) + _userSounds.value
                    }
                }
            }
        }
    }

    private fun updateLocalSoundMetadata(soundId: String) {
        viewModelScope.launch {
            try {
                val updatedMetadata = storageRepository.getSoundMetadata(soundId)

                _userSounds.update { currentSounds ->
                    currentSounds.map { sound ->
                        if (sound.id == soundId) updatedMetadata else sound
                    }
                }

                _favoriteSounds.update { currentFavorites ->
                    currentFavorites.map { sound ->
                        if (sound.id == soundId) updatedMetadata else sound
                    }
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Błąd podczas aktualizacji danych - ${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "UPDATE_LOCAL_SOUND_METADATA_ERROR",
                    retryAction = { updateLocalSoundMetadata(soundId) }
                )
                println(e)
            }
        }
    }

    fun updateSoundMetadata(soundId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            try {
                storageRepository.updateSoundMetadata(soundId, updates)
                    .onSuccess {
                        sharedSoundEventsManager.emitEvent(
                            SoundEvent.SoundMetadataUpdated(
                                soundId,
                                updates
                            )
                        )

                        updateLocalSoundMetadata(soundId)
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
                            sharedSoundEventsManager.emitEvent(SoundEvent.SoundDeleted(soundId))
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