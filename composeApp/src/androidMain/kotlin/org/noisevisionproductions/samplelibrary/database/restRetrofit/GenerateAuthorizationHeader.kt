package org.noisevisionproductions.samplelibrary.database.restRetrofit

import android.content.Context
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.utils.ConfigManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min
import kotlin.math.pow

private var cachedAuthHeader: String? = null
private var lastAuthHeaderDate: String? = null
private var lastVerb: String? = null
private var lastResourceLink: String? = null

fun generateAuthorizationHeader(
    context: Context,
    verb: String,
    resourceType: String,
    resourceLink: String,
    date: String
): String? {
    val configManager = ConfigManager(context)
    val masterKey = configManager.getCosmosMasterKeyForMetadata() ?: return null

    if (cachedAuthHeader != null && lastAuthHeaderDate == date && lastVerb == verb && lastResourceLink == resourceLink) {
        return cachedAuthHeader!!
    }

    val key = Base64.decode(masterKey, Base64.DEFAULT)
    val payload = "$verb\n$resourceType\n$resourceLink\n$date\n\n"

    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(key, "HmacSHA256"))

    val hash = mac.doFinal(payload.toByteArray())
    val signature = Base64.encodeToString(hash, Base64.NO_WRAP)

    cachedAuthHeader = "type=master&ver=1.0&sig=$signature"
    lastAuthHeaderDate = date
    return cachedAuthHeader!!
}

suspend fun fetchDocumentsFromCosmosDB(
    context: Context,
    maxRetries: Int = 5
): List<DocumentCosmosDB>? = withContext(Dispatchers.IO) {
    var attempt = 0
    while (attempt < maxRetries) {
        try {
            val date = getCurrentUtcDate()
            val resourceLink = "dbs/metadata/colls/metadatafromsamples"
            val authHeader = generateAuthorizationHeader(
                context,
                verb = "get",
                resourceType = "docs",
                resourceLink = resourceLink,
                date = date
            )

            if (authHeader == null) {
                Log.e("CosmosDB", "Authorization header generation failed.")
                return@withContext null
            }

            val retrofit = RetrofitClient.getRetrofit(context)
            val api = retrofit.create(CosmosDbApiGet::class.java)
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
                if (++attempt == maxRetries) return@withContext null
                delay(calculateBackoff(attempt))
            }
        } catch (e: Exception) {
            Log.e("CosmosDB", "Attempt ${attempt + 1} failed. Exception: ${e.message}", e)
            if (++attempt == maxRetries) return@withContext null
            delay(calculateBackoff(attempt))
        }
    }
    null
}

private fun calculateBackoff(attempt: Int): Long {
    val maxBackoff: Long = 30000 // 30 seconds in milliseconds
    val calculatedBackoff = (1000.0 * 2.0.pow(attempt.toDouble())).toLong()
    return min(calculatedBackoff, maxBackoff)
}

private fun getCurrentUtcDate(): String {
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormat.format(Date()).lowercase()
}
