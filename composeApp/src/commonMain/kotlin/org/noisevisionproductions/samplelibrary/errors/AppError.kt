package org.noisevisionproductions.samplelibrary.errors

sealed class AppError(
    override val message: String,
    open val technicalDetails: String? = null,
    open val errorCode: String? = null
) : Throwable(message) {

    data class NetworkError(
        override val message: String = "Brak połączenia z internetem",
        val throwable: Throwable? = null,
        override val technicalDetails: String? = throwable?.stackTraceToString()
    ) : AppError(message, technicalDetails)

    data class ApiError(
        override val message: String,
        override val errorCode: String,
        val statusCode: Int,
        val throwable: Throwable? = null,
        override val technicalDetails: String? = throwable?.stackTraceToString()
    ) : AppError(message, technicalDetails, errorCode)

    data class BusinessError(
        override val message: String,
        override val errorCode: String? = null
    ) : AppError(message, errorCode = errorCode)

    data class UnexpectedError(
        override val message: String = "Wystąpił nieoczekiwany błąd",
        val throwable: Throwable? = null,
        override val technicalDetails: String? = throwable?.stackTraceToString()
    ) : AppError(message, technicalDetails)
}
