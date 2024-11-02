package org.noisevisionproductions.samplelibrary.utils

import kotlinx.serialization.json.Json
import platform.Foundation.*
import org.noisevisionproductions.samplelibrary.utils.dataClasses.TagData

actual object TagRepository {
    private var cachedTags: List<String>? = null

    actual fun getTagsFromJsonFile(): List<String> {
        return cachedTags ?: run {
            val jsonString = loadJsonFromBundle("tags.json")
            if (jsonString != null) {
                val tagData = Json.decodeFromString<TagData>(jsonString)
                cachedTags = tagData.tags
                cachedTags!!
            } else {
                emptyList() // Return an empty list if the file is missing or unreadable
            }
        }
    }

    private fun loadJsonFromBundle(filename: String): String? {
        val path = NSBundle.mainBundle.pathForResource(filename, "json")
        return path?.let {
            NSString.stringWithContentsOfFile(it, NSUTF8StringEncoding, null) as String?
        }
    }
}
