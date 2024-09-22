package org.noisevisionproductions.samplelibrary.composeInterfaces

import android.content.res.Resources

actual fun getScreenConfiguration(): ScreenConfiguration {
    val metrics = Resources.getSystem().displayMetrics
    return object : ScreenConfiguration {
        override val screenWidthDp: Float = metrics.widthPixels / metrics.density
        override val screenHeightDP: Float = metrics.heightPixels / metrics.density
    }
}