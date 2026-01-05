package com.habit.app.helper

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.habit.app.BuildConfig
import com.habit.app.R
import com.habit.app.data.model.DownloadFileData
import java.io.File

/**
 * 调起系统api打开文件
 */
object FileOpenUtil {

    fun openFile(context: Context, file: File): Boolean {
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.toast_file_not_exist), Toast.LENGTH_SHORT).show()
            return false
        }
        when(UtilHelper.getFileTypeByName(file.name)) {
            DownloadFileData.TYPE_PIC -> {
                openImageFile(context, file)
            }
            DownloadFileData.TYPE_VIDEO -> {
                openVideoFile(context, file)
            }
            DownloadFileData.TYPE_AUDIO -> {
                openAudioFile(context, file)
            }
            DownloadFileData.TYPE_DOC -> {
                openDocFile(context, file)
            }
            DownloadFileData.TYPE_APK -> {
                openApkFile(context, file)
            }
            DownloadFileData.TYPE_ZIP -> {
                openZipFile(context, file)
            }
            else -> {
                openUnknownFile(context, file)
            }
        }
        return true
    }

    private fun openUnknownFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.setDataAndType(fileUri, "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // 检查是否有应用可以处理这个 Intent
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, context.getString(R.string.toast_can_not_open_file), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImageFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.setDataAndType(fileUri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun openVideoFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.setDataAndType(fileUri, "video/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun openAudioFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.setDataAndType(fileUri, "audio/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun openApkFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun openZipFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        when {
            file.path.lowercase().endsWith(".zip") -> {
                intent.setDataAndType(fileUri, "application/zip")
            }
            file.path.lowercase().endsWith(".rar") -> {
                intent.setDataAndType(fileUri, "application/vnd.rar")
            }
            file.path.lowercase().endsWith(".7z") -> {
                intent.setDataAndType(fileUri, "application/x-7z-compressed")
            }
            file.path.lowercase().endsWith(".gz") -> {
                intent.setDataAndType(fileUri, "application/gzip")
            }
            file.path.lowercase().endsWith(".tar") -> {
                intent.setDataAndType(fileUri, "application/x-tar")
            }
            file.path.lowercase().endsWith(".bz") -> {
                intent.setDataAndType(fileUri, "application/x-bzip")
            }
            file.path.lowercase().endsWith(".bz2") -> {
                intent.setDataAndType(fileUri, "application/x-bzip2")
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    private fun openDocFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        when {
            file.path.lowercase().endsWith(".txt") -> {
                intent.setDataAndType(fileUri, "text/plain")
            }
            file.path.lowercase().endsWith(".pdf") -> {
                intent.setDataAndType(fileUri, "application/pdf")
            }
            file.path.lowercase().endsWith(".doc") -> {
                intent.setDataAndType(fileUri, "application/msword")
            }
            file.path.lowercase().endsWith(".docx") -> {
                intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            }
            file.path.lowercase().endsWith(".xls") -> {
                intent.setDataAndType(fileUri, "application/vnd.ms-excel")
            }
            file.path.lowercase().endsWith(".xlsx") -> {
                intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

}