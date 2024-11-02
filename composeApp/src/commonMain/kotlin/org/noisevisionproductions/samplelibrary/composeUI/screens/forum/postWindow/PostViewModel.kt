package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.database.LikeRepository
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.PostsRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.UiState
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

class PostViewModel(
    private val forumRepository: ForumRepository,
    private val likeManager: LikeManager,
    private val likeRepository: LikeRepository,
    private val postsRepository: PostsRepository
) : ViewModel() {

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

    private val _categoryName = MutableStateFlow("")
    val categoryName: StateFlow<String> = _categoryName.asStateFlow()

    private val _categoryNames = MutableStateFlow<Map<String, String>>(emptyMap())
    val categoryNames: StateFlow<Map<String, String>> = _categoryNames

    private val _individualPost = MutableStateFlow<PostModel?>(null)
    private val individualPost: StateFlow<PostModel?> = _individualPost.asStateFlow()

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())

    private val _filteredPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val filteredPosts: StateFlow<List<PostModel>> = _filteredPosts

    private val _searchQuery = MutableStateFlow("")

    private val postsCache = mutableMapOf<String, PostModel?>()

    private var lastLoadedPostId: String? = null
    private var isLoading = false
    private var allPostsLoaded = false

    init {
        loadPosts()

        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedCategoryId,
                _selectedSortingOption,
                _posts
            ) { query, selectedCategoryId, selectedSortingOption, posts ->
                posts
                    .filter { post ->
                        val matchesQuery = query.isEmpty() || post.title.contains(
                            query,
                            ignoreCase = true
                        ) || post.content.contains(query, ignoreCase = true)
                        val matchesCategory =
                            selectedCategoryId == null || post.categoryId == selectedCategoryId
                        matchesQuery && matchesCategory
                    }
                    .distinct() // Zapewnienie, że lista postów jest unikalna
                    .sortedWith { post1, post2 ->
                        when (selectedSortingOption) {
                            "Najnowsze" -> post2.timestamp.compareTo(post1.timestamp)
                            "Najstarsze" -> post1.timestamp.compareTo(post2.timestamp)
                            else -> 0
                        }
                    }
            }.collect { filtered ->
                _filteredPosts.value = filtered
            }
        }

    }

    fun getPostById(postId: String): StateFlow<PostModel?> {
        viewModelScope.launch {
            // First check the cache and current posts
            val cachedPost = postsCache[postId]
            val currentPost = _posts.value.find { it.postId == postId }

            when {
                cachedPost != null -> _individualPost.value = cachedPost
                currentPost != null -> _individualPost.value = currentPost
                else -> {
                    _individualPost.value = null
                    postsRepository.getPost(postId)
                        .onSuccess { post ->
                            postsCache[postId] = post
                            _individualPost.value = post
                            likeRepository.isPostLiked(post.postId).fold(
                                onSuccess = { isLiked ->
                                    likeManager.initializePostLikeState(post, isLiked)
                                },
                                onFailure = { error ->
                                    println(error)
                                }
                            )
                        }
                        .onFailure { error ->
                            _uiState.value = UiState.Error("Error loading post: ${error.message}")
                        }
                }
            }
        }
        return individualPost
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                val result = postsRepository.getPostsFromFirestore(
                    null,
                    selectedCategory.value,
                    selectedSortingOption.value
                )
                result.onSuccess { postsWithCategories ->
                    val updatedPosts = postsWithCategories.map { postWithCategory ->
                        val post = postWithCategory.post

                        // Check like status for each post
                        val isLikedResult = likeRepository.isPostLiked(post.postId)
                        val isLiked =
                            isLikedResult.getOrElse { false }  // Default to false if error

                        likeManager.initializePostLikeState(post, isLiked)
                        post.copy(isLiked = isLiked)
                    }
                    _uiState.value = UiState.Success(postsWithCategories)
                    _posts.value = updatedPosts.distinct()
                }.onFailure { error ->
                    _uiState.value = UiState.Error("Error loading posts: ${error.message}")
                }
            } catch (e: Exception) {
                UserErrorInfo(
                    message = "Nie udało się załadować postów\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "LOAD_POSTS_ERROR",
                    retryAction = { loadPosts() }
                )
                println(e)
            }
        }
    }


    private fun loadMorePosts() {
        if (isLoading || allPostsLoaded) return

        viewModelScope.launch {
            isLoading = true
            _isLoadingMore.value = true

            try {
                val result = postsRepository.getPostsFromFirestore(
                    lastLoadedPostId,
                    selectedCategoryId.value,
                    selectedSortingOption.value
                )
                result.onSuccess { postWithCategoriesList ->
                    if (postWithCategoriesList.isEmpty()) {
                        allPostsLoaded = true
                        return@onSuccess
                    }

                    postWithCategoriesList.forEach {
                        postsCache[it.post.postId] = it.post
                    }

                    postsWithCategories = postsWithCategories + postWithCategoriesList
                    lastLoadedPostId = postWithCategoriesList.lastOrNull()?.post?.postId

                    postWithCategoriesList.forEach { postWithCategory ->
                        val post = postWithCategory.post

                        // Check like status for each post
                        likeRepository.isPostLiked(post.postId).fold(
                            onSuccess = { isLiked ->
                                likeManager.initializePostLikeState(post, isLiked)
                            },
                            onFailure = { error ->
                                println(error)
                            }
                        )
                    }

                    _posts.value += postWithCategoriesList.map { it.post }
                    _uiState.value = UiState.Success(postsWithCategories)

                    allPostsLoaded = postWithCategoriesList.size < 10
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Nieoczekiwany błąd: ${e.message}")
                UserErrorInfo(
                    message = "Nie udało się załadować postów\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "LOAD_MORE_POSTS_ERROR",
                    retryAction = { loadMorePosts() }
                )
                println(e)
            } finally {
                isLoading = false
                _isLoadingMore.value = false
            }
        }
    }


    fun filterPosts(query: String) {
        _searchQuery.value = query
    }

    fun onScrollToEnd() {
        loadMorePosts()
    }

    fun fetchCategoryName(categoryId: String) {
        viewModelScope.launch {
            val name = getCategoryName(categoryId)
            _categoryNames.update { currentMap ->
                currentMap + (categoryId to name)
            }
        }
    }

    private suspend fun getCategoryName(categoryId: String): String {
        return categoryCache[categoryId] ?: run {
            val result = forumRepository.getCategoryName(categoryId)

            result.fold(
                onSuccess = { categoryName ->
                    categoryCache[categoryId] = categoryName
                    categoryName
                },
                onFailure = { exception ->
                    println("Błąd podczas pobierania kategorii: ${exception.message}")
                    "Nieznana Kategoria"
                }
            )
        }
    }

    fun setSelectedCategory(categoryName: String?) {
        viewModelScope.launch {
            if (categoryName == null) {
                _selectedCategory.value = null
                _selectedCategoryId.value = null
            } else {
                val categoryResult = forumRepository.getCategoryNames(listOf())

                categoryResult.fold(
                    onSuccess = { categoryMap ->
                        val category = categoryMap.values.firstOrNull { it.name == categoryName }
                        if (category != null) {
                            _selectedCategory.value = categoryName
                            _selectedCategoryId.value = category.id
                        } else {
                            _selectedCategory.value = null
                            _selectedCategoryId.value = null
                        }
                    },
                    onFailure = { exception ->
                        println(exception)
                        _selectedCategory.value = null
                        _selectedCategoryId.value = null
                    }
                )
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

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            val currentState = likeManager.getPostLikeState(postId)
            val newLikeState = !currentState.isLiked
            val newLikesCount = currentState.likesCount + if (newLikeState) 1 else -1

            likeManager.updatePostLike(
                postId,
                LikeManager.LikeState(newLikeState, newLikesCount.coerceAtLeast(0))
            )

            try {
                likeRepository.toggleLikePost(postId)
                    .onSuccess {
                        likeRepository.getPostLikesCount(postId)
                            .onSuccess { serverLikesCount ->
                                likeManager.updatePostLike(
                                    postId,
                                    LikeManager.LikeState(newLikeState, serverLikesCount)
                                )
                            }
                    }
                    .onFailure { error ->
                        likeManager.updatePostLike(postId, currentState)
                        _uiState.value = UiState.Error("Error toggling like: ${error.message}")
                    }
            } catch (e: Exception) {
                likeManager.updatePostLike(postId, currentState)
                _uiState.value = UiState.Error("Error toggling like: ${e.message}")
                UserErrorInfo(
                    message = "Nie udało się zmienić polubienia\n${e.message}",
                    actionType = UserErrorAction.RETRY,
                    errorId = "TOOGLE_POST_LIKE_ERROR",
                    retryAction = { togglePostLike(postId) }
                )
                println(e)
            }
        }
    }
}