package org.noisevisionproductions.samplelibrary.utils

fun decodeUrl(url: String): String {
    return url
        .replace("%20", " ")
        .replace("%2F", "/")
        .replace("%26", "&")
        .replace("%23", "#")
        .substringAfterLast("/")
        .substringBefore("?")
}

fun decodeFileName(fileName: String): String {
    return fileName
        .replace("%20", " ")
        .replace("%26", "&")
        .replace("%2F", "/")
        .replace("%23", "#")
}

