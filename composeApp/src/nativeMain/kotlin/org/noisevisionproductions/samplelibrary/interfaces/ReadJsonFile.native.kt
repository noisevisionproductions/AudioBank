package org.noisevisionproductions.samplelibrary.interfaces


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile

actual fun getTagsFromJsonFile(): List<String> {
    // Get the path to the 'tags.json' file in the app bundle
    val path = NSBundle.mainBundle.pathForResource("tags", "json") ?: throw Exception("tags.json not found")

    // Read the file contents
    val jsonString = NSString.stringWithContentsOfFile(path, encoding = platform.Foundation.NSUTF8StringEncoding, error = null)
        ?: throw Exception("Unable to read tags.json")

    // Parse JSON into TagData
    val tagData = Json.decodeFromString<TagData>(jsonString)

    // Return the list of tags
    return tagData.tags
}
