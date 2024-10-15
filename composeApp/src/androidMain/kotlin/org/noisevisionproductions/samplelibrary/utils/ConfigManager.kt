package org.noisevisionproductions.samplelibrary.utils

import android.content.Context
import java.io.IOException
import java.util.Properties

class ConfigManager(context: Context) {

    private val properties: Properties = Properties()

    init {
        try {
            context.assets.open("key.properties").use { inputStream ->
                properties.load(inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getValue(key: String): String? {
        return properties.getProperty(key)
    }

    fun getCosmosMasterKeyForMetadata(): String? {
        return getValue("COSMOS_AZURE_KEY_METADATA")
    }

    fun getCosmosEndPointForMetadata(): String? {
        return getValue("COSMOS_AZURE_ENDPOINT_METADATA")
    }

    fun getAzureConnectionString(): String? {
        return getValue("AZURE_CONNECTION_STRING")
    }

    fun getAzureContainerName(): String? {
        return getValue("AZURE_CONTAINER_NAME")
    }
}