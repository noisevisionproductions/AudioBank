package org.noisevisionproductions.samplelibrary.interfaces

import android.app.Application
import android.content.Context

actual object AppContext {
    private lateinit var application: Application

    fun setUp(context: Context) {
        application = context as Application
    }

    actual fun get(): Any {
        if (::application.isInitialized.not()) throw Exception("Application context isn't initialized")
        return application.applicationContext
    }
}