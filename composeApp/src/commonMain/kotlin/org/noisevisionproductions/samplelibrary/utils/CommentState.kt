package org.noisevisionproductions.samplelibrary.utils

import org.noisevisionproductions.samplelibrary.utils.models.CommentModel

data class CommentState(
    val comments: List<CommentModel> = emptyList(),
    val isLoading: Boolean = false,
    val totalCount: Int = 0
)