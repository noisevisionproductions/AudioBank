package org.noisevisionproductions.samplelibrary.composeUI.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.components.BackgroundWithCirclesWithAvatar
import org.noisevisionproductions.samplelibrary.composeUI.components.DefaultAvatar
import org.noisevisionproductions.samplelibrary.composeUI.components.bottomShadow
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.AccountFragmentNavigationHost
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.AvatarManager.UserAvatar
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.ForumNavigationHost
import org.noisevisionproductions.samplelibrary.composeUI.screens.forum.likes.LikeManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu.DynamicListViewModel
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu.SoundNavigationHost
import org.noisevisionproductions.samplelibrary.database.CommentRepository
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.ForumRepository
import org.noisevisionproductions.samplelibrary.database.LikeRepository
import org.noisevisionproductions.samplelibrary.database.PostsRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.ErrorHandler
import org.noisevisionproductions.samplelibrary.errors.ErrorLogger
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.ErrorDialogManager
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.interfaces.MusicPlayerService
import org.noisevisionproductions.samplelibrary.utils.ViewModelFactory
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

@Composable
fun BarWithFragmentsList(
    dynamicListViewModel: DynamicListViewModel,
    filePicker: FilePicker,
    sharedErrorViewModel: SharedErrorViewModel
) {
    val errorDialogManager = remember { ErrorDialogManager(sharedErrorViewModel) }
    val errorLogger = ErrorLogger()
    val errorHandler = remember { ErrorHandler(errorLogger = errorLogger) }
    errorDialogManager.ShowErrorDialog()

    val musicPlayerService = remember { MusicPlayerService() }
    val avatarPickerRepositoryImpl = remember { AvatarPickerRepositoryImpl(filePicker) }
    val likeManager = remember { LikeManager() }
    val userRepository = remember { UserRepository() }
    val authService = remember { AuthService() }
    val likeRepository = remember { LikeRepository() }
    val forumRepository = remember { ForumRepository() }
    val commentRepository = remember { CommentRepository() }
    val postsRepository = remember { PostsRepository() }
    val firebaseStorageRepository = remember { FirebaseStorageRepository() }

    val viewModelFactory = remember {
        ViewModelFactory(
            authService,
            forumRepository,
            likeManager,
            commentRepository,
            userRepository,
            likeRepository,
            firebaseStorageRepository,
            sharedErrorViewModel,
            errorHandler,
            musicPlayerService,
            avatarPickerRepositoryImpl,
            postsRepository
        )
    }

    val navigationViewModel = remember { viewModelFactory.navigationViewModel() }
    val uploadSoundViewModel = remember { viewModelFactory.uploadSoundViewModel() }
    val userViewModel = remember { viewModelFactory.userViewModel() }
    val postViewModel = remember { viewModelFactory.createPostViewModel() }
    val commentViewModel = remember { viewModelFactory.createCommentViewModel() }
    val musicPlayerViewModel = remember { viewModelFactory.musicPlayerViewModel() }
    val accountViewModel = remember { viewModelFactory.accountViewModel() }
    val userSoundsViewModel = remember { viewModelFactory.userSoundsViewModel() }

    val currentTab by navigationViewModel.selectedTab.collectAsState()
    var currentScreen by remember { mutableStateOf(FragmentsTabs.Tab1) }

    var avatarPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        userRepository.getCurrentUserAvatarPath().fold(
            onSuccess = { path ->
                avatarPath = path
            },
            onFailure = { error ->
                println("Error fetching avatar path: ${error.message}")
                avatarPath = null
            }
        )
    }

    LaunchedEffect(currentTab) {
        currentScreen = currentTab
    }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
            ) {
                BackgroundWithCirclesWithAvatar(
                    backgroundColor = colors.primaryBackgroundColor,
                    modifier = Modifier.matchParentSize()
                )
                if (avatarPath != null) {
                    UserAvatar(
                        avatarUrl = avatarPath!!,
                        size = 90.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    )
                } else {
                    DefaultAvatar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)

                    )
                }
            }

            val tabTitles = listOf("Dźwięki", "Forum", "Pomoc")

            // Pasek wyboru fragmentów
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(colors.barColor)
                    .bottomShadow()
                    .zIndex(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in tabTitles.indices) {
                    TabItem(
                        text = tabTitles[i],
                        isSelected = when (i) {
                            0 -> currentScreen == FragmentsTabs.Tab1
                            1 -> currentScreen == FragmentsTabs.Tab2
                            2 -> currentScreen == FragmentsTabs.Tab3
                            else -> false
                        },
                        onClick = {
                            val newTab = when (i) {
                                0 -> FragmentsTabs.Tab1
                                1 -> FragmentsTabs.Tab2
                                2 -> FragmentsTabs.Tab3
                                else -> FragmentsTabs.Tab1
                            }
                            navigationViewModel.updateSelectedTab(newTab)
                        },
                        isFirst = i == 0,
                        isLast = i == tabTitles.size - 1,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (currentScreen) {
                FragmentsTabs.Tab1 -> {
                    dynamicListViewModel.updateDirectoryPath("samples")
                    SoundNavigationHost(
                        dynamicListViewModel = dynamicListViewModel,
                        filePicker = filePicker,
                        uploadSoundViewModel = uploadSoundViewModel,
                        musicPlayerViewModel = musicPlayerViewModel
                    )
                }

                FragmentsTabs.Tab2 -> ForumNavigationHost(
                    postViewModel = postViewModel,
                    userViewModel = userViewModel,
                    commentViewModel = commentViewModel,
                    userRepository = userRepository,
                    forumRepository = forumRepository,
                    likeManager = likeManager,
                    navigationViewModel = navigationViewModel,
                    postsRepository = postsRepository
                )

                else -> AccountFragmentNavigationHost(
                    accountViewModel = accountViewModel,
                    navigationViewModel = navigationViewModel,
                    userSoundsViewModel = userSoundsViewModel,
                    uploadSoundViewModel = uploadSoundViewModel
                )
            }
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(PaddingValues(top = 4.dp))
            .fillMaxHeight()
            .padding(
                start = if (isFirst) 8.dp else 0.dp,
                end = if (isLast) 8.dp else 0.dp
            )
            .clip(
                if (isSelected)
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                else RoundedCornerShape(0.dp)
            )
            .background(if (isSelected) colors.backgroundWhiteColor else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) colors.textColorChosenBar else colors.textColorUnChosenBar,
            modifier = Modifier.padding(8.dp)
        )
    }
}