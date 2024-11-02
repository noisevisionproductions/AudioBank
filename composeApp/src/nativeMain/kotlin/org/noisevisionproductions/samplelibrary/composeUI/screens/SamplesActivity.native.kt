package org.noisevisionproductions.samplelibrary.composeUI.screens

// In iosMain

import kotlinx.cinterop.ExperimentalForeignApi
import org.noisevisionproductions.samplelibrary.utils.files.FilePicker
import platform.UIKit.UIViewController
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.Foundation.NSURL

actual class SamplesActivity : UIViewController(), UIDocumentPickerDelegateProtocol {

    actual fun initializeFilePicker(): FilePicker {
        // Implement or stub out an iOS equivalent for FilePicker if needed
        return FilePicker()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        val filePicker = initializeFilePicker()
        // Add any setup logic here, such as triggering the file picker or other UI content
        presentFilePicker()
    }

    private fun presentFilePicker() {
        val documentPicker =
            UIDocumentPickerViewController(forOpeningContentTypes = listOf("public.data"))
        documentPicker.delegate = this
        presentViewController(documentPicker, animated = true, completion = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAt
        urls: List<NSURL>
    ) {
        urls.firstOrNull()?.let { selectedFileURL ->
            // Handle the selected file URL here, e.g., pass to shared KMP logic if necessary
            println("File selected: $selectedFileURL")
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        // Handle file picker cancellation if needed
        println("File picker was cancelled")
    }
}
