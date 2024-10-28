package org.noisevisionproductions.samplelibrary.errors

data class UserErrorInfo(
    val message: String,
    val actionType: UserErrorAction,
    val errorId: String,
    val retryAction: (() -> Unit)? = null
)