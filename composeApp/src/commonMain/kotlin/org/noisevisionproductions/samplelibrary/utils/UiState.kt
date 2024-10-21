package org.noisevisionproductions.samplelibrary.utils

import org.noisevisionproductions.samplelibrary.utils.models.PostWithCategory

sealed class UiState {
    data object Loading : UiState()
    data class Success(val posts: List<PostWithCategory>) : UiState()
    data class Error(val message: String) : UiState()
}