package org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

class AccountViewModel(
    private val likeManager: LikeManager,
    private val userRepository: UserRepository,
    private val forumRepository: ForumRepository,
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val sharedErrorViewModel: SharedErrorViewModel,
    private val avatarPickerRepositoryImpl: AvatarPickerRepositoryImpl
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    private val _likedPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val likedPosts: StateFlow<List<PostModel>> = _likedPosts.asStateFlow()

    private val _createdPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val createdPosts = _createdPosts.asStateFlow()

    init {
        observeLikedPosts()
        loadUserData()
        loadLikedPosts()
        loadCreatedPosts()
    }

    private fun observeLikedPosts() {
        viewModelScope.launch {
            likeManager.likedPostsIds.collect { likedPostIds ->
                val posts = likedPostIds.mapNotNull { postId ->
                    forumRepository.getPost(postId).getOrNull()
                }
                _likedPosts.value = posts
            }
        }
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

    private fun loadCreatedPosts() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    val posts = userRepository.getPostsByIds(user.postIds)
                    _createdPosts.value = posts
                }
            } catch (e: Exception) {
                sharedErrorViewModel.showError(
                    UserErrorInfo(
                        message = "Nie udało się załadować utworzonych postów",
                        actionType = UserErrorAction.RETRY,
                        errorId = "LOAD_CREATED_POSTS_ERROR",
                        retryAction = { loadCreatedPosts() }
                    )
                )
            }
        }
    }

    private fun loadLikedPosts() {
        viewModelScope.launch {
            userRepository.getLikedPosts()
                .onSuccess { posts ->
                    _likedPosts.value = posts
                    likeManager.updateLikedPosts(posts)
                }
                .onFailure { error ->
                    sharedErrorViewModel.showError(
                        UserErrorInfo(
                            message = "Failed to load liked posts",
                            actionType = UserErrorAction.RETRY,
                            errorId = "LOAD_LIKED_POSTS_ERROR",
                            retryAction = { loadLikedPosts() }
                        )
                    )
                    println(error)
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