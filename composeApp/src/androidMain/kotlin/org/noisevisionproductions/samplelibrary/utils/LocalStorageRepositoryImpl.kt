package org.noisevisionproductions.samplelibrary.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import java.io.File

actual class LocalStorageRepositoryImpl actual constructor() {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val AVATAR_URL_KEY = stringPreferencesKey("avatar_url")
    }

    private val context = AppContext.get() as Context
    private val cacheDir = context.cacheDir.resolve("avatars").also { it.mkdirs() }

    actual suspend fun saveAvatarUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[AVATAR_URL_KEY] = url
        }
    }

    actual suspend fun getAvatarUrl(): String? {
        return context.dataStore.data
            .map { preferences ->
                preferences[AVATAR_URL_KEY]
            }
            .first()
    }

    actual suspend fun saveAvatarImage(filePath: String) {
        withContext(Dispatchers.IO) {
            val avatarFile = cacheDir.resolve("avatar.jpg")
            val imageBytes = File(filePath).readBytes()
            avatarFile.writeBytes(imageBytes)
        }
    }

    actual suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
        }
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}