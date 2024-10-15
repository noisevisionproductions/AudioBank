package org.noisevisionproductions.samplelibrary.database

import android.content.Context
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import org.noisevisionproductions.samplelibrary.utils.ConfigManager

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
            val configManager = ConfigManager(context)

            connectionString = configManager.getAzureConnectionString()
                ?: throw RuntimeException("AZURE_CONNECTION_STRING is missing in key.properties")

            containerName = configManager.getAzureContainerName()
                ?: throw RuntimeException("AZURE_CONTAINER_NAME is missing in key.properties")

        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Could not load properties from ConfigManager: ${e.message}")
        }
    }

    fun validateConnections() {
        if (!::connectionString.isInitialized || !::containerName.isInitialized) {
            throw RuntimeException("One or more required properties are not initialized.")
        }
    }
}