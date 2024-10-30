package org.noisevisionproductions.samplelibrary.utils.models

data class UserModel(
    val id: String = "",
    val username: String = "",
    val avatarUrl: String? = null,
    val registrationDate: String = "",
    val audioProgramsUsed: List<String> = emptyList(),
    val label: String = "",
    val likedPosts: List<String> = emptyList(),
    val likedComments: List<String> = emptyList(),
    val likedSounds: List<String> = emptyList(),
    val postIds: List<String> = emptyList()
)