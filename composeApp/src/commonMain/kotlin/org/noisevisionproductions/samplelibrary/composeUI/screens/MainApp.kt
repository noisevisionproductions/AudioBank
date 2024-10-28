package org.noisevisionproductions.samplelibrary.composeUI.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.noisevisionproductions.samplelibrary.composeUI.MyCustomTheme
import org.noisevisionproductions.samplelibrary.composeUI.components.Colors
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.DynamicListViewModel
import org.noisevisionproductions.samplelibrary.database.FirebaseStorageRepository
import org.noisevisionproductions.samplelibrary.errors.handleGivenErrors.SharedErrorViewModel
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

val colors = Colors()

@Composable
@Preview
fun App(filePicker: FilePicker) {
    MyCustomTheme {
        val sharedErrorViewModel = SharedErrorViewModel()
        val firebaseStorageRepository = FirebaseStorageRepository()
        val viewModel = remember {
            DynamicListViewModel(
                firebaseStorageRepository = firebaseStorageRepository
            )
        }
        BarWithFragmentsList(
            dynamicListViewModel = viewModel,
            filePicker = filePicker,
            sharedErrorViewModel = sharedErrorViewModel
        )
    }
}
