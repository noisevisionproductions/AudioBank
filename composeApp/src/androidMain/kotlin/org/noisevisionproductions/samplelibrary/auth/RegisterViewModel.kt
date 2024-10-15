package org.noisevisionproductions.samplelibrary.auth

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.noisevisionproductions.samplelibrary.auth.validation.RegisterFormState
import org.noisevisionproductions.samplelibrary.auth.validation.ValidationResult

actual class RegisterViewModel actual constructor(private val authService: AuthService) :
    BaseAuthViewModel() {
    private val _formState = MutableStateFlow(RegisterFormState())
    actual val formState: StateFlow<RegisterFormState> = _formState.asStateFlow()

    actual fun updateNickname(nickname: String) {
        _formState.update {
            it.copy(
                nickname = nickname,
                nicknameError = validateNickname(nickname)
            )
        }
    }

    actual fun updateEmail(email: String) {
        _formState.update {
            it.copy(
                email = email,
                emailError = validateEmail(email)
            )
        }
    }

    actual fun updatePassword(password: String) {
        _formState.update {
            it.copy(
                password = password,
                passwordError = validatePassword(password)
            )
        }
    }

    actual fun updateConfirmPassword(confirmPassword: String) {
        _formState.update {
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = validateConfirmPassword(confirmPassword)
            )
        }
    }

    private fun validateNickname(nickname: String): ValidationResult {
        return when {
            nickname.isBlank() -> ValidationResult.Invalid("Nazwa użytkownika nie może być pusta")
            !nickname.matches("^[a-zA-Z0-9_]+$".toRegex()) -> ValidationResult.Invalid("Nazwa użytkownika może zawierać tylko litery, cyfry i podkreślenia")
            nickname.length < 3 || nickname.length > 20 -> ValidationResult.Invalid("Nazwa użytkownika musi mieć od 3 do 20 znaków")
            else -> ValidationResult.Valid
        }
    }

    override fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid("Hasło nie może być puste")
            password.length < 6 || password.length > 30 -> ValidationResult.Invalid("Hasło musi mieć od 6 do 30 znaków")
            !password.matches(".*[A-Za-z].*".toRegex()) ->
                ValidationResult.Invalid("Hasło musi zawierać przynajmniej jedną literę")

            !password.matches(".*\\d.*".toRegex()) ->
                ValidationResult.Invalid("Hasło musi zawierać co najmniej jedną cyfrę")

            else -> ValidationResult.Valid
        }
    }

    private fun validateConfirmPassword(confirmPassword: String): ValidationResult {
        val password = _formState.value.password
        return when {
            confirmPassword.isBlank() -> ValidationResult.Invalid("Potwierdzenie hasła nie może być puste")
            confirmPassword != password -> ValidationResult.Invalid("Hasła nie są zgodne")
            else -> ValidationResult.Valid
        }
    }

    actual fun performRegister(
        nickname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authService.signUp(nickname, email, password)

            if (result.isSuccess) {
                val userId = result.getOrThrow()
                Log.d("AuthService", "Rejestracja zakończona sukcesem, UID: $userId")
                onSuccess(userId)
            } else {
                val exception = result.exceptionOrNull()
                Log.e("AuthService", "Błąd podczas rejestracji: ${exception?.message}")
                handleFirebaseError(exception?.message, onFailure)
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
            "The password is invalid or the user does not have a password." to {
                updateFormState {
                    copy(
                        passwordError = ValidationResult.Invalid("Niepoprawne hasło")
                    )
                }
            },
            "Given String is empty or null" to { handleEmptyFieldError() },
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." to {
                onFailure(
                    "Problem z połączeniem sieciowym. Spróbuj ponownie później."
                )
            },
            "Too many unsuccessful login attempts. Please try again later." to { onFailure("Zbyt wiele nieudanych prób logowania. Spróbuj ponownie później.") }
        )

        errorMap[errorMessage]?.invoke() ?: onFailure(errorMessage ?: "Nieznany błąd")
    }

    private fun updateFormState(update: RegisterFormState.() -> RegisterFormState) {
        _formState.update { it.update() }
    }

    private fun handleEmptyFieldError() {
        when {
            _formState.value.nickname.isBlank() -> updateFormState {
                copy(
                    nicknameError = ValidationResult.Invalid(
                        "Nazwa użytkownika nie może być pusta"
                    )
                )
            }

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