package com.habit.app.helper

import android.util.Log
import com.habit.app.data.TAG
import okhttp3.OkHttpClient
import java.io.File

object DownloadManager {
    private val downloadTaskMap = LinkedHashMap<String, DownloadTask>()
    private val client = OkHttpClient.Builder().build()

    fun getAllTasks(): LinkedHashMap<String, DownloadTask> {
        return downloadTaskMap
    }

    fun getTaskCount(): Int {
        return downloadTaskMap.size
    }

    fun createAndStartDownloadTask(url: String,
                                   destFile: File,
                                   contentDisposition: String?,
                                   mimeType: String?,
                                   totalSize: Long,
                                   progressListener: (taskSign: Long, url: String, downloaded: Long, total: Long, percent: Int, etaSeconds: Long, speed: Double, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit,
                                   completeListener: (taskSign: Long, url: String, total: Long, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit,
                                   errorListener: (taskSign: Long, url: String, contentDisposition: String?, mimeType: String?, filePath: String, msg: String) -> Unit
                                   ) {
        val downloadTask = DownloadTask(client, url, destFile, contentDisposition, mimeType, totalSize).apply {
            addOnProgressListener(progressListener)
            addOnCompletedListener(completeListener)
            addOnErrorListener(errorListener)
            this.taskReleaseCallback = {
                releaseDownloadTask(url)
            }
            start()
        }
        downloadTaskMap[url] = downloadTask
    }


    /**
     * 释放 WebView（可用于 Activity 关闭时调用）
     */
    fun releaseDownloadTask(url: String) {
        downloadTaskMap[url]?.release()
        downloadTaskMap.remove(url)
    }

    fun releaseNetResource() {
        try {
            downloadTaskMap.forEach { (url, downloadTask) ->
                downloadTask.release()
                downloadTask.cancel()
                downloadTaskMap.remove(url)
            }
            // 关闭 OkHttpClient
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        } catch (e: Exception) {
            Log.e(TAG, "释放网络资源异常：${e.message}")
        }
    }
}