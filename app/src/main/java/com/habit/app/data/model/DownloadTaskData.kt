package com.habit.app.data.model

/**
 * 下载任务数据
 */
data class DownloadTaskData(
    val downloadId: Int = 0,
    val downloadUrl: String = "",
    val downloadFileName: String? = null,
    val downloadFileSize: Long = 0,
    val downloadStamp: Long = System.currentTimeMillis(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadTaskData

        return downloadFileName == other.downloadFileName
    }

    override fun hashCode(): Int {
        return downloadFileName?.hashCode() ?: 0
    }
}
