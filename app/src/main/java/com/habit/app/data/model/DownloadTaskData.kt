package com.habit.app.data.model

/**
 * 下载任务数据
 */
data class DownloadTaskData(
    val downloadId: Int = 0,
    val downloadUrl: String = "",
    val downloadFileName: String? = null,
    val downloadFileSize: Long = 0,
)
