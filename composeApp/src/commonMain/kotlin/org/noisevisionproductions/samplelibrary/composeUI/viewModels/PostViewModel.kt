package org.noisevisionproductions.samplelibrary.composeUI.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.ForumService
import org.noisevisionproductions.samplelibrary.utils.models.PostModel

class PostViewModel : ViewModel() {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var _posts = MutableStateFlow<List<PostModel>>(emptyList())
    val posts: StateFlow<List<PostModel>> = _posts.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val forumService = ForumService()

    private val categoryCache = mutableMapOf<String, String>()

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = forumService.getPostsFromFirestore()
                result.onSuccess { postList ->
                    _posts.value = postList
                }.onFailure { exception ->
                    _error.value = "Error fetching posts: ${exception.message}"
                }
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
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
}