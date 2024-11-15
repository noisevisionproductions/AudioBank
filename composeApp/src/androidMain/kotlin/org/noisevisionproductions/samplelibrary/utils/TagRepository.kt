package org.noisevisionproductions.samplelibrary.utils

import android.content.Context
import kotlinx.serialization.json.Json
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import org.noisevisionproductions.samplelibrary.utils.dataClasses.TagData

actual object TagRepository {
    private var cachedTags: List<String>? = null

    actual fun getTagsFromJsonFile(): List<String> {
        return cachedTags ?: run {
            val context = AppContext.get() as Context
            val jsonString = context.assets.open("tags.json").bufferedReader().use { it.readText() }
            val tagData = Json.decodeFromString<TagData>(jsonString)
            cachedTags = tagData.tags
            cachedTags!!
        }
    }
}