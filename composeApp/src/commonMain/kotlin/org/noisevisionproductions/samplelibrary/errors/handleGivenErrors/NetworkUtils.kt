package org.noisevisionproductions.samplelibrary.errors.handleGivenErrors

expect class NetworkUtils() {
    fun isNetworkAvailable(): Boolean
    fun checkNetworkAvailabilityOrThrow()
}