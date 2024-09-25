package org.noisevisionproductions.samplelibrary

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.FirebaseApp
import org.noisevisionproductions.samplelibrary.composeUI.App
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import org.noisevisionproductions.samplelibrary.interfaces.downloadAndSaveFile


class MainActivity : ComponentActivity() {
    var pendingFileUrl: String? = null
    var pendingFileName: String? = null
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
        setupPermissionLauncher()
    }

    private fun setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Download the file if permission is granted
                pendingFileUrl?.let { url ->
                    pendingFileName?.let { name ->
                        downloadAndSaveFile(this, url, name) {
                            Toast.makeText(this, "Pobieranie rozpoczęte", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Do pobierania plików wymagana jest zgoda na dostęp do pobierania plików.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

class SampleLibrary : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        AppContext.setUp(appContext)
        FirebaseApp.initializeApp(this)
    }
}