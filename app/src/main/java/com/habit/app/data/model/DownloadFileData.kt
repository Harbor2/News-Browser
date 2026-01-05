package com.habit.app.data.model

import androidx.annotation.StringDef
import com.wyz.emlibrary.util.EMUtil

data class DownloadFileData(
    var fileName: String = "",
) {
    var isDownloaded: Boolean = false
    var filePath: String = ""
    var fileModifyTime: Long = 0
    var fileSize: Long = 0

    var downloadUrl: String = ""
    var downloadProgress: Int = 0
    var fileType: String = ""

    var isPause: Boolean = false

    fun getFormatData(): String {
        return EMUtil.formatDateFromTimestamp("dd/MM/yyyy", fileModifyTime)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadFileData

        if (filePath != other.filePath) return false
        return downloadUrl == other.downloadUrl
    }

    override fun hashCode(): Int {
        var result = filePath.hashCode()
        result = 31 * result + downloadUrl.hashCode()
        return result
    }

    companion object {
        const val TYPE_PIC = "Image"
        const val TYPE_VIDEO = "Video"
        const val TYPE_AUDIO = "Audio"
        const val TYPE_DOC = "Doc"
        const val TYPE_APK = "App"
        const val TYPE_ZIP = "Zip"
        const val TYPE_UNKNOWN = "Unknown"

        @StringDef(TYPE_PIC, TYPE_VIDEO, TYPE_AUDIO, TYPE_DOC, TYPE_APK, TYPE_ZIP, TYPE_UNKNOWN)
        annotation class FileType
    }
}
