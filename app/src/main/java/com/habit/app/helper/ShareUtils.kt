package com.habit.app.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.habit.app.BuildConfig
import com.habit.app.R
import com.habit.app.data.model.DownloadFileData
import java.io.File

object ShareUtils {

    /**
     * 分享多个文件
     */
    @JvmName("shareFilesByPath")
    fun shareFiles(context: Context, filePathList: List<String>) {
        val fileList = arrayListOf<File>()
        filePathList.forEach {
            fileList.add(File(it))
        }
        shareFiles(context, fileList)
    }

    /**
     * 分享多个文件
     */
    @JvmName("shareFilesByFile")
    fun shareFiles(context: Context, fileList: List<File>) {
        val uriList: ArrayList<Uri> = arrayListOf()
        fileList.forEach {
            if (it.exists()) {
                uriList.add(
                    FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", it)
                )
            }
        }
        if (uriList.isEmpty()) {
            return
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        intent.setType("*/*")
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        context.startActivity(intent)
    }

    /**
     * 分享单个文件
     */
    fun shareSingleFile(context: Context, filePath: String) {
        val targetFile = File(filePath)
        shareSingleFile(context, targetFile)
    }

    /**
     * 分享单个文件
     */
    fun shareSingleFile(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, context.getString(R.string.toast_file_not_exist), Toast.LENGTH_SHORT).show()
            return
        }
        when(UtilHelper.getFileTypeByName(file.name)) {
            DownloadFileData.TYPE_PIC -> {
                shareImageFile(context, file)
            }
            DownloadFileData.TYPE_VIDEO -> {
                shareVideoFile(context, file)
            }
            DownloadFileData.TYPE_AUDIO -> {
                shareAudioFile(context, file)
            }
            DownloadFileData.TYPE_DOC -> {
                shareDocFile(context ,file)
            }
            DownloadFileData.TYPE_APK -> {
                shareApkFile(context, file)
            }
            DownloadFileData.TYPE_ZIP -> {
                shareZipFile(context, file)
            }
            else -> shareUnknownFile(context, file)
        }
    }

    private fun shareApkFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.type = "application/vnd.android.package-archive"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun shareAudioFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.type = "audio/*"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun shareVideoFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.type = "video/*"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun shareImageFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun shareZipFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri =
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

        val extensionMap = mapOf(
            "zip" to "application/zip",
            "rar" to "application/vnd.rar",
            "7z" to "application/x-7z-compressed",
            "gz" to "application/gzip",
            "tar" to "application/x-tar",
            "bz" to "application/x-bzip",
            "bz2" to "application/x-bzip2"
        )

        val shareType = extensionMap[file.extension.lowercase()] ?: "application/*"
        intent.type = shareType
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun shareDocFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)

        val extensionMap = mapOf(
            "txt" to "text/plain",
            "pdf" to "application/pdf",
            "doc" to "application/msword",
            "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "dotm" to "application/vnd.ms-word.template.macroenabled.12",
            "dotx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
            "xls" to "application/vnd.ms-excel",
            "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "xlsm" to "application/vnd.ms-excel.sheet.macroenabled.12",
            "xltm" to "application/vnd.ms-excel.template.macroenabled.12",
            "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "ppt" to "application/vnd.ms-powerpoint"
        )

        val extension = file.extension.lowercase()
        val shareType = extensionMap[extension] ?: "application/*"
        intent.type = shareType
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }

    private fun shareUnknownFile(context: Context, file: File) {
        val intent = Intent(Intent.ACTION_SEND)
        val fileUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share File"))
    }
}