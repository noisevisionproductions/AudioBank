package org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

class AccountViewModel(
    private val userRepository: UserRepository,
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val sharedErrorViewModel: SharedErrorViewModel,
    private val avatarPickerRepositoryImpl: AvatarPickerRepositoryImpl
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    private val _selectedImagePath = MutableStateFlow<ByteArray?>(null)
    val selectedImagePath = _selectedImagePath.asStateFlow()

    private val _likedPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val likedPosts = _likedPosts.asStateFlow()

    init {
        loadUserData()
        loadLikedPosts()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _userState.value = UserState.Success(user)
                }
            } catch (e: Exception) {
                sharedErrorViewModel.showError(
                    UserErrorInfo(
                        message = "Nie udało się załadować danych użytkownika",
                        actionType = UserErrorAction.RETRY,
                        errorId = "LOAD_USER_DATA_ERROR",
                        retryAction = { loadUserData() }
                    )
                )
            }
        }
    }

    private fun loadLikedPosts() {
        viewModelScope.launch {
            try {
                val posts = userRepository.getLikedPosts()
                _likedPosts.value = posts
            } catch (e: Exception) {
                sharedErrorViewModel.showError(
                    UserErrorInfo(
                        message = "Nie udało się załadować polubionych postów",
                        actionType = UserErrorAction.RETRY,
                        errorId = "LOAD_LIKED_POSTS_ERROR",
                        retryAction = { loadLikedPosts() }
                    )
                )
            }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                userRepository.updateUsername(newUsername)
                loadUserData()
            } catch (e: Exception) {
                sharedErrorViewModel.showError(
                    UserErrorInfo(
                        message = "Nie udało się zaktualizować nazwy użytkownika",
                        actionType = UserErrorAction.OK,
                        errorId = "UPDATE_USERNAME_ERROR"
                    )
                )
            }
        }
    }

    fun pickAvatar() {
        viewModelScope.launch {
            try {
                avatarPickerRepositoryImpl.pickAvatar()?.let { filePath ->
                    updateAvatar(filePath)
                }
            } catch (e: Exception) {
                handleError("Błąd podczas wyboru avatara", e)
            }
        }
    }

    private fun updateAvatar(filePath: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            try {
                val imageUrl = firebaseStorageRepository.uploadImage(filePath)
                userRepository.updateAvatarUrl(imageUrl)
                loadUserData()
            } catch (e: Exception) {
                handleError("Błąd podczas aktualizacji avatara", e)
            }
        }
    }

    fun removeLikedPost(postId: String) {
        viewModelScope.launch {
            try {
                userRepository.removeLikedPost(postId)
                _likedPosts.value = _likedPosts.value.filter { it.postId != postId }
                loadUserData()
            } catch (e: Exception) {
                sharedErrorViewModel.showError(
                    UserErrorInfo(
                        message = "Nie udało się usunąć polubionego postu",
                        actionType = UserErrorAction.OK,
                        errorId = "REMOVE_LIKED_POST_ERROR"
                    )
                )
                println(e.message)
            }
        }
    }

    private fun handleError(message: String, error: Exception) {
        sharedErrorViewModel.showError(
            UserErrorInfo(
                message = message,
                actionType = UserErrorAction.RETRY,
                errorId = "AVATAR_ERROR",
                retryAction = { loadUserData() }
            )
        )
        println(error)
    }
}