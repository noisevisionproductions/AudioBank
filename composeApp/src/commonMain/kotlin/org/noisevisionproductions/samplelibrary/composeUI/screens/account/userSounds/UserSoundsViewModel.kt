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

    private fun observeSoundEvents() {
        viewModelScope.launch {
            sharedSoundEventsManager.soundEvents.collect { event ->
                when (event) {
                    is SoundEvent.SoundLiked -> {
                        val likedSound = _userSounds.value.find { it.id == event.soundId }
                            ?: storageRepository.getSoundMetadata(event.soundId).getOrNull()
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

    private fun loadUsername() {
        viewModelScope.launch {
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    user?.username?.let { username ->
                        _username.value = username
                        loadUserSounds(username)
                        loadFavoriteSounds()
                    }
                },
                onFailure = { error ->
                    println("Error loading user data: ${error.message}")
                    _username.value = null
                }
            )
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
            userRepository.getLikedSounds().fold(
                onSuccess = { likedSoundIds ->
                    if (likedSoundIds.isNotEmpty()) {
                        storageRepository.getSoundsMetadataByIds(likedSoundIds).fold(
                            onSuccess = { sounds ->
                                _favoriteSounds.value = sounds
                            },
                            onFailure = { e ->
                                UserErrorInfo(
                                    message = "Błąd podczas ładowania ulubionych dźwięków\n${e.message}",
                                    actionType = UserErrorAction.RETRY,
                                    errorId = "LOAD_FAVORITE_SOUNDS_ERROR",
                                    retryAction = { loadFavoriteSounds() }
                                )
                                println("Error loading favorite sounds metadata: ${e.message}")
                            }
                        )
                    } else {
                        _favoriteSounds.value = emptyList()
                    }
                },
                onFailure = { e ->
                    UserErrorInfo(
                        message = "Błąd podczas ładowania ulubionych dźwięków\n${e.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "LOAD_FAVORITE_SOUNDS_ERROR",
                        retryAction = { loadFavoriteSounds() }
                    )
                    println("Error loading liked sound IDs: ${e.message}")
                }
            )
            _isLoading.value = false
        }
    }


    private fun updateLocalSoundMetadata(soundId: String) {
        viewModelScope.launch {
            try {
                val result = storageRepository.getSoundMetadata(soundId)
                result.onSuccess { updatedMetadata ->
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
                }.onFailure { error ->
                    UserErrorInfo(
                        message = "Error updating metadata - ${error.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "UPDATE_LOCAL_SOUND_METADATA_ERROR",
                        retryAction = { updateLocalSoundMetadata(soundId) }
                    )
                    println(error)
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Unexpected error - ${e.message}",
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