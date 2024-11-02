package org.noisevisionproductions.samplelibrary.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.errors.validation.LoginFormState
import org.noisevisionproductions.samplelibrary.errors.validation.ValidationResult

class LoginViewModel(private val authService: AuthService) :
    BaseAuthViewModel() {

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    fun updateEmail(email: String) {
        _formState.update {
            it.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
    }

    fun updatePassword(password: String) {
        _formState.update {
            it.copy(
                password = password,
                passwordError = validatePassword(password)
            )
        }
    }

    fun performLogin(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authService.signIn(email, password)

            if (result.isSuccess) {
                onSuccess(result.getOrThrow())
            } else {
                handleFirebaseError(result.exceptionOrNull()?.message, onFailure)
            }
        }
    }

    private fun handleFirebaseError(errorMessage: String?, onFailure: (String) -> Unit) {
        val errorMap = mapOf(
            "The email address is already in use by another account." to {
                updateFormState {
                    copy(
                        emailError = ValidationResult.Invalid("Ten adres e-mail jest już używany")
                    )
                }
            },
            "The email address is badly formatted." to {
                updateFormState {
                    copy(
                        emailError = ValidationResult.Invalid(
                            "Niepoprawny format adresu e-mail"
                        )
                    )
                }
            },
            "The password is too weak." to {
                updateFormState {
                    copy(
                        passwordError = ValidationResult.Invalid(
                            "Hasło jest zbyt słabe"
                        )
                    )
                }
            },
            "There is no user record corresponding to this identifier." to {
                updateFormState {
                    copy(
                        emailError = ValidationResult.Invalid("Nie znaleziono konta o podanym adresie e-mail")
                    )
                }
            },
            "There is no user record corresponding to this identifier." to {
                updateFormState {
                    copy(
                        emailError = ValidationResult.Invalid("Nie znaleziono konta o podanym adresie e-mail")
                    )
                }
            },
            "The supplied auth credential is incorrect, malformed or has expired." to {
                updateFormState {
                    copy(
                        passwordError = ValidationResult.Invalid(
                            "Podane dane uwierzytelniające są nieprawidłowe"
                        )
                    )
                }
            },
            "The password is invalid or the user does not have a password." to {
                updateFormState {
                    copy(
                        passwordError = ValidationResult.Invalid("Niepoprawne hasło")
                    )
                }
            },
            "Given String is empty or null" to { handleEmptyFieldError() },
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." to {
                onFailure("Problem z połączeniem sieciowym. Spróbuj ponownie później.")
            },
            "Too many unsuccessful login attempts. Please try again later." to {
                onFailure("Zbyt wiele nieudanych prób logowania. Spróbuj ponownie później.")
            }
        )

        errorMap[errorMessage]?.invoke() ?: onFailure(errorMessage ?: "Nieznany błąd")
    }

    private fun updateFormState(update: LoginFormState.() -> LoginFormState) {
        _formState.update { it.update() }
    }

    private fun handleEmptyFieldError() {
        when {
            _formState.value.email.isBlank() -> updateFormState {
                copy(
                    emailError = ValidationResult.Invalid(
                        "Adres e-mail nie może być pusty"
                    )
                )
            }

            _formState.value.password.isBlank() -> updateFormState {
                copy(
                    passwordError = ValidationResult.Invalid(
                        "Hasło nie może być puste"
                    )
                )
            }
        }
    }
}