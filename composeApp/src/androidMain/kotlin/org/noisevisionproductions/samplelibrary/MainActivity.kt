package org.noisevisionproductions.samplelibrary

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.FirebaseApp
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.screens.LoginActivity
import org.noisevisionproductions.samplelibrary.database.AzureStorageClient
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import org.noisevisionproductions.samplelibrary.interfaces.downloadAndSaveFile

class MainActivity : ComponentActivity() {
    var pendingFileUrl: String? = null
    var pendingFileName: String? = null
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
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
    private lateinit var authService: AuthService

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        AppContext.setUp(appContext)
        FirebaseApp.initializeApp(this)

        AzureStorageClient.loadAzureConnections(this)
        AzureStorageClient.validateConnections()

        authService = AuthService()
    }
}