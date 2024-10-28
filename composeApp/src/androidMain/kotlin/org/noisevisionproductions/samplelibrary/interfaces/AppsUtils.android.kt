package org.noisevisionproductions.samplelibrary.interfaces

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import org.noisevisionproductions.audiobank.R
import org.noisevisionproductions.samplelibrary.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs


actual fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
) {
    val androidContext = AppContext.get() as Context

    if (fileUrl.isBlank()) {
        Toast.makeText(androidContext, "Nie wybrano żadnego pliku", Toast.LENGTH_SHORT)
            .show()
        return
    }

    if (fileName.isBlank()) {
        Toast.makeText(androidContext, "Nie wybrano żadnego pliku", Toast.LENGTH_SHORT)
            .show()
        return
    }

    val request = DownloadManager.Request(Uri.parse(fileUrl))
        .setTitle(fileName)
        .setDescription("Pobieranie pliku...")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    val downloadManager =
        androidContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

    onCompletion()
}

fun checkAndRequestStoragePermission(
    fileUrl: String,
    fileName: String,
    activity: MainActivity
) {
    val androidContext = activity.applicationContext

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                androidContext, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted, start the download
            downloadAndSaveFile(androidContext, fileUrl, fileName) {
                Toast.makeText(androidContext, "Pobieranie rozpoczęte", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Request permission and store the file details for later use
            activity.pendingFileUrl = fileUrl
            activity.pendingFileName = fileName
            activity.requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    } else {
        // Android 10 and above, no need for WRITE_EXTERNAL_STORAGE permission
        downloadAndSaveFile(androidContext, fileUrl, fileName) {
            Toast.makeText(androidContext, "Pobieranie rozpoczęte", Toast.LENGTH_SHORT).show()
        }
    }
}

actual fun showPostCreatedMessage(message: String) {
    val context = AppContext.get() as Context
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

actual fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

actual fun formatTimeAgo(timestamp: String): String {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())

    val date = try {
        dateFormat.parse(timestamp)
    } catch (e: Exception) {
        return "Nieznany czas"
    }

    val timestampMillis = date?.time ?: return "Nieznany czas"

    val currentTime = System.currentTimeMillis()
    val timeDifference = abs(currentTime - timestampMillis)

    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
    val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
    val days = TimeUnit.MILLISECONDS.toDays(timeDifference)

    return when {
        minutes < 1 -> "Przed chwilą"
        minutes < 60 -> "$minutes minut temu"
        hours < 24 -> "$hours godzin temu"
        days < 7 -> "$days dni temu"
        days < 30 -> "${days / 7} tygodni temu"
        else -> "ponad miesiąc temu"
    }
}

actual fun poppinsFontFamily(): FontFamily = FontFamily(
    Font(R.font.poppins_thin, FontWeight.Thin),
    Font(R.font.poppins_thin_italic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.poppins_extra_light, FontWeight.ExtraLight),
    Font(R.font.poppins_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.poppins_light, FontWeight.Light),
    Font(R.font.poppins_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.poppins_semi_bold, FontWeight.SemiBold),
    Font(R.font.poppins_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.poppins_extra_bold, FontWeight.ExtraBold),
    Font(R.font.poppins_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.poppins_black, FontWeight.Black),
    Font(R.font.poppins_black_italic, FontWeight.Black, FontStyle.Italic)
)