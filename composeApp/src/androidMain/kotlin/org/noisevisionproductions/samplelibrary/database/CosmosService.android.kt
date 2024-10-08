package org.noisevisionproductions.samplelibrary.database

import android.content.Context
import org.noisevisionproductions.samplelibrary.FileMetadata
import org.noisevisionproductions.samplelibrary.interfaces.AppContext
import org.noisevisionproductions.samplelibrary.restRetrofit.fetchDocumentsFromCosmosDB

actual class CosmosService {

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

    actual suspend fun getSynchronizedData(): List<FileMetadata> {
        // Pobierz listę plików z Azure Storage (to może pozostać bez zmian)
        val context = AppContext.get() as Context
        val azureStorageService = AzureStorageService()
        val directoryPath = "samples"
        val (fileUrls, _) = azureStorageService.listFilesInBucket(
            directoryPath,
            continuationToken = null
        )

        // Pobierz dane z Cosmos DB za pomocą REST API
        val cosmosMetadata = fetchFileMetadata(context) ?: emptyList()


        // Mapowanie wyników na obiekty FileMetadata
        val filesWithMetadata = fileUrls.mapNotNull { blobUrl ->
            val fileName = blobUrl.substringAfterLast("/")

            // Znormalizuj URL z Azure Storage, zamieniając host
            val normalizedBlobUrl = normalizeBlobUrl(blobUrl)

            // Pobierz tylko część ścieżki pliku (ignorując różnice w hostach)
            val blobPath =
                decodeUrlForSynchronization(normalizedBlobUrl).substringAfter(".blob.core.windows.net")
            val metadata = cosmosMetadata.find { it.url.contains(blobPath) }

            println("Blob Path: $blobPath")
            println("Cosmos Metadata URL: ${metadata?.url}")

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


        return filesWithMetadata
    }
}

private fun normalizeBlobUrl(blobUrl: String): String {
    return blobUrl.replace(
        "https://storagewithsamples.blob.core.windows.net",
        "https://storagemetadataeu.blob.core.windows.net"
    )
}

private fun decodeUrlForSynchronization(url: String): String {
    return url
        .replace("%20", " ")
        .replace("%23", "#")
        .replace("%2F", "/")
        .replace("\\", "")
}