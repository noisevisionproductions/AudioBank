package org.noisevisionproductions.samplelibrary.restRetrofit

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Properties

object RetrofitClient {

    private var BASE_URL: String = ""

    private val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()

    private var retrofit: Retrofit? = null

    fun getRetrofit(context: Context): Retrofit {
        if (retrofit == null) {
            loadProperties(context)
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    private fun loadProperties(context: Context) {
        val properties = Properties()
        context.assets.open("key.properties").use { inputStream ->
            properties.load(inputStream)
        }
        BASE_URL = properties.getProperty("COSMOS_AZURE_ENDPOINT", "")
    }
}