package org.noisevisionproductions.samplelibrary.errors.handleGivenErrors

// In iosMain

import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityFlags
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.darwin.dispatch_get_main_queue
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import org.noisevisionproductions.samplelibrary.errors.AppError

actual class NetworkUtils actual constructor() {

    actual fun isNetworkAvailable(): Boolean {
        return checkReachability("www.google.com") // Using a common external URL to check reachability
    }

    actual fun checkNetworkAvailabilityOrThrow() {
        if (!isNetworkAvailable()) {
            throw AppError.NetworkError("No internet connection")
        }
    }

    private fun checkReachability(hostname: String): Boolean {
        memScoped {
            val reachability = SCNetworkReachabilityCreateWithName(null, hostname) ?: return false
            val flags = alloc<SCNetworkReachabilityFlags>()
            val isReachable = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
            if (!isReachable) return false

            val flagsValue = flags.value
            return (flagsValue and SCNetworkReachabilityFlags.kSCNetworkReachabilityFlagsReachable) != 0u &&
                    (flagsValue and SCNetworkReachabilityFlags.kSCNetworkReachabilityFlagsConnectionRequired) == 0u
        }
    }
}
