package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.data.DOWNLOADING_NAME_PREFIX
import com.habit.app.data.TAG
import com.habit.app.data.model.DownloadFileData
import com.habit.app.data.model.DownloadItemPayload
import com.habit.app.databinding.ActivityFileDownloadBinding
import com.habit.app.helper.DownloadManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.item.DownloadFileItem
import com.habit.app.viewmodel.home.FileDownloadViewModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMFileUtil
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.immersiveWindow
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import java.io.File


class FileDownloadActivity : BaseActivity() {
    private lateinit var binding: ActivityFileDownloadBinding
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val viewModel: FileDownloadViewModel by viewModels()

    /**
     * item 回调
     */
    private val fileItemCallback = object : DownloadFileItem.FileItemCallback {
        override fun onDownloadPause(fileData: DownloadFileData) {

        }
        override fun onDownloadCancel(fileData: DownloadFileData) {

        }
        override fun onFileOpen(fileData: DownloadFileData) {

        }
        override fun onFileMenuClick(fileData: DownloadFileData, targetView: View) {

        }
    }

    private val progressCallback: (taskSign: Long, String, Long, Long, Int, Long, Double, String?, String?, String) -> Unit = { taskSign: Long, url: String, downloaded: Long, total: Long, percent: Int, eta: Long, speed: Double, _: String?, _: String?, filePath: String ->
        Log.d(TAG, "DownloadActivity onProgress 进度: $percent% (${EMUtil.formatBytesSize(downloaded)} / ${EMUtil.formatBytesSize(total)}), 预计剩余时间：$eta 秒")
        updateDownloadInfo(taskSign, url, percent, eta, speed, downloaded, filePath)
    }

    private val completeCallback: (taskSign: Long, String, Long, String?, String?, String) -> Unit = { taskSign: Long, url: String, _: Long, _: String?, _: String?, filePath: String ->
        Log.d(TAG, "DownloadActivity onCompleted 下载完成：$url")
        completeFileDownloading(taskSign, url, filePath)
    }

    private var errorCallback: (taskSign: Long, String, String?, String?, String, String) -> Unit = {taskSign: Long, url: String, contentDisposition: String?, mimeType: String?, filePath: String, msg: String ->
        Log.d(TAG, "DownloadActivity onError 下载失败：$msg")
//        // 删除原文件
//        mScope.launch(Dispatchers.IO) {
//            if (File(filePath).exists()) {
//                File(filePath).delete()
//            }
//            withContext(Dispatchers.Main) {
//                updateDownloadNum(getDownloadingItemsCount(), getDownloadedItems().size)
//            }
//        }
//        showFileErrorDialog(taskSign, url, contentDisposition, mimeType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)

        initView()
        setupObserver()
        initData()
        initListener()
    }

    private fun initView() {
        updateUiConfig()

        with(binding.recList) {
            itemAnimator = null
            adapter = mAdapter
            layoutManager = LinearLayoutManager(this@FileDownloadActivity)
        }
    }

    private fun initData() {
        updateDownloadList()
    }

    private fun setupObserver() {
        viewModel.emptyObserver.observe(this) { value ->
            binding.tvEmpty.isVisible = value
        }
    }

    private fun initListener() {
        binding.ivNaviBack.setOnClickListener {
            finish()
        }

    }

    private fun updateDownloadList() {
        val allItems = ArrayList<AbstractFlexibleItem<*>>()
        val downloadItems = getDownloadedItems()
        val downloadingItems = getDownloadingItems()
        allItems.addAll(downloadingItems)
        allItems.addAll(downloadItems)
        mAdapter.updateDataSet(allItems)
    }

    /**
     * 获取已下载的file
     */
    private fun getDownloadedItems(): ArrayList<AbstractFlexibleItem<*>> {
        val items = ArrayList<AbstractFlexibleItem<*>>()
        val downloadFileList = EMFileUtil.getDirFilesList(File(cacheDir, "downloads"),
            containerSubFile = false,
            containDir = false,
            containerHiddenFile = false
        ).sortedByDescending { it.lastModified() }
        downloadFileList.filter { !it.name.startsWith(DOWNLOADING_NAME_PREFIX) }.forEach { file ->
            Log.d(TAG, "获取已下载文件：${file.path}")
            DownloadFileData(file.name).apply {
                isDownloaded = true
                filePath = file.path
                fileModifyTime = file.lastModified()
                fileSize = file.length()
                fileType = UtilHelper.getFileTypeByName(file.name)

                items.add(DownloadFileItem(this@FileDownloadActivity, this, fileItemCallback))
            }
        }
        return items
    }

    private fun getDownloadingItems(): ArrayList<AbstractFlexibleItem<*>> {
        val items = ArrayList<AbstractFlexibleItem<*>>()
        val allTasks = DownloadManager.getAllTasks()
        allTasks.forEach { (url, downloadTask) ->
            val fileName = URLUtil.guessFileName(url, downloadTask.contentDisposition, downloadTask.mimeType)
            DownloadFileData(fileName).apply {
                isDownloaded = false
                fileSize = downloadTask.totalSize
                downloadUrl = url
                downloadProgress = downloadTask.curDownloadedPercent
                downloadEtaSeconds = downloadTask.curDownloadEta
                downloadSpeed = downloadTask.curDownloadSpeed
                fileType = UtilHelper.getFileTypeByName(fileName)
                fileModifyTime = downloadTask.taskSign
                items.add(DownloadFileItem(this@FileDownloadActivity, this, fileItemCallback))
            }
            // 设置监听
            downloadTask.removeOnProgressListener(progressCallback)
            downloadTask.removeOnCompletedListener(completeCallback)
            downloadTask.removeOnErrorListener(errorCallback)
            downloadTask.addOnProgressListener(progressCallback)
            downloadTask.addOnCompletedListener(completeCallback)
            downloadTask.addOnErrorListener(errorCallback)
        }
        return items
    }

    /**
     * 下载进度更新
     */
    private fun updateDownloadInfo(taskSign: Long, url: String, percent: Int, eta: Long, speed: Double, downloadSize: Long, downloadFilePath: String) {
        mAdapter.currentItems.filterIsInstance<DownloadFileItem>().forEach { item ->
            run {
                if (!item.fileData.isDownloaded
                    && item.fileData.fileModifyTime == taskSign
                    && item.fileData.downloadUrl == url) {

                    // 更新DownloadFileData信息
                    item.fileData.filePath = downloadFilePath
                    item.fileData.fileDownloadSize = downloadSize

                    mAdapter.updateItem(item, DownloadItemPayload(percent))
                    return@run
                }
            }
        }
    }

    /**
     * 结束下载
     */
    private fun completeFileDownloading(taskSign: Long, url: String, filePath: String) {
        run {
            for ((index, item) in mAdapter.currentItems.withIndex()) {
                if (item is DownloadFileItem && item.fileData.fileModifyTime == taskSign && item.fileData.downloadUrl == url) {
                    item.fileData.isDownloaded = true
                    mAdapter.updateItem(item)
                    Log.d(TAG, "结束下载, item更新")
                    return@run
                }
            }
        }
    }

    private fun updateUiConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.ivNaviBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_back))
        mAdapter.currentItems.forEach { item ->
            mAdapter.updateItem(item, "update")
        }
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUiConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, FileDownloadActivity::class.java))
        }
    }
}