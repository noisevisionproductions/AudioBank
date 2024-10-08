package org.noisevisionproductions.samplelibrary.composeUI


fun decodeUrl(url: String): String {
    return url
        .replace("%20", " ")
        .replace("%2F", "/")
        .replace("%26", "&")
        .substringAfterLast("/")
        .substringBefore("?")
}

