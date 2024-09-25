package org.noisevisionproductions.samplelibrary.composeUI

import androidx.compose.runtime.Composable

@Composable
fun displayFormattedDuration(durationMs: Int): String {
    val minutes = (durationMs / 1000) / 60
    var seconds = (durationMs / 1000) % 60
    if (durationMs in 1..999) {
        seconds = 1
    }
    // Return the formatted string "mm:ss"
    return "${if (minutes < 10) "0" else ""}$minutes:${if (seconds < 10) "0" else ""}$seconds"
}

fun decodeFirestoreUrl(url: String): String {
    return url
        .replace("%20", " ")
        .replace("%2F", "/")
        .replace("%26", "&")
        .substringAfterLast("/")
        .substringBefore("?")
}