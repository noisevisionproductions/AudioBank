package org.noisevisionproductions.samplelibrary.database

import android.content.Context
import org.noisevisionproductions.samplelibrary.database.restRetrofit.fetchDocumentsFromCosmosDB
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import org.noisevisionproductions.samplelibrary.utils.FileMetadata
import org.noisevisionproductions.samplelibrary.utils.decodeUrlForSynchronization

actual class AzureCosmosDBService {

    private suspend fun fetchFileMetadata(context: Context): List<FileMetadata>? {
        val documents = fetchDocumentsFromCosmosDB(context)

        return documents?.map { document ->
            FileMetadata(
                url = document.url,
                fileName = document.fileName,
                duration = document.duration
            )
        }
    }

    actual suspend fun getSynchronizedData(continuationToken: String?): Pair<List<FileMetadata>, String?> {
        val context = AppContext.get() as Context
        val azureStorageService = AzureStorageService()
        val directoryPath = "samples"

        val (fileUrls, newContinuationToken) = azureStorageService.listFilesInBucket(
            directoryPath,
            continuationToken
        )

        // Pobierz dane z Cosmos DB za pomocą REST API
        val cosmosMetadata = fetchFileMetadata(context) ?: emptyList()

        // Mapowanie wyników na obiekty FileMetadata
        val filesWithMetadata = fileUrls.mapNotNull { blobUrl ->
            val fileName = blobUrl.substringAfterLast("/")

            val normalizedBlobUrl = normalizeBlobUrl(blobUrl)

            val blobPath =
                decodeUrlForSynchronization(normalizedBlobUrl)
                    .substringAfter(".blob.core.windows.net")
            val metadata = cosmosMetadata.find { it.url.contains(blobPath) }

            if (metadata != null) {
                FileMetadata(
                    url = blobUrl,
                    fileName = fileName,
                    duration = metadata.duration
                )
            } else {
                null
            }
        }
        return Pair(filesWithMetadata, newContinuationToken)
    }
}

private fun normalizeBlobUrl(blobUrl: String): String {
    return blobUrl.replace(
        "https://storagewithsamples.blob.core.windows.net",
        "https://storagemetadataeu.blob.core.windows.net"
    )
}

