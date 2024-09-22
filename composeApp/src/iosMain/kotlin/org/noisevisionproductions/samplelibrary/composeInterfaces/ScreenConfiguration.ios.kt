package org.noisevisionproductions.samplelibrary.composeInterfaces

// iOS-specific implementation in iosMain
import platform.UIKit.UIScreen

actual fun getScreenConfiguration(): ScreenConfiguration {
    val screen = UIScreen.mainScreen
    return object : ScreenConfiguration {
        override val screenWidthDp: Float = screen.bounds.size.width.toFloat()
        override val screenHeightDp: Float = screen.bounds.size.height.toFloat()
    }
}
