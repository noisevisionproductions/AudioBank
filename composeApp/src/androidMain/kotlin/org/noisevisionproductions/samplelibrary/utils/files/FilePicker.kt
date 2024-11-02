package org.noisevisionproductions.samplelibrary.utils.files

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import org.noisevisionproductions.samplelibrary.utils.dataClasses.FileData
import java.io.File
import java.io.FileOutputStream

actual class FilePicker(private val activity: ComponentActivity) {

    private var singleFileLauncher: ActivityResultLauncher<String>
    private var multipleFilesLauncher: ActivityResultLauncher<String>
    private var onFilePicked: ((FileData?) -> Unit)? = null
    private var onFilesPicked: ((List<FileData>) -> Unit)? = null

    init {
        singleFileLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            try {
                if (uri != null) {
                    val fileData = getFileDataFromUri(activity, uri)
                    onFilePicked?.invoke(fileData)
                } else {
                    onFilePicked?.invoke(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFilePicked?.invoke(null)
            } finally {
                onFilePicked = null
            }
        }

        // Launcher for multiple files selection
        multipleFilesLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris: List<Uri>? ->
            try {
                if (uris != null) {
                    val fileDataList = uris.mapNotNull { uri ->
                        try {
                            getFileDataFromUri(activity, uri)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    onFilesPicked?.invoke(fileDataList)
                } else {
                    onFilesPicked?.invoke(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFilesPicked?.invoke(emptyList())
            } finally {
                onFilesPicked = null
            }
        }
    }

    actual fun pickFiles(
        mimeType: String,
        allowMultiple: Boolean,
        onFilesPicked: (List<FileData>) -> Unit
    ) {
        if (allowMultiple) {
            this.onFilesPicked = onFilesPicked
            multipleFilesLauncher.launch(mimeType)
        } else {
            this.onFilePicked = { fileData ->
                if (fileData != null) {
                    onFilesPicked(listOf(fileData))
                } else {
                    onFilesPicked(emptyList())
                }
            }
            singleFileLauncher.launch(mimeType)
        }
    }

    private fun getFileDataFromUri(context: Context, uri: Uri): FileData? {
        val fileName = getFileName(context, uri)
        val fileBytes = readFileAsByteArray(context, uri)
        val filePath = getFilePathFromUri(context, uri)

        return if (fileBytes != null) {
            FileData(
                name = fileName,
                fileData = fileBytes,
                filePath = filePath
            )
        } else {
            null
        }
    }

    private fun readFileAsByteArray(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        if (uri.scheme == "file") {
            filePath = uri.path
        } else if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex)
                }
            }
        }
        if (filePath == null) {
            filePath = copyUriToFile(context, uri)
        }
        return filePath
    }

    private fun copyUriToFile(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, getFileName(context, uri))
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = "unknown_file"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
}
