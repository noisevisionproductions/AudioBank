package org.noisevisionproductions.samplelibrary.interfaces

import android.content.Context
import kotlinx.serialization.json.Json
import org.noisevisionproductions.samplelibrary.utils.TagData

actual fun getTagsFromJsonFile(): List<String> {
    val context = AppContext.get() as Context
    val jsonString  = context.assets.open("tags.json").bufferedReader().use { it.readText() }
    val tagData = Json.decodeFromString<TagData>(jsonString)
    return tagData.tags
}