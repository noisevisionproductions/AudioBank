package org.noisevisionproductions.samplelibrary.utils

import kotlinx.serialization.Serializable

@Serializable
data class TagData(val tags: List<String>)