package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeService
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.utils.UiState
import org.noisevisionproductions.samplelibrary.utils.models.PostModel
import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

class PostViewModel(
    private val forumRepository: ForumRepository,
    private val likeManager: LikeManager,
    private val likeService: LikeService
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

    private val _posts = MutableStateFlow<List<PostModel>>(emptyList())
    private val posts: StateFlow<List<PostModel>> = _posts

    private val _filteredPosts = MutableStateFlow<List<PostModel>>(emptyList())
    val filteredPosts: StateFlow<List<PostModel>> = _filteredPosts

    private val _searchQuery = MutableStateFlow("")

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
            val result = forumRepository.getPostsFromFirestore(
                null,
                selectedCategory.value,
                selectedSortingOption.value
            )
            result.onSuccess { postsWithCategories ->
                val updatedPosts = postsWithCategories.map { postWithCategories ->
                    val post = postWithCategories.post
                    val isLiked = likeService.isPostLiked(post.postId)
                    likeManager.initializePostLikeState(post.postId, isLiked, post.likesCount)
                    post.copy(isLiked = isLiked)
                }
                _uiState.value = UiState.Success(postsWithCategories)
                _posts.value = updatedPosts.distinct()
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
                val result = forumRepository.getPostsFromFirestore(
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
        return categoryCache[categoryId] ?: try {
            val categoryName = forumRepository.getCategoryName(categoryId)
            categoryCache[categoryId] = categoryName
            categoryName
        } catch (e: Exception) {
            println("Błąd podczas pobierania kategorii: ${e.message}")
            "Nieznana Kategoria"
        }
    }

    fun setSelectedCategory(categoryName: String?) {
        viewModelScope.launch {
            if (categoryName == null) {
                _selectedCategory.value = null
                _selectedCategoryId.value = null
            } else {
                val categoryMap = forumRepository.getCategoryNames(listOf())
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

    fun togglePostLike(postId: String) {
        viewModelScope.launch {
            val currentState = likeManager.getPostLikeState(postId)
            val newLikeState = !(currentState?.isLiked ?: false)
            val newLikesCount = (currentState?.likesCount ?: 0) + (if (newLikeState) 1 else -1)

            likeManager.updatePostLike(
                postId,
                LikeManager.LikeState(newLikeState, newLikesCount.coerceAtLeast(0))
            )

            try {
                val result = likeService.toggleLikePost(postId)
                result.onFailure { error ->
                    // Przywrócenie poprzedniego stanu w przypadku błędu
                    likeManager.updatePostLike(
                        postId,
                        LikeManager.LikeState(
                            currentState?.isLiked ?: false,
                            currentState?.likesCount ?: 0
                        )
                    )
                    _uiState.value = UiState.Error("Error toggling like: ${error.message}")
                }
            } catch (e: Exception) {
                // Przywrócenie poprzedniego stanu w przypadku wyjątku
                likeManager.updatePostLike(
                    postId,
                    LikeManager.LikeState(
                        currentState?.isLiked ?: false,
                        currentState?.likesCount ?: 0
                    )
                )
                _uiState.value = UiState.Error("Error toggling like: ${e.message}")
            }
        }
    }
}