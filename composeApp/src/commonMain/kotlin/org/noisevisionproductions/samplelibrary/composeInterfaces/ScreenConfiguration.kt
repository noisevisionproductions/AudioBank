package org.noisevisionproductions.samplelibrary.composeInterfaces

interface ScreenConfiguration {
    val screenWidthDp: Float
    val screenHeightDP: Float
}

expect fun getScreenConfiguration(): ScreenConfiguration