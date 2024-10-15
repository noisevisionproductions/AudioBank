package org.noisevisionproductions.samplelibrary.utils

import android.app.Activity
import android.content.Context
import org.noisevisionproductions.samplelibrary.interfaces.AppActivity

class AndroidPlatformContext(private val context: Context) : AppActivity {
    override fun getActivity(): Any? {
        return when (context) {
            is Activity -> context
            else -> null
        }
    }
}