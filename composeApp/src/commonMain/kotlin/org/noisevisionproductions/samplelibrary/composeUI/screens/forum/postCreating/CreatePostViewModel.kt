package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postCreating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.PostsRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

class CreatePostViewModel(
    private val forumRepository: ForumRepository,
    private val postsRepository: PostsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content = _content.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryModel?>(null)
    private val selectedCategory = _selectedCategory.asStateFlow()

    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous = _isAnonymous.asStateFlow()

    private val _isAgreementAccepted = MutableStateFlow(false)
    val isAgreementAccepted = _isAgreementAccepted.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryModel>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isTitleError = MutableStateFlow(false)
    val isTitleError = _isTitleError.asStateFlow()

    private val _isContentError = MutableStateFlow(false)
    val isContentError = _isContentError.asStateFlow()

    val isFormValid = combine(
        title,
        content,
        selectedCategory,
        isAgreementAccepted
    ) { title, content, category, agreement ->
        title.isNotEmpty() && content.isNotEmpty() && category != null && agreement
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        false
    )

    init {
        loadCategories()
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
        _isTitleError.value = newTitle.isEmpty()
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
        _isContentError.value = newContent.isEmpty()
    }

    fun updateSelectedCategory(category: CategoryModel) {
        _selectedCategory.value = category
    }

    fun updateIsAnonymous(isAnonymous: Boolean) {
        _isAnonymous.value = isAnonymous
    }

    fun updateAgreementAccepted(isAccepted: Boolean) {
        _isAgreementAccepted.value = isAccepted
    }

    // TODO fix
    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = forumRepository.getCategories()

            result.fold(
                onSuccess = { loadedCategories ->
                    _categories.value = loadedCategories
                },
                onFailure = { exception ->
                    UserErrorInfo(
                        message = "Błąd podczas ładowania kategorii: ${exception.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "LOAD_CATEGORIES_ERROR",
                        retryAction = { loadCategories() }
                    )
                    println("Error loading categories: ${exception.message}")
                }
            )
            _isLoading.value = false
        }
    }

    fun createPost(username: String?, onPostCreated: () -> Unit) {
        if (!isFormValid.value) {
            _errorMessage.value = "Wypełnij wszystkie pola"
            return
        }

        viewModelScope.launch {
            userRepository.getCurrentUserId().fold(
                onSuccess = { userId ->
                    val postUsername = if (isAnonymous.value) "Anonim" else username ?: "Nieznany"
                    selectedCategory.value?.let { category ->
                        postsRepository.createPost(
                            title = title.value,
                            content = content.value,
                            username = postUsername,
                            categoryId = category.id,
                            userId = userId
                        ).fold(
                            onSuccess = { success ->
                                if (success) {
                                    _errorMessage.value = null
                                    onPostCreated()
                                } else {
                                    _errorMessage.value = "Błąd podczas tworzenia postu"
                                }
                            },
                            onFailure = { error ->
                                UserErrorInfo(
                                    message = "Błąd podczas tworzenia postu: ${error.message}",
                                    actionType = UserErrorAction.RETRY,
                                    errorId = "CREATE_POST_ERROR",
                                    retryAction = { createPost(username, onPostCreated) }
                                )
                                println("Error creating post: ${error.message}")
                            }
                        )
                    } ?: run {
                        _errorMessage.value = "Nie wybrano kategorii"
                    }
                },
                onFailure = { error ->
                    UserErrorInfo(
                        message = "Błąd pobierania ID użytkownika: ${error.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "FETCH_USER_ID_ERROR",
                        retryAction = { createPost(username, onPostCreated) }
                    )
                    println("Error fetching user ID: ${error.message}")
                }
            )
        }
    }

}