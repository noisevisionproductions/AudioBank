package org.noisevisionproductions.samplelibrary.composeUI.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.components.topShadow
import org.noisevisionproductions.samplelibrary.composeUI.screens.account.AccountFragmentNavigationHost
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
import org.noisevisionproductions.samplelibrary.utils.LocalStorageRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.ViewModelFactory
import org.noisevisionproductions.samplelibrary.utils.files.AvatarPickerRepositoryImpl
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker
import samplelibrary.composeapp.generated.resources.Res
import samplelibrary.composeapp.generated.resources.icon_forum_main
import samplelibrary.composeapp.generated.resources.icon_profile_main
import samplelibrary.composeapp.generated.resources.icon_sounds_main

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
    val localStorageRepositoryImpl = remember { LocalStorageRepositoryImpl() }

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
            postsRepository,
            localStorageRepositoryImpl
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

    LaunchedEffect(currentTab) {
        currentScreen = currentTab
    }

    Surface {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
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

                CustomBottomNavigation(
                    currentScreen = currentScreen,
                    onTabSelected = { navigationViewModel.updateSelectedTab(it) }
                )
            }
        }
    }
}

@Composable
private fun CustomBottomNavigation(
    currentScreen: FragmentsTabs,
    onTabSelected: (FragmentsTabs) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(colors.barColor)
            .topShadow(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            title = "Dźwięki",
            icon = painterResource(Res.drawable.icon_sounds_main),
            isSelected = currentScreen == FragmentsTabs.Tab1,
            onClick = { onTabSelected(FragmentsTabs.Tab1) }
        )
        BottomNavItem(
            title = "Forum",
            icon = painterResource(Res.drawable.icon_profile_main),
            isSelected = currentScreen == FragmentsTabs.Tab2,
            onClick = { onTabSelected(FragmentsTabs.Tab2) }
        )
        BottomNavItem(

            title = "Profil",
            icon = painterResource(Res.drawable.icon_forum_main),
            isSelected = currentScreen == FragmentsTabs.Tab3,
            onClick = { onTabSelected(FragmentsTabs.Tab3) }
        )
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (isSelected) colors.backgroundWhiteColor else colors.textColorUnChosenBar
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isSelected) colors.backgroundWhiteColor else colors.textColorUnChosenBar,
            fontSize = 12.sp
        )
    }
}