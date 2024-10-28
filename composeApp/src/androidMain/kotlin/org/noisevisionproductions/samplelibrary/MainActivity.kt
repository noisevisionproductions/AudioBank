package org.noisevisionproductions.samplelibrary

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.FirebaseApp
import org.noisevisionproductions.samplelibrary.auth.AuthService
import org.noisevisionproductions.samplelibrary.composeUI.screens.LoginActivity
import org.noisevisionproductions.samplelibrary.composeUI.screens.SamplesActivity
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadNewSound
import org.noisevisionproductions.samplelibrary.composeUI.screens.samples.soundsUploading.UploadSoundViewModel
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import org.noisevisionproductions.samplelibrary.interfaces.downloadAndSaveFile
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker

class MainActivity : ComponentActivity() {
    var pendingFileUrl: String? = null
    var pendingFileName: String? = null
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authService = AuthService()


        if (authService.isUserLoggedIn()) {
            val mainMenuIntent = Intent(this, SamplesActivity::class.java)
            startActivity(mainMenuIntent)
        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

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



        authService = AuthService()
    }
}