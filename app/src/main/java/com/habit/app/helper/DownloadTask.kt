package com.habit.app.helper

import android.util.Log
import com.habit.app.data.DOWNLOADING_NAME_PREFIX
import com.habit.app.data.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadTask(
    private val client: OkHttpClient,
    private val url: String,
    val file: File,
    val contentDisposition: String?,
    val mimeType: String?,
    val totalSize: Long = 0L
) {
    var taskSign = System.currentTimeMillis()
        private set
    var curDownloadedPercent: Int = 0
        private set
    var curDownloadSpeed: Double = 0.0
        private set
    var curDownloadEta: Long = 0L
        private set
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    // 使用 MutableList 存储多个监听器
    private val onProgressListeners = mutableListOf<(taskSign: Long, url: String, downloaded: Long, total: Long, percent: Int, etaSeconds: Long, speed: Double, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit>()
    private val onCompletedListeners = mutableListOf<(taskSign: Long, url: String, total: Long, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit>()
    private val onErrorListeners = mutableListOf<(taskSign: Long, url: String, contentDisposition: String?, mimeType: String?, filePath: String, msg: String) -> Unit>()
    var taskReleaseCallback: (() -> Unit)? = null

    private var call: Call? = null
    private var isPaused = false
    private var isCanceled = false
    private var downloadedBytes: Long = 0L


    fun start() {
        resume()
    }

    fun pause() {
        isPaused = true
        call?.cancel()
    }

    fun cancel() {
        isCanceled = true
        call?.cancel()
        file.delete()

        // 清空监听器集合，避免内存泄漏
        onProgressListeners.clear()
        onCompletedListeners.clear()
        onErrorListeners.clear()
    }

    fun resume() {
        isPaused = false
        isCanceled = false

        downloadedBytes = if (file.exists()) file.length() else 0L

        val request = Request.Builder()
            .url(url)
            .addHeader("Range", "bytes=$downloadedBytes-")
            .addHeader("Accept", "*/*")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
            .build()

        call = client.newCall(request)
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!isPaused && !isCanceled) {
                    if (file.exists()) {
                        file.delete()
                    }
                    scope.launch {
                        Log.e(TAG, "error1 = ${e.message}")
                        notifyError(e.message ?: "error")
                    }
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    if (file.exists()) {
                        file.delete()
                    }
                    scope.launch {
                        Log.e(TAG, "error2 = ${response.code} -- ${response.message}")
                        notifyError("error：${response.code}")
                    }
                    return
                }

                val totalBytes = downloadedBytes + (response.body?.contentLength() ?: 0L)
                val inputStream = response.body?.byteStream() ?: return
                val outputStream = FileOutputStream(file, true)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var current = downloadedBytes
                val startTime = System.currentTimeMillis()
                var lastPercent = -1
                try {
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (isPaused || isCanceled) break
                        outputStream.write(buffer, 0, bytesRead)
                        current += bytesRead

                        curDownloadedPercent = (current * 100 / totalBytes).toInt()
                        val elapsed = System.currentTimeMillis() - startTime
                        val speed = current.toDouble() / (elapsed / 1000.0 + 1)
                        curDownloadEta = ((totalBytes - current) / speed).toLong()

                        if (curDownloadedPercent != lastPercent) {
                            lastPercent = curDownloadedPercent
                            curDownloadSpeed = speed + (100..314572).random()
                            scope.launch {
                                notifyProgress(current, totalBytes, curDownloadedPercent, curDownloadEta, curDownloadSpeed)
                            }
                        }
                    }
                    outputStream.flush()
                    if (!isPaused && !isCanceled) {
                        val resultFile = if (file.name.startsWith(DOWNLOADING_NAME_PREFIX)) {
                            val renamedFile = File(file.parent, file.name.replace(DOWNLOADING_NAME_PREFIX, ""))
                            if (file.renameTo(renamedFile)) renamedFile else file
                        } else {
                            file
                        }
                        scope.launch {
                            notifyCompleted(totalBytes, resultFile.path)
                        }
                        curDownloadedPercent = 100
                        curDownloadSpeed = 0.0
                        curDownloadEta = 0
                    }
                } catch (e: Exception) {
                    if (!isPaused && !isCanceled) {
                        if (file.exists()) {
                            file.delete()
                        }
                        scope.launch {
                            Log.e(TAG, "error3 = ${e.message}")
                            notifyError(e.message ?: "error")
                        }
                    }
                } finally {
                    inputStream.close()
                    outputStream.close()
                }
            }
        })
    }

    // 释放资源的方法
    fun release() {
        // 取消当前的下载任务
        isCanceled = true
        call?.cancel()
        scope.cancel()

        // 清空监听器集合，避免内存泄漏
        onProgressListeners.clear()
        onCompletedListeners.clear()
        onErrorListeners.clear()
    }

    // 添加监听器的方法
    fun addOnProgressListener(listener: (taskSign: Long, url: String, downloaded: Long, total: Long, percent: Int, etaSeconds: Long, speed: Double, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit) {
        onProgressListeners.add(listener)
    }

    fun addOnCompletedListener(listener: (taskSign: Long, url: String, total: Long, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit) {
        onCompletedListeners.add(listener)
    }

    fun addOnErrorListener(listener: (taskSign: Long, url: String, contentDisposition: String?, mimeType: String?, filePath: String, msg: String) -> Unit) {
        onErrorListeners.add(listener)
    }

    // 移除监听器的方法
    fun removeOnProgressListener(listener: (taskSign: Long, url: String, downloaded: Long, total: Long, percent: Int, etaSeconds: Long, speed: Double, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit) {
        onProgressListeners.remove(listener)
    }

    fun removeOnCompletedListener(listener: (taskSign: Long, url: String, total: Long, contentDisposition: String?, mimeType: String?, filePath: String) -> Unit) {
        onCompletedListeners.remove(listener)
    }

    fun removeOnErrorListener(listener: (taskSign: Long, url: String, contentDisposition: String?, mimeType: String?, filePath: String, msg: String) -> Unit) {
        onErrorListeners.remove(listener)
    }

    // 通知进度更新
    private fun notifyProgress(downloaded: Long, total: Long, percent: Int, eta: Long, speed: Double) {
        onProgressListeners.forEach { listener ->
            try {
                listener(taskSign, url, downloaded, total, percent, eta, speed, contentDisposition, mimeType, file.path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 通知下载完成
    private fun notifyCompleted(total: Long, filePath: String) {
        onCompletedListeners.forEach { listener ->
            try {
                listener(taskSign, url, total, contentDisposition, mimeType, filePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        taskReleaseCallback?.invoke()
    }

    // 通知下载错误
    private fun notifyError(message: String) {
        onErrorListeners.forEach { listener ->
            try {
                listener(taskSign, url, contentDisposition, mimeType, file.path, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        taskReleaseCallback?.invoke()
    }
}