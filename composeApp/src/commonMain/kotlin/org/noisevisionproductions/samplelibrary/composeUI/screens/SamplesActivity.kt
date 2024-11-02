package org.noisevisionproductions.samplelibrary.composeUI.screens

import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

expect class SamplesActivity {
    fun initializeFilePicker(): FilePicker
}