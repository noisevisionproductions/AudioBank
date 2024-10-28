package org.noisevisionproductions.samplelibrary.errors.handleGivenErrors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import org.noisevisionproductions.samplelibrary.errors.AppError
import org.noisevisionproductions.samplelibrary.interfaces.AppContext

actual class NetworkUtils actual constructor() {
    actual fun isNetworkAvailable(): Boolean {
        val context = AppContext.get() as Context

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    actual fun checkNetworkAvailabilityOrThrow() {
        if (!isNetworkAvailable()) {
            throw AppError.NetworkError()
        }
    }
}