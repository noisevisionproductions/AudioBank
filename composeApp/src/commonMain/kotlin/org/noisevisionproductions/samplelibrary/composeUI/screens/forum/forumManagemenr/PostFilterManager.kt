package org.noisevisionproductions.samplelibrary.composeUI.screens.forum.forumManagemenr

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostFilterManager {
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    private val _selectedSortingOption = MutableStateFlow("Najnowsze")
    val selectedSortingOption: StateFlow<String> = _selectedSortingOption.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(categoryName: String?, categoryId: String?) {
        _selectedCategory.value = categoryName
        _selectedCategoryId.value = categoryId
    }

    fun updateSortingOption(option: String) {
        _selectedSortingOption.value = option
    }
}