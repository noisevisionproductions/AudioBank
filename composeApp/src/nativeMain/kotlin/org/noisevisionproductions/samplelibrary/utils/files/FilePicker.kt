package org.noisevisionproductions.samplelibrary.utils.files
// In iosMain

import platform.Foundation.*
import platform.UIKit.*
import kotlinx.cinterop.*
import org.noisevisionproductions.samplelibrary.utils.dataClasses.FileData
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

actual class FilePicker() : NSObject(),
    UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    private val viewController: UIViewController = TODO()
    private var onFilesPicked: ((List<FileData>) -> Unit)? = null

    actual fun pickFiles(
        mimeType: String,
        allowMultiple: Boolean,
        onFilesPicked: (List<FileData>) -> Unit
    ) {
        this.onFilesPicked = onFilesPicked
        if (mimeType.startsWith("image")) {
            pickImage()
        } else {
            onFilesPicked(emptyList()) // Currently supports only images
        }
    }

    private fun pickImage() {
        val picker = UIImagePickerController().apply {
            sourceType =
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            mediaTypes = listOf("public.image")
            delegate = this@FilePicker
        }
        viewController.presentViewController(picker, animated = true, completion = null)
    }

    // Called when an image is selected
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>?
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val selectedImage =
            didFinishPickingMediaWithInfo?.get(UIImagePickerControllerOriginalImage) as? UIImage
        val imageData = selectedImage?.JPEGRepresentation(1.0) // Convert to JPEG NSData

        val fileName = "picked_image.jpg" // Default name for picked image
        val filePath = saveImageToFile(imageData, fileName) // Save image to file

        if (filePath != null) {
            onFilesPicked?.invoke(
                listOf(
                    FileData(
                        name = fileName,
                        fileData = imageData?.toByteArray() ?: ByteArray(0),
                        filePath = filePath
                    )
                )
            )
        } else {
            onFilesPicked?.invoke(emptyList())
        }
        onFilesPicked = null
    }

    // Called when the picker is cancelled
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onFilesPicked?.invoke(emptyList())
        onFilesPicked = null
    }

    private fun saveImageToFile(imageData: NSData?, fileName: String): String? {
        val filePath = NSTemporaryDirectory() + fileName
        return try {
            imageData?.writeToFile(filePath, atomically = true)
            filePath
        } catch (e: Exception) {
            NSLog("Error saving image to file: ${e.localizedMessage}")
            null
        }
    }
}

// Extension function to convert NSData to ByteArray
fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    usePinned { buffer -> memcpy(buffer.addressOf(0), this.bytes, this.length) }
    return bytes
}
