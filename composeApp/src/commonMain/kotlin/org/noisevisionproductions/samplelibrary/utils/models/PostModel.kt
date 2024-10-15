package org.noisevisionproductions.samplelibrary.utils.models

import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp

data class PostModel(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val timestamp: String = getCurrentTimestamp(),
    val categoryId: String = "",
    val title: String = "",
    val content: String = "",
    val replies: List<String> = listOf()
)