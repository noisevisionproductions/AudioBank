package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postCreating

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.utils.models.CategoryModel

class CreatePostViewModel(
    private val forumRepository: ForumRepository,
    private val authService: AuthService
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

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            forumRepository.getCategories { loadedCategories ->
                _categories.value = loadedCategories
                _isLoading.value = false
            }
        }
    }

    fun createPost(username: String?, onPostCreated: () -> Unit) {
        if (!isFormValid.value) {
            _errorMessage.value = "Wypełnij wszystkie pola"
            return
        }

        viewModelScope.launch {
            val userId = authService.getCurrentUserId()

            if (userId == null) {
                _errorMessage.value = "Musisz być zalogowany, aby utworzyć post"
                return@launch
            }

            val postUsername = if (isAnonymous.value) {
                "Anonim"
            } else {
                username ?: run {
                    _errorMessage.value = "Nie można pobrać nazwy użytkownika"
                    return@launch
                }
            }

            selectedCategory.value?.let { category ->
                forumRepository.createPost(
                    title = title.value,
                    content = content.value,
                    username = postUsername,
                    categoryId = category.id,
                    userId = userId
                ) { success ->
                    if (success) {
                        _errorMessage.value = null
                        onPostCreated()
                    } else {
                        _errorMessage.value = "Błąd podczas tworzenia postu"

                    }
                }
            }
        }
    }
}