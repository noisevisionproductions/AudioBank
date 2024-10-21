package org.noisevisionproductions.samplelibrary.composeUI.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.database.ForumService
import org.noisevisionproductions.samplelibrary.utils.UiState
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

class PostViewModel : ViewModel() {
    private val authService = AuthService()
    private val forumService = ForumService()

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _selectedSortingOption = MutableStateFlow("Najnowsze")
    private val selectedSortingOption: StateFlow<String> = _selectedSortingOption.asStateFlow()

    private var postsWithCategories: List<PostWithCategory> = emptyList()
    private val categoryCache = mutableMapOf<String, String>()

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    private val posts: StateFlow<List<PostModel>> = _posts

    private var lastLoadedPostId: String? = null
    private var isLoading = false
    private var allPostsLoaded = false

    init {
        loadPosts()
    }

    fun getPostById(postId: String): StateFlow<PostModel?> {
        return posts.map { postList ->
            postList.find { it.postId == postId }
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )
    }

    private fun loadPosts() {
        viewModelScope.launch {
            val result = forumService.getPostsFromFirestore(
                null,
                selectedCategory.value,
                selectedSortingOption.value
            )
            result.onSuccess { postsWithCategories ->
                val updatedPosts = postsWithCategories.map { postWithCategories ->
                    val post = postWithCategories.post
                    val isLiked = authService.isPostLiked(post.postId)
                    post.copy(isLiked = isLiked)
                }
                _uiState.value = UiState.Success(postsWithCategories)
                _posts.value = updatedPosts
            }.onFailure { error ->
                _uiState.value = UiState.Error("Error loading posts: ${error.message}")
            }
        }
    }

    private fun loadMorePosts() {
        if (isLoading || allPostsLoaded) return
        isLoading = true
        _isLoadingMore.value = true

        viewModelScope.launch {
            try {
                val result = forumService.getPostsFromFirestore(
                    lastLoadedPostId,
                    selectedCategoryId.value,
                    selectedSortingOption.value
                )
                result.onSuccess { postWithCategoriesList ->

                    postsWithCategories = postsWithCategories + postWithCategoriesList

                    if (postWithCategoriesList.size < 10) {
                        allPostsLoaded = true
                    }

                    lastLoadedPostId = postWithCategoriesList.lastOrNull()?.post?.postId

                    _uiState.value = UiState.Success(postsWithCategories)

                    _posts.value += postWithCategoriesList.map { it.post }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Nieoczekiwany błąd: ${e.message}")
            } finally {
                isLoading = false
                _isLoadingMore.value = false
            }
        }
    }

    fun onScrollToEnd() {
        loadMorePosts()
    }

    suspend fun getCategoryName(categoryId: String): String {
        return categoryCache[categoryId] ?: try {
            val categoryName = forumService.getCategoryName(categoryId)
            categoryCache[categoryId] = categoryName
            categoryName
        } catch (e: Exception) {
            println("error category" + e.message)
            "Unknown Category"
        }
    }

    fun setSelectedCategory(categoryName: String?) {
        viewModelScope.launch {
            if (categoryName == null) {
                _selectedCategory.value = null
                _selectedCategoryId.value = null
            } else {
                val categoryMap = forumService.getCategoryNames(listOf())
                println("testestsetset$categoryMap")
                val category = categoryMap.values.firstOrNull { it.name == categoryName }

                if (category != null) {
                    _selectedCategory.value = categoryName
                    _selectedCategoryId.value = category.id
                } else {
                    _selectedCategory.value = null
                    _selectedCategoryId.value = null
                }
            }
            reloadPosts()
        }
    }

    fun setSelectedSortingOption(sortingOption: String) {
        _selectedSortingOption.value = sortingOption
        reloadPosts()
    }

    private fun reloadPosts() {
        postsWithCategories = emptyList()
        lastLoadedPostId = null
        allPostsLoaded = false
        loadMorePosts()
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            _posts.update { currentPosts ->
                currentPosts.map { post ->
                    if (post.postId == postId) updatePostLikeStatus(post, !post.isLiked) else post
                }
            }

            val result = authService.toggleLikePost(postId)
            result.onFailure { error ->
                _posts.update { currentPosts ->
                    currentPosts.map { post ->
                        if (post.postId == postId) updatePostLikeStatus(
                            post,
                            !post.isLiked
                        ) else post
                    }
                }
                _uiState.value = UiState.Error("Error toggling like: ${error.message}")
            }
        }
    }

    private fun updatePostLikeStatus(post: PostModel, shouldBeLiked: Boolean): PostModel {
        return post.copy(
            isLiked = shouldBeLiked,
            likesCount = if (shouldBeLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(
                0
            )
        )
    }
}