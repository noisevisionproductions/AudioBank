package org.noisevisionproductions.samplelibrary.errors

class ErrorHandler(private val errorLogger: ErrorLogger) {
    fun handleUserError(
        error: AppError,
        errorId: String,
        retryAction: (() -> Unit)? = null
    ): UserErrorInfo {
        errorLogger.logError(error)

        return when (error) {
            is AppError.NetworkError -> UserErrorInfo(
                message = "Sprawdź połączenie z internetem",
                actionType = UserErrorAction.RETRY,
                errorId = errorId,
                retryAction = retryAction
            )

            is AppError.ApiError -> UserErrorInfo(
                message = error.message,
                actionType = UserErrorAction.RETRY,
                errorId = errorId,
                retryAction = retryAction
            )

            is AppError.BusinessError -> UserErrorInfo(
                message = error.message,
                actionType = UserErrorAction.OK,
                errorId = errorId,
                retryAction = retryAction
            )

            is AppError.UnexpectedError -> UserErrorInfo(
                message = "Przepraszamy, wystąpił nieoczekiwany błąd",
                actionType = UserErrorAction.CONTACT_SUPPORT,
                errorId = errorId,
                retryAction = retryAction
            )
        }
    }
}