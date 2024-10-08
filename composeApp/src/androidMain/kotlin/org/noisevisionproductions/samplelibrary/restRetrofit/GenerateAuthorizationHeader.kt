package org.noisevisionproductions.samplelibrary.restRetrofit

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.delay

fun generateAuthorizationHeader(
    verb: String,
    resourceType: String,
    resourceLink: String,
    masterKey: String,
    date: String
): String {
    val key = Base64.decode(masterKey, Base64.DEFAULT)
    val payload = "$verb\n$resourceType\n$resourceLink\n$date\n\n"

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(key, "HmacSHA256"))

    val hash = mac.doFinal(payload.toByteArray())
    val signature = Base64.encodeToString(hash, Base64.NO_WRAP)

    return "type=master&ver=1.0&sig=$signature"
}

suspend fun fetchDocumentsFromCosmosDB(
    context: Context,
    maxRetries: Int = 3
): List<DocumentCosmosDB>? {
    return withContext(Dispatchers.IO) {
        repeat(maxRetries) { attempt ->
            try {
                if (attempt == 0) {
                    delay(1000)
                }

                val date = getCurrentUtcDate()
                val masterKey = loadMasterKeyFromProperties(context)
                val resourceLink = "dbs/metadata/colls/metadatafromsamples"
                val authHeader = generateAuthorizationHeader(
                    verb = "get",
                    resourceType = "docs",
                    resourceLink = resourceLink,
                    masterKey = masterKey,
                    date = date
                )

                val retrofit = RetrofitClient.getRetrofit(context)
                val api = retrofit.create(CosmosDbApi::class.java)
                val response = api.getDocuments(
                    database = "metadata",
                    collection = "metadatafromsamples",
                    authorization = authHeader,
                    date = date,
                    msVersion = "2018-12-31"
                )

                if (response.isSuccessful) {
                    return@withContext response.body()?.documents
                } else {
                    Log.e(
                        "CosmosDB",
                        "Attempt ${attempt + 1} failed. Error: ${response.code()} - ${
                            response.errorBody()?.string()
                        }"
                    )
                    if (attempt == maxRetries - 1) {
                        return@withContext null
                    }
                    delay(1000L * (attempt + 1))
                }
            } catch (e: Exception) {
                Log.e("CosmosDB", "Attempt ${attempt + 1} failed. Exception: ${e.message}", e)
                if (attempt == maxRetries - 1) {
                    return@withContext null
                }
                delay(1000L * (attempt + 1))
            }
        }
        null
    }
}

private fun loadMasterKeyFromProperties(context: Context): String {
    val properties = Properties()
    context.assets.open("key.properties").use { inputStream ->
        properties.load(inputStream)
    }
    return properties.getProperty("COSMOS_AZURE_KEY")
}

private fun getCurrentUtcDate(): String {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormat.format(Date()).lowercase()
}
