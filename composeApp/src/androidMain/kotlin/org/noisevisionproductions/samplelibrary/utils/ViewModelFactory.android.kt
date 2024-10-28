package org.noisevisionproductions.samplelibrary.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.accountSettings.AccountViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeService
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow.PostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.MusicPlayerViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.database.CommentRepository
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.ErrorHandler
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl

actual class ViewModelFactory actual constructor(
    private val authService: AuthService,
    private val forumRepository: ForumRepository,
    private val likeManager: LikeManager,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val likeService: LikeService,
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val sharedErrorViewModel: SharedErrorViewModel,
    private val errorHandler: ErrorHandler,
    private val musicPlayerService: MusicPlayerService,
    private val avatarPickerRepositoryImpl: AvatarPickerRepositoryImpl
) : ViewModelProvider.Factory {

    // Tworzenie instancji PostViewModel
    actual fun createPostViewModel(): PostViewModel {
        return PostViewModel(forumRepository, likeManager, likeService)
    }

    // Tworzenie instancji CommentViewModel
    actual fun createCommentViewModel(): CommentViewModel {
        return CommentViewModel(
            commentRepository,
            authService,
            likeManager,
            likeService,
            userRepository,
            sharedErrorViewModel,
            errorHandler,
        )
    }

    actual fun userViewModel(): UserViewModel {
        return UserViewModel(userRepository)
    }

    actual fun uploadSoundViewModel(): UploadSoundViewModel {
        return UploadSoundViewModel(firebaseStorageRepository, userRepository)
    }

    actual fun musicPlayerViewModel(): MusicPlayerViewModel {
        return MusicPlayerViewModel(musicPlayerService)
    }

    actual fun accountViewModel(): AccountViewModel {
        return AccountViewModel(
            userRepository, firebaseStorageRepository, sharedErrorViewModel,
            avatarPickerRepositoryImpl
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PostViewModel::class.java) -> createPostViewModel() as T
            modelClass.isAssignableFrom(CommentViewModel::class.java) -> createCommentViewModel() as T
            modelClass.isAssignableFrom(UserViewModel::class.java) -> userViewModel() as T
            modelClass.isAssignableFrom(UploadSoundViewModel::class.java) -> userViewModel() as T
            modelClass.isAssignableFrom(MusicPlayerViewModel::class.java) -> musicPlayerViewModel() as T
            modelClass.isAssignableFrom(AccountViewModel::class.java) -> accountViewModel() as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
