package org.noisevisionproductions.samplelibrary.interfaces

import android.media.MediaPlayer
import com.azure.storage.blob.models.ListBlobsOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.noisevisionproductions.samplelibrary.database.AzureStorageClient

actual class AzureStorageService {

    actual suspend fun listFilesInBucket(bucketName: String, continuationToken: String): Pair<List<String>, String?> {
        return withContext(Dispatchers.IO) {
            try {
                /* val containerClient = AzureStorageClient.containerClient
                 val blobs = containerClient.listBlobs()

                 if (!containerClient.exists()) {
                     containerClient.create()
                 }

                 return@withContext blobs.map { blobItem ->
                     val blobClient = containerClient.getBlobClient(blobItem.name)
                     blobClient.blobUrl
                 }*/
                val containerClient = AzureStorageClient.containerClient
                if (!containerClient.exists()) {
                    containerClient.create()
                }
                val maxResultPerPage = 20

                val options = ListBlobsOptions().setMaxResultsPerPage(maxResultPerPage)
                val pager = containerClient.listBlobs(options, null)
                val blobItems = mutableListOf<String>()
                var newContinuationToken: String? = continuationToken

                for (page in pager.streamByPage(continuationToken)) {
                    for (blobItem in page.elements) {
                        val blobClient = containerClient.getBlobClient(blobItem.name)
                        blobItems.add(blobClient.blobUrl)
                    }
                    newContinuationToken = page.continuationToken
                    break
                }

                return@withContext Pair(blobItems, newContinuationToken)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Pair(emptyList<String>(), null)
            }
        }
    }
}
