package org.noisevisionproductions.samplelibrary.utils.dataClasses

data class AudioMetadata(
    val id: String? = null,
    val fileName: String,
    val fileExtension: String? = null,
    val duration: String? = null,
    val url: String? = null,
    val timestamp: String? = null,
    val contentType: String? = null,
    val bpm: String? = null,
    val tone: String? = null,
    val tags: List<String> = emptyList()
)