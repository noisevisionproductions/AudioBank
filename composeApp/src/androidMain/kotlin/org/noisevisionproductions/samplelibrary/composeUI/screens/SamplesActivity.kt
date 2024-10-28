package org.noisevisionproductions.samplelibrary.composeUI.screens

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

class SamplesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filePicker = FilePicker(activity = this)
        setContent {
            App(filePicker = filePicker)
        }
    }
}