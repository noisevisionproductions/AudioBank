package org.noisevisionproductions.samplelibrary.composeUI.screens.account.userProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.PostsRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.LocalStorageRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

class AccountViewModel(
    private val likeManager: LikeManager,
    private val userRepository: UserRepository,
    private val postsRepository: PostsRepository,
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val sharedErrorViewModel: SharedErrorViewModel,
    private val avatarPickerRepositoryImpl: AvatarPickerRepositoryImpl,
    private val localStorageRepositoryImpl: LocalStorageRepositoryImpl
) : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    private val _likedPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val likedPosts: StateFlow<List<PostModel>> = _likedPosts.asStateFlow()

    private val _createdPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val createdPosts = _createdPosts.asStateFlow()

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl.asStateFlow()

    init {
        viewModelScope.launch {
            localStorageRepositoryImpl.getAvatarUrl()?.let { url ->
                _avatarUrl.value = url
            }
            observeLikedPosts()
            loadUserData()
            loadLikedPosts()
            loadCreatedPosts()
        }
    }

    private fun observeLikedPosts() {
        viewModelScope.launch {
            likeManager.likedPostsIds.collect { likedPostIds ->
                val posts = likedPostIds.mapNotNull { postId ->
                    postsRepository.getPost(postId).getOrNull()
                }
                _likedPosts.value = posts
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    if (user != null) {
                        _userState.value = UserState.Success(user)
                    } else {
                        println("User data not found")
                    }
                },
                onFailure = { e ->
                    sharedErrorViewModel.showError(
                        UserErrorInfo(
                            message = "Nie udało się załadować danych użytkownika",
                            actionType = UserErrorAction.RETRY,
                            errorId = "LOAD_USER_DATA_ERROR",
                            retryAction = { loadUserData() }
                        )
                    )
                    println("Error loading user data: ${e.message}")
                }
            )
        }
    }

    private fun loadCreatedPosts() {
        viewModelScope.launch {
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    if (user != null) {
                        userRepository.getPostsByIds(user.postIds).fold(
                            onSuccess = { posts ->
                                _createdPosts.value = posts
                            },
                            onFailure = { e ->
                                sharedErrorViewModel.showError(
                                    UserErrorInfo(
                                        message = "Nie udało się załadować utworzonych postów",
                                        actionType = UserErrorAction.RETRY,
                                        errorId = "LOAD_CREATED_POSTS_ERROR",
                                        retryAction = { loadCreatedPosts() }
                                    )
                                )
                                println("Error loading created posts: ${e.message}")
                            }
                        )
                    } else {
                        println("User data not found")
                    }
                },
                onFailure = { e ->
                    sharedErrorViewModel.showError(
                        UserErrorInfo(
                            message = "Nie udało się załadować danych użytkownika",
                            actionType = UserErrorAction.RETRY,
                            errorId = "LOAD_USER_DATA_ERROR",
                            retryAction = { loadCreatedPosts() }
                        )
                    )
                    println("Error loading user data for created posts: ${e.message}")
                }
            )
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
                            message = "Nie udało się załadować polubionych postów",
                            actionType = UserErrorAction.RETRY,
                            errorId = "LOAD_LIKED_POSTS_ERROR",
                            retryAction = { loadLikedPosts() }
                        )
                    )
                    println("Error loading liked posts: ${error.message}")
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
                firebaseStorageRepository.uploadImage(filePath)
                    .onSuccess { imageUrl ->
                        userRepository.updateAvatarUrl(imageUrl).onSuccess {
                            _avatarUrl.value = imageUrl
                            localStorageRepositoryImpl.saveAvatarUrl(imageUrl)
                            localStorageRepositoryImpl.saveAvatarImage(filePath)
                            loadUserData()
                        }.onFailure { error ->
                            UserErrorInfo(
                                message = "Error uploading avatar image - ${error.message}",
                                actionType = UserErrorAction.RETRY,
                                errorId = "AVATAR_ERROR",
                                retryAction = { loadUserData() }
                            )
                            println(error)
                        }
                    }
                    .onFailure { uploadError ->
                        UserErrorInfo(
                            message = "Error uploading avatar image - ${uploadError.message}",
                            actionType = UserErrorAction.RETRY,
                            errorId = "AVATAR_ERROR",
                            retryAction = { loadUserData() }
                        )
                        println(uploadError)
                    }
            } catch (e: Exception) {
                handleError("Unexpected error during avatar update", e)
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
        println("Error: $message - ${error.message}")
    }
}