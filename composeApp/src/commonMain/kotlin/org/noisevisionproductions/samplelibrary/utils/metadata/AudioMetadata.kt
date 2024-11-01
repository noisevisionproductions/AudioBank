package org.noisevisionproductions.samplelibrary.utils.metadata

import org.noisevisionproductions.samplelibrary.interfaces.getCurrentTimestamp

data class AudioMetadata(
    val id: String? = "",
    val fileName: String,
    val fileExtension: String? = "",
    val duration: String? = "",
    val url: String? = "",
    val timestamp: String? = getCurrentTimestamp(),
    val contentType: String? = "",
    val bpm: String? = "",
    val tone: String? = "",
    val tags: List<String> = emptyList(),
    val isLiked: Boolean = false
)