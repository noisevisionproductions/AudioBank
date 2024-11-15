package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.forumManagemenr

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.noisevisionproductions.samplelibrary.utils.UiState
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

class PostViewModelState {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts.asStateFlow()

    private val _filteredPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val filteredPosts: StateFlow<List<PostModel>> = _filteredPosts

    private val _individualPost = MutableStateFlow<PostModel?>(null)
    val individualPost: StateFlow<PostModel?> = _individualPost.asStateFlow()

    private val _categoryNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryNames: StateFlow<Map<String, String>> = _categoryNames

    fun updateUiState(newState: UiState) {
        _uiState.value = newState
    }

    fun updateLoadingMore(isLoading: Boolean) {
        _isLoadingMore.value = isLoading
    }

    fun updatePosts(posts: List<PostModel>) {
        _posts.value = posts
    }

    fun appendPosts(newPosts: List<PostModel>) {
        _posts.value += newPosts
    }

    fun updateFilteredPosts(posts: List<PostModel>) {
        _filteredPosts.value = posts
    }

    fun updateIndividualPost(post: PostModel?) {
        _individualPost.value = post
    }

    fun updateCategoryNames(categories: Map<String, String>) {
        _categoryNames.value = categories
    }
}
