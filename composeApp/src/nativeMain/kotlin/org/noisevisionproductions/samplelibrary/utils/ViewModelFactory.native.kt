package org.noisevisionproductions.samplelibrary.utils
// In iosMain

import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.auth.UserViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.userProfile.AccountViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.userSounds.UserSoundsViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement.SharedSoundEventsManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.comments.CommentViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.postWindow.PostViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu.DynamicListViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.mediaPlayer.MusicPlayerViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.database.CommentRepository
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.LikeRepository
import org.noisevisionproductions.samplelibrary.database.PostsRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.ErrorHandler
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.fragmentNavigation.NavigationViewModel

actual class ViewModelFactory actual constructor(
    private val authService: AuthService,
    private val forumRepository: ForumRepository,
    private val likeManager: LikeManager,
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
    private val firebaseStorageRepository: FirebaseStorageRepository,
    private val sharedErrorViewModel: SharedErrorViewModel,
    private val errorHandler: ErrorHandler,
    private val musicPlayerService: MusicPlayerService,
    private val avatarPickerRepositoryImpl: AvatarPickerRepositoryImpl,
    private val postsRepository: PostsRepository
) {
    private val sharedSoundEventsManager = SharedSoundEventsManager

    actual fun createPostViewModel(): PostViewModel {
        return PostViewModel(
            forumRepository = forumRepository,
            likeManager = likeManager,
            likeRepository = likeRepository,
            postsRepository = postsRepository
        )
    }

    actual fun createCommentViewModel(): CommentViewModel {
        return CommentViewModel(
            commentRepository = commentRepository,
            authService = authService,
            likeManager = likeManager,
            likeRepository = likeRepository,
            userRepository = userRepository,
            sharedErrorViewModel = sharedErrorViewModel,
            errorHandler = errorHandler,
        )
    }

    actual fun userViewModel(): UserViewModel {
        return UserViewModel(userRepository = userRepository)
    }

    actual fun uploadSoundViewModel(): UploadSoundViewModel {
        return UploadSoundViewModel(
            storageService = firebaseStorageRepository,
            userRepository = userRepository,
            sharedSoundViewModel = sharedSoundEventsManager
        )
    }

    actual fun musicPlayerViewModel(): MusicPlayerViewModel {
        return MusicPlayerViewModel(
            musicPlayerService = musicPlayerService
        )
    }

    actual fun accountViewModel(): AccountViewModel {
        return AccountViewModel(
            userRepository = userRepository,
            firebaseStorageRepository = firebaseStorageRepository,
            sharedErrorViewModel = sharedErrorViewModel,
            avatarPickerRepositoryImpl = avatarPickerRepositoryImpl,
            likeManager = likeManager,
            postsRepository = postsRepository
        )
    }

    actual fun navigationViewModel(): NavigationViewModel {
        return NavigationViewModel()
    }

    actual fun userSoundsViewModel(): UserSoundsViewModel {
        return UserSoundsViewModel(
            storageRepository = firebaseStorageRepository,
            userRepository = userRepository,
            sharedSoundEventsManager = sharedSoundEventsManager
        )
    }

    actual fun dynamicListViewModel(): DynamicListViewModel {
        return DynamicListViewModel(
            firebaseStorageRepository = firebaseStorageRepository,
            likeRepository = likeRepository,
            userRepository = userRepository,
            sharedSoundEventsManager = sharedSoundEventsManager
        )
    }
}
