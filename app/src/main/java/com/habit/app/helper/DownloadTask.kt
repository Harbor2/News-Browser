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
) {
    var curDownloadedPercent: Int = 0
        private set
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    // 使用 MutableList 存储多个监听器
    private val onProgressListeners = mutableListOf<(url: String, downloaded: Long, total: Long, percent: Int, fileName: String) -> Unit>()
    private val onCompletedListeners = mutableListOf<(url: String, total: Long, fileName: String) -> Unit>()
    private val onErrorListeners = mutableListOf<(url: String, fileName: String, filePath: String, msg: String) -> Unit>()
    var taskReleaseCallback: (() -> Unit)? = null

    private var call: Call? = null
    var isPaused = true
        private set
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

                        if (curDownloadedPercent != lastPercent) {
                            lastPercent = curDownloadedPercent
                            scope.launch {
                                notifyProgress(current, totalBytes, curDownloadedPercent)
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
                            notifyCompleted(totalBytes, resultFile.name)
                        }
                        curDownloadedPercent = 100
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
    fun addOnProgressListener(listener: (url: String, downloaded: Long, total: Long, percent: Int, fileName: String) -> Unit) {
        onProgressListeners.add(listener)
    }

    fun addOnCompletedListener(listener: (url: String, total: Long, fileName: String) -> Unit) {
        onCompletedListeners.add(listener)
    }

    fun addOnErrorListener(listener: (url: String, fileName: String, filePath: String, msg: String) -> Unit) {
        onErrorListeners.add(listener)
    }

    // 移除监听器的方法
    fun removeOnProgressListener(listener: (url: String, downloaded: Long, total: Long, percent: Int, filePath: String) -> Unit) {
        onProgressListeners.remove(listener)
    }

    fun removeOnCompletedListener(listener: (url: String, total: Long, filePath: String) -> Unit) {
        onCompletedListeners.remove(listener)
    }

    fun removeOnErrorListener(listener: (url: String, fileName: String, filePath: String, msg: String) -> Unit) {
        onErrorListeners.remove(listener)
    }

    // 通知进度更新
    private fun notifyProgress(downloaded: Long, total: Long, percent: Int) {
        onProgressListeners.forEach { listener ->
            try {
                listener(url, downloaded, total, percent, file.name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 通知下载完成
    private fun notifyCompleted(total: Long, fileName: String) {
        onCompletedListeners.forEach { listener ->
            try {
                listener(url, total, fileName)
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
                listener(url, file.name, file.path, message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        taskReleaseCallback?.invoke()
    }
}