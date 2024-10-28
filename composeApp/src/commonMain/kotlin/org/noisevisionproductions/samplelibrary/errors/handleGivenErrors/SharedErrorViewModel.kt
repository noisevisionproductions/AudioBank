package org.noisevisionproductions.samplelibrary.errors.handleGivenErrors

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.noisevisionproductions.samplelibrary.errors.AppError
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo

class SharedErrorViewModel : ViewModel() {
    private val _currentError = MutableStateFlow<UserErrorInfo?>(null)
    val currentError: StateFlow<UserErrorInfo?> = _currentError

    fun showError(errorInfo: UserErrorInfo) {
        _currentError.value = errorInfo
    }

    fun hideError() {
        _currentError.value = null
    }
}
