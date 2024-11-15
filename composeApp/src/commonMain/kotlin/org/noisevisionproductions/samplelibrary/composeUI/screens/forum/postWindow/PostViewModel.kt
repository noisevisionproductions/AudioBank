package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.forumManagemenr.PostCacheManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.forumManagemenr.PostFilterManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.forumManagemenr.PostViewModelState
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

    private val state = PostViewModelState()
    private val filterManager = PostFilterManager()
    val cacheManager = PostCacheManager()

    private var lastLoadedPostId: String? = null
    private var isLoading = false
    private var allPostsLoaded = false

    val uiState = state.uiState
    val isLoadingMore = state.isLoadingMore
    val filteredPosts = state.filteredPosts
    val categoryNames = state.categoryNames

    var firstVisibleItemIndex = 0
    var firstVisibleItemScrollOffset = 0

    init {
        initializeViewModel()
    }

    private fun initializeViewModel() {
        loadPosts()
        preloadCategories()
        setupFilterCombine()
    }

    private fun setupFilterCombine() {
        viewModelScope.launch {
            combine(
                filterManager.searchQuery,
                filterManager.selectedCategoryId,
                filterManager.selectedSortingOption,
                state.posts,
                state.categoryNames
            ) { query, categoryId, sortOption, posts, _ ->
                filterAndSortPosts(posts, query, categoryId, sortOption)
            }.collect { filtered ->
                state.updateFilteredPosts(filtered)
            }
        }
    }

    private fun filterAndSortPosts(
        posts: List<PostModel>,
        query: String,
        categoryId: String?,
        sortOption: String
    ): List<PostModel> {
        return posts
            .filter { post ->
                val matchesQuery = query.isEmpty() ||
                        post.title.contains(query, ignoreCase = true) ||
                        post.content.contains(query, ignoreCase = true)
                val matchesCategory = categoryId == null || post.categoryId == categoryId
                matchesQuery && matchesCategory
            }
            .distinct()
            .sortedWith { post1, post2 ->
                when (sortOption) {
                    "Najnowsze" -> post2.timestamp.compareTo(post1.timestamp)
                    "Najstarsze" -> post1.timestamp.compareTo(post2.timestamp)
                    else -> 0
                }
            }
    }

    fun getPostById(postId: String): StateFlow<PostModel?> {
        viewModelScope.launch {
            loadPostById(postId)
        }
        return state.individualPost
    }

    private suspend fun loadPostById(postId: String) {
        val cachedPost = cacheManager.getCachedPost(postId)
        when {
            cachedPost != null -> state.updateIndividualPost(cachedPost)
            else -> fetchPostFromRepository(postId)
        }
    }

    private suspend fun fetchPostFromRepository(postId: String) {
        state.updateIndividualPost(null)
        postsRepository.getPost(postId)
            .onSuccess { post ->
                cacheManager.cachePost(post)
                state.updateIndividualPost(post)
                initializePostLikeState(post)
            }
            .onFailure { error ->
                state.updateUiState(UiState.Error("Error loading post: ${error.message}"))
            }
    }

    private suspend fun initializePostLikeState(post: PostModel) {
        likeRepository.isPostLiked(post.postId).fold(
            onSuccess = { isLiked ->
                likeManager.initializePostLikeState(post, isLiked)
            },
            onFailure = { error ->
                println(error)
            }
        )
    }

    private fun loadPosts() {
        viewModelScope.launch {
            try {
                val result = postsRepository.getPostsFromFirestore(
                    null,
                    filterManager.selectedCategory.value,
                    filterManager.selectedSortingOption.value
                )
                result.onSuccess { postsWithCategories ->
                    val updatedPosts = postsWithCategories.map { postWithCategory ->
                        val post = postWithCategory.post

                        val isLikedResult = likeRepository.isPostLiked(post.postId)
                        val isLiked =
                            isLikedResult.getOrElse { false }

                        likeManager.initializePostLikeState(post, isLiked)
                        post.copy(isLiked = isLiked)
                    }
                    cacheManager.cachePosts(postsWithCategories)
                    state.updatePosts(updatedPosts)
                    state.updateUiState(UiState.Success(postsWithCategories))
                }.onFailure { error ->
                    state.updateUiState(UiState.Error("Failed to load posts: ${error.message}"))
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

    private fun preloadCategories() {
        viewModelScope.launch {
            val result = forumRepository.getCategories()
            result.onSuccess { categories ->
                val categoryMap = categories.associate { it.id to it.name }
                cacheManager.cacheCategories(categoryMap)
                state.updateCategoryNames(categoryMap)
            }.onFailure { exception ->
                UserErrorInfo(
                    message = "Błąd podczas ładowania kategorii - ${exception.message}",
                    actionType = UserErrorAction.OK,
                    errorId = "PRELOAD_CATEGORIES_ERROR"
                )
                println(exception)
            }
        }
    }

    fun filterPosts(query: String) {
        filterManager.updateSearchQuery(query)
    }

    fun setSelectedCategory(categoryName: String?) {
        viewModelScope.launch {
            updateSelectedCategory(categoryName)
        }
    }

    private suspend fun updateSelectedCategory(categoryName: String?) {
        if (categoryName == null) {
            filterManager.updateCategory(null, null)
        } else {
            forumRepository.getCategoryNames(listOf()).fold(
                onSuccess = { categoryMap ->
                    val category = categoryMap.values.firstOrNull { it.name == categoryName }
                    filterManager.updateCategory(
                        categoryName = if (category != null) categoryName else null,
                        categoryId = category?.id
                    )
                },
                onFailure = {
                    filterManager.updateCategory(null, null)
                }
            )
        }
        reloadPosts()
    }

    fun setSelectedSortingOption(sortingOption: String) {
        filterManager.updateSortingOption(sortingOption)
        reloadPosts()
    }

    private fun reloadPosts() {
        cacheManager.clearPostsCache()
        lastLoadedPostId = null
        allPostsLoaded = false
        loadMorePosts()
    }

    fun onScrollToEnd() {
        loadMorePosts()
    }

    private fun loadMorePosts() {
        viewModelScope.launch {
            if (isLoading || allPostsLoaded || state.isLoadingMore.value) return@launch

            try {
                state.updateLoadingMore(true)
                isLoading = true

                delay(300)

                loadNextBatchOfPosts()
            } catch (e: Exception) {
                handleLoadMorePostsError(e)
            } finally {
                isLoading = false
                state.updateLoadingMore(false)
            }
        }
    }

    private suspend fun loadNextBatchOfPosts() {
        postsRepository.getPostsFromFirestore(
            lastLoadedPostId,
            filterManager.selectedCategoryId.value,
            filterManager.selectedSortingOption.value
        ).onSuccess { posts ->
            if (posts.isEmpty()) {
                allPostsLoaded = true
                return
            }

            processFetchedPosts(posts)
        }
    }

    private suspend fun processFetchedPosts(posts: List<PostWithCategory>) {
        cacheManager.cachePosts(posts)
        lastLoadedPostId = posts.lastOrNull()?.post?.postId

        posts.forEach { postWithCategory ->
            initializePostLikeState(postWithCategory.post)
        }

        state.appendPosts(posts.map { it.post })
        state.updateUiState(UiState.Success(cacheManager.getPostsWithCategories()))

        allPostsLoaded = posts.size < 10
    }

    private fun handleLoadMorePostsError(error: Exception) {
        state.updateUiState(UiState.Error("Nieoczekiwany błąd: ${error.message}"))
        UserErrorInfo(
            message = "Błąd podczas ładowania kategorii - ${error.message}",
            actionType = UserErrorAction.OK,
            errorId = "PRELOAD_CATEGORIES_ERROR"
        )
        println(error)
    }

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            handlePostLikeToggle(postId)
        }
    }

    private suspend fun handlePostLikeToggle(postId: String) {
        val currentState = likeManager.getPostLikeState(postId)
        val newLikeState = !currentState.isLiked
        val newLikesCount = currentState.likesCount + if (newLikeState) 1 else -1

        updateLikeState(postId, newLikeState, newLikesCount, currentState)
    }

    private suspend fun updateLikeState(
        postId: String,
        newLikeState: Boolean,
        newLikesCount: Int,
        currentState: LikeManager.LikeState
    ) {
        try {
            likeManager.updatePostLike(
                postId,
                LikeManager.LikeState(newLikeState, newLikesCount.coerceAtLeast(0))
            )

            likeRepository.toggleLikePost(postId)
                .onSuccess {
                    updateLikeCountFromServer(postId, newLikeState)
                }
                .onFailure { error ->
                    handleLikeError(postId, currentState, error)
                }
        } catch (e: Exception) {
            handleLikeError(postId, currentState, e)
        }
    }

    private suspend fun updateLikeCountFromServer(postId: String, isLiked: Boolean) {
        likeRepository.getPostLikesCount(postId)
            .onSuccess { serverLikesCount ->
                likeManager.updatePostLike(
                    postId,
                    LikeManager.LikeState(isLiked, serverLikesCount)
                )
            }
    }

    private fun handleLikeError(
        postId: String,
        currentState: LikeManager.LikeState,
        error: Throwable
    ) {
        likeManager.updatePostLike(postId, currentState)
        state.updateUiState(UiState.Error("Error toggling like: ${error.message}"))
        if (error is Exception) {
            UserErrorInfo(
                message = "Błąd podczas ładowania kategorii - ${error.message}",
                actionType = UserErrorAction.OK,
                errorId = "PRELOAD_CATEGORIES_ERROR"
            )
            println(error)
        }
    }
}