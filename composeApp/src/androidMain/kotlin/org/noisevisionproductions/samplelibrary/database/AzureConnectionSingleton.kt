package org.noisevisionproductions.samplelibrary.database

import android.content.Context
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import java.util.Properties

object AzureStorageClient {
    private lateinit var connectionString: String
    private lateinit var containerName: String

    val containerClient: BlobContainerClient by lazy {
        BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient()
            .getBlobContainerClient(containerName)
    }

    fun loadAzureConnections(context: Context) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("key.properties")
            val properties = Properties().apply {
                load(inputStream)
            }

            connectionString = properties.getProperty("AZURE_CONNECTION_STRING")
            containerName = properties.getProperty("AZURE_CONTAINER_NAME")

        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Could not load properties file: ${e.message}")
        }
    }

    fun validateConnections() {
        if (!::connectionString.isInitialized || !::containerName.isInitialized) {
            throw RuntimeException("One or more required properties are not initialized.")
        }
    }
}