package org.noisevisionproductions.samplelibrary.composeUI.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

actual class SamplesActivity : AppCompatActivity() {

    actual fun initializeFilePicker(): FilePicker = FilePicker(activity = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filePicker = initializeFilePicker()
        setContent {
            App(filePicker = filePicker)
        }
    }
}