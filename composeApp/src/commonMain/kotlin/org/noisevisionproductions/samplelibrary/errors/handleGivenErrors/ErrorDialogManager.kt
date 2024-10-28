package org.noisevisionproductions.samplelibrary.errors.handleGivenErrors

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo

class ErrorDialogManager(
    private val sharedErrorViewModel: SharedErrorViewModel
) {

    @Composable
    fun ShowErrorDialog() {
        val errorInfo by sharedErrorViewModel.currentError.collectAsState()

        errorInfo?.let { userErrorInfo ->
            ErrorDialog(
                userErrorInfo = userErrorInfo,
                onDismiss = { sharedErrorViewModel.hideError() },
                onAction = { actionType ->
                    handleAction(actionType, userErrorInfo)
                    sharedErrorViewModel.hideError()
                }
            )
        }
    }

    private fun handleAction(actionType: UserErrorAction, userErrorInfo: UserErrorInfo) {
        when (actionType) {
            UserErrorAction.RETRY -> {
                userErrorInfo.retryAction?.invoke()
            }

            UserErrorAction.CONTACT_SUPPORT -> contactSupport()
            UserErrorAction.OK -> {} // Zamknięcie dialogu
        }
    }

    private fun navigateToCommentForm() {
        // Implementacja nawigacji do formularza komentarza
    }

    private fun contactSupport() {
        // Implementacja kontaktu z supportem
    }

    @Composable
    fun ErrorDialog(
        userErrorInfo: UserErrorInfo,
        onDismiss: () -> Unit,
        onAction: (UserErrorAction) -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Błąd") },
            text = { Text(userErrorInfo.message) },
            confirmButton = {
                Button(
                    onClick = {
                        onAction(userErrorInfo.actionType)
                        onDismiss()
                    }
                ) {
                    Text(
                        when (userErrorInfo.actionType) {
                            UserErrorAction.RETRY -> "Spróbuj ponownie"
                            UserErrorAction.OK -> "OK"
                            UserErrorAction.CONTACT_SUPPORT -> "Kontakt z supportem"
                        }
                    )
                }
            }
        )
    }
}
