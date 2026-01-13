package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.data.DOWNLOADING_NAME_PREFIX
import com.habit.app.data.MENU_DELETE
import com.habit.app.data.MENU_RENAME
import com.habit.app.data.MENU_SELECT
import com.habit.app.data.MENU_SHARE
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.DownloadFileData
import com.habit.app.data.model.DownloadItemPayload
import com.habit.app.databinding.ActivityFileDownloadBinding
import com.habit.app.helper.DownloadManager
import com.habit.app.helper.FileOpenUtil
import com.habit.app.helper.ShareUtils
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.dialog.DeleteConfirmDialog
import com.habit.app.ui.dialog.DownloadMenuFloat
import com.habit.app.ui.dialog.FileRenameDialog
import com.habit.app.ui.item.BookmarkHistoryTitleItem
import com.habit.app.ui.item.DownloadFileItem
import com.habit.app.viewmodel.home.FileDownloadViewModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMFileUtil
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.immersiveWindow
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class FileDownloadActivity : BaseActivity() {
    private lateinit var binding: ActivityFileDownloadBinding
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)
    private val viewModel: FileDownloadViewModel by viewModels()

    private var mDownloadFailedDialog: DeleteConfirmDialog? = null
    private var mRenameDialog: FileRenameDialog? = null
    private var mCancelDownloadDialog: DeleteConfirmDialog? = null
    private var mDeleteFileDialog: DeleteConfirmDialog? = null

    /**
     * item 回调
     */
    private val fileItemCallback = object : DownloadFileItem.FileItemCallback {
        override fun onDownloadPause(fileData: DownloadFileData) {
            Log.d(TAG, "下载暂停、继续: ${fileData.isPause}")
            val task = DownloadManager.getExistTaskByFileName(fileData.fileName)
            task?.let {
                if (fileData.isPause) {
                    it.pause()
                } else {
                    it.resume()
                }
            }
        }
        override fun onDownloadCancel(fileData: DownloadFileData) {
            showCancelDownloadDialog(fileData)
        }
        override fun onFileOpen(fileData: DownloadFileData) {
            openFile(fileData)
        }

        override fun onFileSelect(fileData: DownloadFileData) {
            val curSelectAll = checkSelectAll()
            if (viewModel.selectAllObserver.value!! != curSelectAll) {
                viewModel.setSelectAll(curSelectAll)
            }
        }
        override fun onFileMenuClick(fileData: DownloadFileData, targetView: View) {
            showMenu(targetView, fileData)
        }
    }

    /**
     * menu回调
     */
    private val downloadMenuCallback = object : DownloadMenuFloat.DownloadMenuCallback {
        override fun onOptionSelect(option: String, data: DownloadFileData) {
            processMenuOption(option, data)
        }
    }


    private val progressCallback: (String, Long, Long, Int, String) -> Unit = {url: String, downloaded: Long, total: Long, percent: Int, fileName: String ->
        Log.d(TAG, "DownloadActivity onProgress 下载进度, fileName: $fileName, 进度: $percent% (${EMUtil.formatBytesSize(downloaded)} / ${EMUtil.formatBytesSize(total)})")
        updateDownloadInfo(percent, fileName)
    }

    private val completeCallback: (String, Long, String, String) -> Unit = {url: String, total: Long, fileName: String, filePath: String ->
        Log.d(TAG, "DownloadActivity onCompleted 下载完成, fileName: $fileName")
        completeFileDownloading(fileName, total)
    }

    private var errorCallback: (String, String, String, String) -> Unit = {url: String, fileName: String, filePath: String, msg: String ->
        Log.d(TAG, "DownloadActivity onError 下载失败, fileName: $fileName, filePath: $filePath, msg: $msg")
        showFileErrorDialog(fileName, filePath)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, ThemeManager.isNightTheme(), binding.containerNavi)

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
        viewModel.loadingObserver.observe(this) { value ->
            binding.loadingView.isVisible = value
        }
        viewModel.editObserver.observe(this) { value ->
            binding.ivNaviClose.isVisible = value
            binding.ivNaviSelectAll.isVisible = value
            binding.ivNaviBack.isVisible = !value
            binding.tvTitle.isVisible = !value
            binding.containerBottomOption.isVisible = value
        }
        viewModel.selectAllObserver.observe(this) { value ->
            binding.ivNaviSelectAll.setImageResource(ThemeManager.getSkinImageResId(if (value) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
        }
    }

    private fun initListener() {
        binding.ivNaviBack.setOnClickListener {
            finish()
        }
        binding.ivNaviClose.setOnClickListener {
            viewModel.setEditObserver(false)
            changeSelectMode(false)
        }
        binding.ivNaviSelectAll.setOnClickListener {
            viewModel.setSelectAll(!viewModel.selectAllObserver.value!!)
            processSelectAllOrNot(viewModel.selectAllObserver.value!!)
        }

        binding.btnCancel.setOnClickListener {
            binding.ivNaviClose.performClick()
        }
        binding.btnDelete.setOnClickListener {
             showDeleteFileDialog()
        }
    }

    /**
     * 处理menu回调
     */
    private fun processMenuOption(option: String, data: DownloadFileData) {
        when (option) {
            MENU_SHARE -> {
                shareFile(data)
            }
            MENU_DELETE -> {
                showDeleteFileDialog(data)
            }
            MENU_RENAME -> {
                renameFile(data)
            }
            MENU_SELECT -> {
                viewModel.setEditObserver(true)
                changeSelectMode(true, data)
                val curSelectAll = checkSelectAll()
                if (viewModel.selectAllObserver.value!! != curSelectAll) {
                    viewModel.setSelectAll(curSelectAll)
                }
            }
        }
    }

    private fun updateDownloadList() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dataList = ArrayList<DownloadFileData>()
            dataList.addAll(getDownloadedItems())
            dataList.addAll(getDownloadingItems())
            // 排序
            val sortedList = dataList.sortedByDescending { it.fileModifyTime }
            withContext(Dispatchers.Main) {
                viewModel.loadingObserver.value = false
                val allItems = ArrayList<AbstractFlexibleItem<*>>()
                if (sortedList.isEmpty()) {
                    mAdapter.clear()
                    binding.recList.post {
                        binding.recList.isVisible = false
                        binding.tvEmpty.isVisible = true
                    }
                    return@withContext
                }

                binding.recList.isVisible = true
                binding.tvEmpty.isVisible = false
                sortedList.forEach { data ->
                    val timeItem = BookmarkHistoryTitleItem(data.getFormatData())
                    if (!allItems.contains(timeItem)) {
                        allItems.add(timeItem)
                    }
                    allItems.add(DownloadFileItem(this@FileDownloadActivity, data, fileItemCallback))
                }
                mAdapter.updateDataSet(allItems)
            }
        }
    }

    /**
     * 获取已下载的file
     */
    private fun getDownloadedItems(): ArrayList<DownloadFileData> {
        val dataList = ArrayList<DownloadFileData>()
        val downloadFileList = EMFileUtil.getDirFilesList(UtilHelper.getExternalFilesDownloadDir(),
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

                dataList.add(this)
            }
        }
        return dataList
    }

    private fun getDownloadingItems(): ArrayList<DownloadFileData> {
        val dataList = ArrayList<DownloadFileData>()
        val downloadingFileList = EMFileUtil.getDirFilesList(UtilHelper.getExternalFilesDownloadDir(),
            containerSubFile = false,
            containDir = false,
            containerHiddenFile = false
        )
        downloadingFileList.filter { it.name.startsWith(DOWNLOADING_NAME_PREFIX) }.forEach { file ->
            if (!file.exists()) return@forEach
            val dbFileData = DBManager.getDao().getDownloadTaskData(file.name) ?: return@forEach
            Log.d(TAG, "获取正在下载的文件：${file.path}")
            var existTask = DownloadManager.getExistTaskByFileName(file.name)
            if (existTask == null) {
                // 不存在，则为断点续传任务
                existTask = DownloadManager.resumeDownloadTask(file, dbFileData)
                if (existTask == null) {
                    // 任务初始化失败，删除对应文件
                    file.delete()
                    return@forEach
                }
            }
            existTask.apply {
                addOnProgressListener(progressCallback)
                addOnCompletedListener(completeCallback)
                addOnErrorListener(errorCallback)
                this.taskReleaseCallback = {
                    DownloadManager.releaseDownloadTask(file.name)
                }
            }

            DownloadFileData(file.name).apply {
                isDownloaded = false
                filePath = file.path
                fileModifyTime = dbFileData.downloadStamp
                fileSize = dbFileData.downloadFileSize
                fileType = UtilHelper.getFileTypeByName(file.name)
                downloadProgress = if(dbFileData.downloadFileSize == 0L) 0 else (file.length() * 100 / dbFileData.downloadFileSize).toInt()
                isPause = existTask.isPaused

                dataList.add(this)
            }
        }
        return dataList
    }

    /**
     * 下载进度更新
     */
    private fun updateDownloadInfo(percent: Int, fileName: String) {
        mAdapter.currentItems.filterIsInstance<DownloadFileItem>().forEach { item ->
            run {
                if (!item.fileData.isDownloaded
                    && item.fileData.fileName == fileName) {
                    // 更新DownloadFileData信息
                    item.fileData.downloadProgress = percent

                    mAdapter.updateItem(item, DownloadItemPayload(percent))
                    return@run
                }
            }
        }
    }

    /**
     * 完成下载
     */
    private fun completeFileDownloading(fileName: String, total: Long) {
        run {
            for ((index, item) in mAdapter.currentItems.withIndex()) {
                if (item is DownloadFileItem
                    && (item.fileData.fileName == fileName || item.fileData.fileName == DOWNLOADING_NAME_PREFIX.plus(fileName))) {
                    item.fileData.isDownloaded = true
                    item.fileData.fileName = if (item.fileData.fileName.startsWith(DOWNLOADING_NAME_PREFIX)) {
                        item.fileData.fileName.replace(DOWNLOADING_NAME_PREFIX, "")
                    } else {
                        item.fileData.fileName
                    }
                    item.fileData.filePath = item.fileData.filePath.replace(DOWNLOADING_NAME_PREFIX, "")
                    item.fileData.fileSize = total
                    mAdapter.updateItem(item)
                    return@run
                }
            }
        }
        DBManager.getDao().deleteDownloadTaskData(fileName)
    }

    private fun showFileErrorDialog(fileName: String, filePath: String) {
        mDownloadFailedDialog = DeleteConfirmDialog.tryShowDialog(this)?.apply {
            this.initData(
                R.drawable.iv_download_failed_icon,
                getString(R.string.text_download_failed, fileName),
                getString(R.string.text_cancel),
                getString(R.string.text_ok))
            setOnDismissListener {
                mDownloadFailedDialog = null
                this@FileDownloadActivity.lifecycleScope.launch(Dispatchers.IO) {
                    DBManager.getDao().deleteDownloadTaskData(fileName)
                    // 删除原文件
                    if (File(filePath).exists()) {
                        File(filePath).delete()
                    }
                }
            }
        }
    }

    /**
     * 进入 退出选择模式
     */
    private fun changeSelectMode(enter: Boolean, data: DownloadFileData? = null) {
        mAdapter.currentItems.filterIsInstance<DownloadFileItem>().forEach { item ->
            item.fileData.isSelect = if (enter) {
                item.fileData == data
            } else {
                null
            }
        }
        mAdapter.updateDataSet(mAdapter.currentItems)
    }

    /**
     * 检查是否全选
     */
    private fun checkSelectAll(): Boolean {
        mAdapter.currentItems.filterIsInstance<DownloadFileItem>().forEach { item ->
            if (item.fileData.isSelect != true) {
                return false
            }
        }
        return true
    }

    /**
     * 处理全选 或 取消全选
     */
    private fun processSelectAllOrNot(selectAll: Boolean) {
        mAdapter.currentItems.filterIsInstance<DownloadFileItem>().forEach { item ->
            item.fileData.isSelect = selectAll
        }
        mAdapter.updateDataSet(mAdapter.currentItems)
    }

    /**
     * 获取选中的数据
     */
    private fun getSelectedData(): List<DownloadFileData> {
        return mAdapter.currentItems.filterIsInstance<DownloadFileItem>().filter { it.fileData.isSelect == true }.map { it.fileData }
    }

    /**
     * 处理用户取消下载
     */
    private fun processDownloadCancel(fileData: DownloadFileData) {
        DownloadManager.releaseDownloadTask(fileData.fileName)
        // 删除数据库download
        DBManager.getDao().deleteDownloadTaskData(fileData.fileName)
        // 删除目标文件
        lifecycleScope.launch(Dispatchers.IO) {
            val targetFile = File(fileData.filePath)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }
        // 重新加载列表
        updateDownloadList()
    }

    private fun openFile(data: DownloadFileData) {
        FileOpenUtil.openFile(this, File(data.filePath))
    }

    private fun shareFile(data: DownloadFileData) {
        ShareUtils.shareSingleFile(this, File(data.filePath))
    }

    private fun deleteFiles(fileData: DownloadFileData? = null) {
        val selectData = if (fileData != null) {
            listOf(fileData)
        } else {
            getSelectedData()
        }
        if (selectData.isEmpty()) {
            UtilHelper.showToast(this, getString(R.string.toast_you_must_select_one))
            return
        }

        viewModel.loadingObserver.value = true
        lifecycleScope.launch(Dispatchers.IO) {
            val downloadingData = selectData.filter { !it.isDownloaded }
            val downloadedData = selectData.filter { it.isDownloaded }

            downloadedData.forEach {
                val targetFile = File(it.filePath)
                if (targetFile.exists()) {
                    targetFile.delete()
                }
            }
            downloadingData.forEach {
                val targetFile = File(it.filePath)
                if (targetFile.exists()) {
                    targetFile.delete()
                    DBManager.getDao().deleteDownloadTaskData(it.fileName)
                    DownloadManager.releaseDownloadTask(it.fileName)
                } else {
                    // 可能下载完成
                    val backstopFile = File(it.filePath.replace(DOWNLOADING_NAME_PREFIX, ""))
                    if (backstopFile.exists()) {
                        Log.d(TAG, "下载过程中进入，删除时下载完成，兜底删除文件：${it.filePath}")
                        backstopFile.delete()
                    }
                }
            }

            withContext(Dispatchers.Main) {
                updateDownloadList()
                viewModel.setEditObserver(false)
                viewModel.setSelectAll(false)
            }
        }
    }

    private fun renameFile(data: DownloadFileData) {
        mRenameDialog = FileRenameDialog.tryShowDialog(this)?.apply {
            setData(data)
            this.mCallback = { newData ->
                run {
                    mAdapter.currentItems.filterIsInstance<DownloadFileItem>().forEach { item ->
                        if (item.fileData.fileName == data.fileName) {
                            item.fileData = newData
                            mAdapter.updateItem(item)
                            return@run
                        }
                    }
                }
            }
            setOnDismissListener {
                mRenameDialog = null
            }
        }
    }

    private fun showCancelDownloadDialog(fileData: DownloadFileData) {
        mCancelDownloadDialog = DeleteConfirmDialog.tryShowDialog(this)?.apply {
            this.initData(
                R.drawable.iv_dialog_delete_icon,
                getString(R.string.text_cancel_download_title),
                getString(R.string.text_cancel),
                getString(R.string.text_sure)
            )
            setOnDismissListener {
                mCancelDownloadDialog = null
            }
            this.mCallback = { result ->
                if (result) {
                    processDownloadCancel(fileData)
                }
            }
        }
    }

    private fun showDeleteFileDialog(data: DownloadFileData? = null) {
        mDeleteFileDialog = DeleteConfirmDialog.tryShowDialog(this)?.apply {
            this.initData(
                R.drawable.iv_dialog_delete_icon,
                getString(R.string.text_delete_file_title),
                getString(R.string.text_cancel),
                getString(R.string.text_delete)
            )
            setOnDismissListener {
                mDeleteFileDialog = null
            }
            this.mCallback = { result ->
                if (result) {
                    deleteFiles(data)
                }
            }
        }
    }

    private fun showMenu(anchorView: View, payload: DownloadFileData) {
        DownloadMenuFloat(this).setData(payload).setCallback(downloadMenuCallback).show(anchorView)
    }

    private fun updateUiConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.ivNaviBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_back))
        binding.ivNaviClose.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_close))
        binding.ivNaviSelectAll.setImageResource(ThemeManager.getSkinImageResId(if (viewModel.selectAllObserver.value!!) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
        EMManager.from(binding.btnCancel)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        EMManager.from(binding.btnDelete)
            .setCorner(12f)
            .setBackGroundColor("#FF1B0B")
        binding.btnCancel.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_60))
        mAdapter.currentItems.forEach { item ->
            mAdapter.updateItem(item)
        }
        binding.cardLoading.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        binding.tvEmpty.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        mDownloadFailedDialog?.updateThemeUI()
        mRenameDialog?.updateThemeUI()
        mCancelDownloadDialog?.updateThemeUI()
        mDeleteFileDialog?.updateThemeUI()
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