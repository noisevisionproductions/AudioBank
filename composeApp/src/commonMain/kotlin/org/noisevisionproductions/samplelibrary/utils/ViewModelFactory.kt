package org.noisevisionproductions.samplelibrary.utils

import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeService
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow.PostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings.AccountViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.MusicPlayerViewModel
import org.noisevisionproductions.samplelibrary.database.CommentRepository
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.errors.ErrorHandler
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepository
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl

expect class ViewModelFactory(
    authService: AuthService,
    forumRepository: ForumRepository,
    likeManager: LikeManager,
    commentRepository: CommentRepository,
    userRepository: UserRepository,
    likeService: LikeService,
    firebaseStorageRepository: FirebaseStorageRepository,
    sharedErrorViewModel: SharedErrorViewModel,
    errorHandler: ErrorHandler,
    musicPlayerService: MusicPlayerService,
    avatarPickerRepositoryImpl: AvatarPickerRepositoryImpl
) {
    fun createPostViewModel(): PostViewModel
    fun createCommentViewModel(): CommentViewModel
    fun userViewModel(): UserViewModel
    fun uploadSoundViewModel(): UploadSoundViewModel
    fun musicPlayerViewModel(): MusicPlayerViewModel
    fun accountViewModel(): AccountViewModel
}
