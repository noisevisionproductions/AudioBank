package org.noisevisionproductions.samplelibrary.interfaces


actual object AppContext {
    actual fun get(): Any {
        // Return nothing for iOS, or some platform-specific resource if necessary
        return Unit
    }
}