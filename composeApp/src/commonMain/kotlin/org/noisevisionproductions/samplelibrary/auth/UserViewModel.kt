package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.database.UserRepository
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo

class UserViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var _username = MutableStateFlow<String?>(null)
    val username: StateFlow<String?> get() = _username.asStateFlow()

    private var _label = MutableStateFlow<String?>(null)
    val label: StateFlow<String?> = _label.asStateFlow()

    private val _userLabels = MutableStateFlow<Map<String, String>>(emptyMap())
    val userLabels: StateFlow<Map<String, String>> = _userLabels.asStateFlow()

    private var _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            userRepository.getCurrentUser().fold(
                onSuccess = { userModel ->
                    if (userModel != null) {
                        _username.value = userModel.username
                        _label.value = userModel.label
                    } else {
                        _error.value = "Error fetching user data: User not found"
                    }
                },
                onFailure = { error ->
                    UserErrorInfo(
                        message = "Błąd podczas pobierania danych użytkownika: ${error.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "FETCH_USER_DATA_ERROR",
                        retryAction = { fetchUserData() }
                    )
                    println("Error fetching user data: ${error.message}")
                }
            )
            _isLoading.value = false
        }
    }

    fun fetchLabelForUser(userId: String) {
        viewModelScope.launch {
            userRepository.getUserLabelById(userId).fold(
                onSuccess = { label ->
                    _userLabels.update { currentMap ->
                        currentMap.toMutableMap().apply {
                            put(userId, label ?: "No label")
                        }
                    }
                },
                onFailure = { e ->
                    UserErrorInfo(
                        message = "Błąd podczas pobierania etykiety użytkownika: ${e.message}",
                        actionType = UserErrorAction.RETRY,
                        errorId = "FETCH_USER_LABEL_ERROR",
                        retryAction = { fetchLabelForUser(userId) }
                    )
                    println("Error fetching label for user ID $userId: ${e.message}")
                }
            )
        }
    }
}