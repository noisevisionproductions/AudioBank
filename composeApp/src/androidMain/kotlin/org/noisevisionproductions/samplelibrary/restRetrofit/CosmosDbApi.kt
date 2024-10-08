package org.noisevisionproductions.samplelibrary.restRetrofit

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface CosmosDbApi {
    @GET("dbs/{database}/colls/{collection}/docs")
    suspend fun getDocuments(
        @Path("database") database: String,
        @Path("collection") collection: String,
        @Header("Authorization") authorization: String,
        @Header("x-ms-date") date: String,
        @Header("x-ms-version") msVersion: String
    ): Response<CosmosDbResponse>
}


data class CosmosDbResponse(
    @SerializedName("Documents") val documents: List<DocumentCosmosDB>,
    @SerializedName("_count") val count: Int
)

data class DocumentCosmosDB(
    @SerializedName("url") val url: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("duration") val duration: String,
)