package org.noisevisionproductions.samplelibrary.composeUI.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.noisevisionproductions.samplelibrary.composeUI.MyCustomTheme
import org.noisevisionproductions.samplelibrary.composeUI.components.Colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.dataStatesManagement.SharedSoundEventsManager
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.exploreMenu.DynamicListViewModel
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.database.LikeRepository
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

val colors = Colors()

@Composable
fun App(filePicker: FilePicker) {
    MyCustomTheme {
        val sharedErrorViewModel = SharedErrorViewModel()

        val viewModel = remember {
            DynamicListViewModel(
                firebaseStorageRepository = FirebaseStorageRepository(),
                likeRepository = LikeRepository(),
                userRepository = UserRepository(),
                sharedSoundEventsManager = SharedSoundEventsManager
            )
        }
        BarWithFragmentsList(
            dynamicListViewModel = viewModel,
            filePicker = filePicker,
            sharedErrorViewModel = sharedErrorViewModel
        )
    }
}
