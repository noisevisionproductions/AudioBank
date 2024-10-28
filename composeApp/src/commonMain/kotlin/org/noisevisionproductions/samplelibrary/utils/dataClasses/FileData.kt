package org.noisevisionproductions.samplelibrary.utils.dataClasses

data class FileData(
    val name: String,
    val fileData: ByteArray,
    val filePath: String? = null,      // Optional file path
    val bpmValue: String? = null,       // Optional BPM
    val toneValue: String? = null,      // Optional Tone
    val tags: List<String> = emptyList() // List of tags
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileData) return false

        if (name != other.name) return false
        if (!fileData.contentEquals(other.fileData)) return false
        if (filePath != other.filePath) return false
        if (bpmValue != other.bpmValue) return false
        if (toneValue != other.toneValue) return false
        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fileData.contentHashCode()
        result = 31 * result + (filePath?.hashCode() ?: 0)
        result = 31 * result + (bpmValue?.hashCode() ?: 0)
        result = 31 * result + (toneValue?.hashCode() ?: 0)
        result = 31 * result + tags.hashCode()
        return result
    }
}
