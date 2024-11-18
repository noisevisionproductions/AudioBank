package org.noisevisionproductions.samplelibrary.interfaces

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.noisevisionproductions.audiobank.R
import org.noisevisionproductions.samplelibrary.errors.UserErrorAction
import org.noisevisionproductions.samplelibrary.errors.UserErrorInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun checkAndRequestStoragePermission(
    fileUrl: String,
    fileName: String,
    context: Context
) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            downloadAndSaveFile(context, fileUrl, fileName) {}
        }

        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED -> {
            downloadAndSaveFile(context, fileUrl, fileName) {}
        }

        context is Activity -> {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                100
            )
        }

        else -> {
            Toast.makeText(
                context,
                "Do pobierania plików wymagana jest zgoda na dostęp do pobierania plików.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
actual fun downloadAndSaveFile(
    context: Any?,
    fileUrl: String,
    fileName: String,
    onCompletion: () -> Unit
) {
    val androidContext = AppContext.get() as Context

    try {
        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Pobieranie pliku...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager =
            androidContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    onCompletion()
                    context?.unregisterReceiver(this)
                }
            }
        }

        androidContext.registerReceiver(
            onComplete,
            IntentFilter(DownloadManager.ACTION_VIEW_DOWNLOADS),
            Context.RECEIVER_NOT_EXPORTED
        )

        Toast.makeText(androidContext, "Pobieranie rozpoczęte", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        UserErrorInfo(
            message = "Błąd podczas pobierania dźwięku - ${e.message}",
            actionType = UserErrorAction.OK,
            errorId = "DOWNLOAD_SOUND_ERROR"
        )
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
    Font(R.font.montserrat_thin, FontWeight.Thin),
    Font(R.font.montserrat_thin_italic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.montserrat_extra_light, FontWeight.ExtraLight),
    Font(R.font.montserrat_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.montserrat_medium, FontWeight.Medium),
    Font(R.font.montserrat_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.montserrat_semi_bold, FontWeight.SemiBold),
    Font(R.font.montserrat_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.montserrat_extra_bold, FontWeight.ExtraBold),
    Font(R.font.montserrat_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.montserrat_black, FontWeight.Black),
    Font(R.font.montserrat_black_italic, FontWeight.Black, FontStyle.Italic)
)