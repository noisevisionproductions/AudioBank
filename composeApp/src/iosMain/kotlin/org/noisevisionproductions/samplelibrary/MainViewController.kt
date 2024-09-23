package org.noisevisionproductions.samplelibrary

import androidx.compose.ui.window.ComposeUIViewController
import org.noisevisionproductions.samplelibrary.composeUI.App

fun MainViewController() = ComposeUIViewController { App(dbHelper) }