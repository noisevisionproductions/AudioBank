package org.noisevisionproductions.samplelibrary.utils.files

import org.noisevisionproductions.samplelibrary.utils.dataClasses.FileData

expect class FilePicker {
    fun pickFiles(mimeType: String, allowMultiple: Boolean, onFilesPicked: (List<FileData>) -> Unit)
}