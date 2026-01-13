package com.habit.app.helper

import android.util.Log
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.DownloadTaskData
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

    fun getExistTaskByFileName(fileName: String): DownloadTask? {
        return downloadTaskMap[fileName]
    }

    fun createAndStartDownloadTask(url: String,
                                   destFile: File,
                                   totalSize: Long,
                                   insertDB: Boolean = true,
                                   progressListener: (url: String, downloaded: Long, total: Long, percent: Int, fileName: String) -> Unit,
                                   completeListener: (url: String, total: Long, fileName: String, filePath: String) -> Unit,
                                   errorListener: (url: String, fileName: String, filePath: String, msg: String) -> Unit
    ) {
        val downloadTask = DownloadTask(client, url, destFile).apply {
            addOnProgressListener(progressListener)
            addOnCompletedListener(completeListener)
            addOnErrorListener(errorListener)
            this.taskReleaseCallback = {
                releaseDownloadTask(destFile.name)
            }
            start()
        }
        // 数据库插入task数据
        if (insertDB) {
            DBManager.getDao().insertDownloadTaskData(
                DownloadTaskData(
                    downloadUrl = url,
                    downloadFileName = destFile.name,
                    downloadFileSize = totalSize
                )
            )
        }
        downloadTaskMap[destFile.name] = downloadTask
    }

    /**
     * app重启后 根据task表恢复下载任务
     */
    fun resumeDownloadTask(destFile: File, dbFileData: DownloadTaskData): DownloadTask? {
        return DownloadTask(client, dbFileData.downloadUrl, destFile).apply {
            downloadTaskMap[destFile.name] = this
        }
    }


    /**
     * 释放 WebView（可用于 Activity 关闭时调用）
     */
    fun releaseDownloadTask(fileName: String) {
        downloadTaskMap[fileName]?.release()
        downloadTaskMap.remove(fileName)
    }

    fun releaseNetResource() {
        try {
            downloadTaskMap.forEach { (fileName, downloadTask) ->
                downloadTask.release()
                downloadTask.cancel()
                downloadTaskMap.remove(fileName)
            }
            // 关闭 OkHttpClient
            client.dispatcher.executorService.shutdown()
            client.connectionPool.evictAll()
        } catch (e: Exception) {
            Log.e(TAG, "释放网络资源异常：${e.message}")
        }
    }
}