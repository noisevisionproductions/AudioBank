package org.noisevisionproductions.samplelibrary.utils

import androidx.lifecycle.ViewModelProvider
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.database.ForumRepository

actual class ViewModelFactory actual constructor(
    authService: AuthService,
    forumRepository: ForumRepository,
    commentViewModel: CommentViewModel,
    commentRepository: LikeManager
) : ViewModelProvider.Factory