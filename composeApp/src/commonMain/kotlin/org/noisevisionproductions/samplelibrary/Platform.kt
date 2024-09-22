package org.noisevisionproductions.samplelibrary

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform