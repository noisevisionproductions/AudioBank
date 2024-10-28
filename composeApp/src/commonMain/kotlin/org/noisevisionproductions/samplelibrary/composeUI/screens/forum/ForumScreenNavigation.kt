package org.noisevisionproductions.samplelibrary.composeUI.screens.forum

sealed class ForumScreenNavigation {
    data object PostList : ForumScreenNavigation()
    data object CreatePost : ForumScreenNavigation()
    data class PostDetail(val postId: String) : ForumScreenNavigation()
}