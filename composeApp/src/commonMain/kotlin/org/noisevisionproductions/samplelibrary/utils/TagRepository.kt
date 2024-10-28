package org.noisevisionproductions.samplelibrary.utils

expect object TagRepository {
    fun getTagsFromJsonFile(): List<String>
}