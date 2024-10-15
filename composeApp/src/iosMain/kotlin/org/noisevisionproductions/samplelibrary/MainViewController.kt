package org.noisevisionproductions.samplelibrary

import androidx.compose.ui.window.ComposeUIViewController
import org.noisevisionproductions.samplelibrary.composeUI.screens.App

fun MainViewController() = ComposeUIViewController { App(dbHelper) }