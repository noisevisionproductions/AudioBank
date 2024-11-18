package org.noisevisionproductions.samplelibrary.interfaces

// In iosMain

import androidx.compose.ui.text.font.FontFamily
import platform.Foundation.*
import platform.UIKit.*
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import kotlin.math.abs

actual class AppsUtils actual constructor() {

    // iOS does not have a download manager. You could implement downloading with URLSession.
    actual fun downloadAndSaveFile(
        context: Any?,
        fileUrl: String,
        fileName: String,
        onCompletion: () -> Unit
    ) {
        val url = NSURL(string = fileUrl)
        val destinationPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String + "/$fileName"
        val destinationUrl = NSURL.fileURLWithPath(destinationPath)

        val task = NSURLSession.sharedSession.dataTaskWithURL(url!!) { data, response, error ->
            if (error != null) {
                NSLog("Error downloading file: ${error.localizedDescription}")
                return@dataTaskWithURL
            }
            data?.writeToURL(destinationUrl, true, null)
            NSLog("File downloaded to $destinationPath")
            onCompletion()
        }
        task.resume()
    }

    // iOS does not use permission checks in the same way, as storage permissions are managed by the app sandbox.
    fun checkAndRequestStoragePermission(
        fileUrl: String,
        fileName: String,
        viewController: UIViewController
    ) {
        downloadAndSaveFile(viewController, fileUrl, fileName) {
            showToast("Pobieranie rozpoczęte", viewController)
        }
    }

    // iOS equivalent for displaying toast messages
    actual fun showPostCreatedMessage(message: String) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.let { showToast(message, it) }
    }

    fun showToast(message: String, viewController: UIViewController) {
        val alert = UIAlertController(
            title = null,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )
        viewController.presentViewController(alert, animated = true, completion = null)
        // Dismiss after 2 seconds
        val delay = dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC.toLong())
        dispatch_after(delay, dispatch_get_main_queue()) {
            alert.dismissViewControllerAnimated(
                true,
                completion = null
            )
        }
    }

    // Gets the current timestamp in a specified format
    actual fun getCurrentTimestamp(): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = "dd-MM-yyyy HH:mm:ss"
            locale = NSLocale.currentLocale
        }
        return formatter.stringFromDate(NSDate())
    }

    // Format a timestamp to a "time ago" string
    actual fun formatTimeAgo(timestamp: String): String {
        val formatter = NSDateFormatter().apply {
            dateFormat = "dd-MM-yyyy HH:mm:ss"
            locale = NSLocale.currentLocale
        }
        val date = formatter.dateFromString(timestamp) ?: return "Nieznany czas"

        val currentTime = NSDate().timeIntervalSince1970
        val timestampMillis = date.timeIntervalSince1970
        val timeDifference = abs(currentTime - timestampMillis)

        val minutes = timeDifference / 60
        val hours = timeDifference / 3600
        val days = timeDifference / 86400

        return when {
            minutes < 1 -> "Przed chwilą"
            minutes < 60 -> "$minutes minut temu"
            hours < 24 -> "$hours godzin temu"
            days < 7 -> "$days dni temu"
            days < 30 -> "${days / 7} tygodni temu"
            else -> "ponad miesiąc temu"
        }
    }

    // Load a custom font family, if available, using iOS font resources.
    actual fun poppinsFontFamily(): FontFamily {
        // Currently, there's no direct equivalent in iOS for Compose FontFamily.
        // iOS uses system fonts, or you must import a custom font and reference it specifically in SwiftUI/UIKit.
        return FontFamily.Default
    }

}

actual fun poppinsFontFamily(): FontFamily {
    TODO("Not yet implemented")
}

actual fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
) {
}

actual fun showPostCreatedMessage(message: String) {
}

actual fun formatTimeAgo(timestamp: String): String {
    TODO("Not yet implemented")
}

actual fun getCurrentTimestamp(): String {
    TODO("Not yet implemented")
}