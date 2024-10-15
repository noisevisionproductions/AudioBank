package org.noisevisionproductions.samplelibrary.database.restRetrofit

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.noisevisionproductions.samplelibrary.utils.ConfigManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BASIC)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private var retrofit: Retrofit? = null

    fun getRetrofit(context: Context): Retrofit {
        val configManager = ConfigManager(context)
        val cosmosEndPoint = configManager.getCosmosEndPointForMetadata()

        if (retrofit == null) {
            retrofit = if (cosmosEndPoint != null) {
                Retrofit.Builder()
                    .baseUrl(cosmosEndPoint)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            } else {
                throw IllegalStateException("Cosmos endpoint is null. Check your configuration.")
            }
        }
        return retrofit!!
    }
}