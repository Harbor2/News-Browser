package com.habit.app.ui.home.controller

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.data.DOWNLOADING_NAME_PREFIX
import com.habit.app.data.ENGINE_BAIDU
import com.habit.app.data.ENGINE_BAIDU_URL
import com.habit.app.data.ENGINE_BING
import com.habit.app.data.ENGINE_BING_URL
import com.habit.app.data.ENGINE_DUCKDUCK
import com.habit.app.data.ENGINE_DUCKDUCKGO_URL
import com.habit.app.data.ENGINE_GOOGLE
import com.habit.app.data.ENGINE_GOOGLE_URL
import com.habit.app.data.ENGINE_YAHOO
import com.habit.app.data.ENGINE_YAHOO_URL
import com.habit.app.data.ENGINE_YANDEX
import com.habit.app.data.ENGINE_YANDEX_URL
import com.habit.app.data.MAX_COUNT_DOWNLOAD_TASKS
import com.habit.app.data.MAX_SNAP_COUNT
import com.habit.app.data.TAG
import com.habit.app.data.USER_AGENT_DESKTOP
import com.habit.app.data.USER_AGENT_PHONE
import com.habit.app.data.WEBVIEW_DEFAULT_NAME
import com.habit.app.data.assessUrlList
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.HistoryData
import com.habit.app.data.model.WebViewData
import com.habit.app.databinding.ActivityMainBinding
import com.habit.app.event.HomeAccessUpdateEvent
import com.habit.app.helper.DownloadManager
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.UtilHelper
import com.habit.app.helper.WebViewManager
import com.habit.app.ui.MainActivity
import com.habit.app.ui.custom.CustomWebView
import com.habit.app.ui.dialog.DeleteConfirmDialog
import com.habit.app.ui.dialog.FileDownloadDialog
import com.habit.app.ui.dialog.NavigationEditDialog
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.net.URLEncoder

class MainController(
    val activity: MainActivity,
    val viewModel: MainActivityModel,
    val binding: ActivityMainBinding
) {

    /**
     * 当前搜索内容
     */
    var mCurInputStr = ""
    var mCurWebView: WebView? = null
    var mCurWebSign: String = ""

    private var webScrollCallback: ((Boolean) -> Unit) = { isUpScroll -> }

    private var mNaviEditDialog: NavigationEditDialog? = null
    private var mPreDownloadDialog: FileDownloadDialog? = null
    private var mDownloadFailedDialog: DeleteConfirmDialog? = null

    /**
     * webView加载进度监听
     */
    private val webProgressListener = object : WebChromeClient() {
        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            super.onReceivedIcon(view, icon)
            Log.d(TAG, "网页icon加载完成")
            view?.setTag(R.id.web_small_icon, icon)
            // 保存搜索历史
            saveSearchHistory(view, icon)
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            Log.d(TAG, "网页标题加载完成：$title")
            view?.setTag(R.id.web_title, title)
            view?.url?.let {
                if (!binding.editInput.hasFocus()) {
                    binding.editInput.setText(it)
                }
            }
            updateGoBackStatus()
        }
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            val curProgress = binding.topProgress.progress
            if (curProgress != newProgress) {
                binding.topProgress.progress = newProgress
            }
            binding.topProgress.isInvisible = newProgress == 100
        }
    }

    /**
     * webView下载监听
     */
    private val webDownloadListener = object : DownloadListener {
        override fun onDownloadStart(
            url: String?,
            userAgent: String?,
            contentDisposition: String?,
            mimeType: String?,
            contentLength: Long
        ) {
            if (url.isNullOrEmpty()) {
                UtilHelper.showToast(activity, activity.getString(R.string.toast_url_invalid))
                return
            }
            if (DownloadManager.getTaskCount() >= MAX_COUNT_DOWNLOAD_TASKS) {
                UtilHelper.showToast(activity, activity.getString(R.string.toast_download_tasks_max))
                return
            }

            UtilHelper.showToast(activity, "准备下载app")
            showFileDownloadDialog(url, contentDisposition, mimeType, contentLength)
        }
    }

    /**
     * 下载进度回调
     */
    private val progressCallback: (url: String, downloaded: Long, total: Long, percent: Int, fileName: String) -> Unit = {url: String, downloaded: Long, total: Long, percent: Int, fileName: String ->
        Log.d(TAG, "onProgress 下载进度, fileName: $fileName, 进度: $percent% (${EMUtil.formatBytesSize(downloaded)} / ${EMUtil.formatBytesSize(total)})")
    }

    /**
     * 下载完成回调
     */
    private val completeCallback: (url: String, total: Long, fileName: String) -> Unit = {url: String, total: Long, fileName: String ->
        Log.d(TAG, "onCompleted 下载完成, fileName: $fileName, url: $url")
        UtilHelper.showToast(activity, activity.getString(R.string.text_download_completed, fileName))
        DBManager.getDao().deleteDownloadTaskData(fileName)
    }

    /**
     * 下载失败回调
     */
    private var errorCallback: (url: String, fileName: String, filePath: String, msg: String) -> Unit = {url: String, fileName: String, filePath: String, msg: String ->
        Log.d(TAG, "onError 下载失败, fileName: $fileName, filePath: $filePath, msg: $msg")
        showFileErrorDialog(fileName, filePath)
    }

    /**
     * 初始化时，创建新tab
     */
    fun createNewWebTabAndInsertDB(privacyMode: Boolean? = null) {
        // 插入数据库新快照
        val newTabSign = System.currentTimeMillis().toString()
        val newTabViewData = WebViewData(
            WEBVIEW_DEFAULT_NAME,
            newTabSign,
            "",
            true,
            privacyMode ?: false,
            "",
            ""
        )
        Log.d(TAG, "插入WebSnapData")
        DBManager.getDao().insertWebSnapToTable(newTabViewData)

        mCurWebSign = newTabSign
        mCurWebView = null
        viewModel.setPhoneModeObserver(true)
        viewModel.setPrivacyObserver(privacyMode ?: false)
    }

    /**
     * 保存当前web快照，创建新tab
     */
    fun saveCurSnapAndCreateNewWebTab() {
        if (DBManager.getDao().getWebSnapsCount(viewModel.privacyObserver.value!!) >= MAX_SNAP_COUNT) {
            UtilHelper.showToast(activity, activity.getString(R.string.toast_snap_max_count))
           return
        }
        // 保存当前快照
        createWebViewSnapshot { webViewData ->
            if (webViewData != null) {
                Log.d(TAG, "更新 WebSnapData: 保存当前快照并创建新tab")
                DBManager.getDao().updateWebSnapItem(webViewData)
            }
            // 插入数据库新快照
            val newTabSign = System.currentTimeMillis().toString()
            val newTabViewData = WebViewData(
                WEBVIEW_DEFAULT_NAME,
                newTabSign,
                "",
                true,
                viewModel.privacyObserver.value!!,
                "",
                ""
            )
            Log.d(TAG, "插入 WebSnapData")
            DBManager.getDao().insertWebSnapToTable(newTabViewData)

            mCurWebSign = newTabSign
            mCurWebView = null
            viewModel.setPhoneModeObserver(true)
            // 首页
            viewModel.setSearchObserver(false)
            updateTabsCount()
        }
    }

    /**
     * 处理web搜索
     */
    fun processWebSearch(searchStr: String, recordSearch: Boolean = false) {
        if (searchStr.isEmpty()) {
            hideSoftKeyBoard()
            return
        }
        if (recordSearch) {
            Log.d(TAG, "记录搜索记录：$searchStr")
            // 记录搜索记录
            DBManager.getDao().insertSearchRecord(searchStr)
        }

        mCurInputStr = searchStr
        // 检查webView
        if (mCurWebView == null && mCurWebSign.isNotEmpty()) {
            // 搜索时创建webView
            updateWebView()
        } else {
            viewModel.setSearchObserver(true)
        }

        // 直接打开链接
        if (assessUrlList.contains(mCurInputStr) || mCurInputStr.startsWith("http://") || mCurInputStr.startsWith("https://")) {
            Log.d(TAG, "直接打开网页链接：$mCurInputStr")
            binding.editInput.setText(mCurInputStr)
            mCurWebView?.loadUrl(mCurInputStr)
            hideSoftKeyBoard()
            return
        }

        val searchUrlStart =
            when (KeyValueManager.getValueByKey(KeyValueManager.KEY_ENGINE_SELECT) ?: ENGINE_GOOGLE) {
                ENGINE_GOOGLE -> ENGINE_GOOGLE_URL
                ENGINE_YAHOO -> ENGINE_YAHOO_URL
                ENGINE_DUCKDUCK -> ENGINE_DUCKDUCKGO_URL
                ENGINE_BING -> ENGINE_BING_URL
                ENGINE_YANDEX -> ENGINE_YANDEX_URL
                ENGINE_BAIDU -> ENGINE_BAIDU_URL
                else -> ENGINE_GOOGLE_URL
            }
        val searchStr =
            String.format("%s%s", searchUrlStart, URLEncoder.encode(mCurInputStr, "UTF-8"))
        Log.d(TAG, "网页搜索链接内容：$searchStr")
        binding.editInput.setText(searchStr)
        mCurWebView?.loadUrl(searchStr)
        // 最后关闭搜索框
        hideSoftKeyBoard()
    }

    /**
     * 配置webView
     */
    fun updateWebView(dbWebData: WebViewData? = null) {
        Log.d(TAG, "webView装填")
        // 尝试获取webView
        mCurWebView = WebViewManager.getWebView(activity, mCurWebSign, dbWebData?.url, dbWebData?.isPhoneMode ?: true).apply {
            Log.d(TAG, "配置webView")
            // 手机 电脑模式
            mCurWebView?.settings?.userAgentString = if (viewModel.phoneModeObserver.value!!) USER_AGENT_PHONE else USER_AGENT_DESKTOP
            mCurWebView?.settings?.builtInZoomControls = !viewModel.phoneModeObserver.value!!
            // WebView内部加载
            webViewClient = CustomWebViewClient()
            // 监听webView加载状态
            webChromeClient = webProgressListener
            // 设置文件下载监听
            setDownloadListener(webDownloadListener)
            (this as? CustomWebView)?.mScrollBack = webScrollCallback
            // 获取焦点
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus(View.FOCUS_DOWN)

            // web装填
            if (this.parent != null) {
                (this.parent as ViewGroup).removeAllViews()
            }
            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            binding.containerWeb.removeAllViews()
            binding.containerWeb.addView(this, params)
        }
        updateGoBackStatus()
    }


    fun updateTabsCount() {
        val tagCount = DBManager.getDao().getWebSnapsCount(viewModel.privacyObserver.value!!).toString()
        binding.tvBottomMainTabNum.text = tagCount
        binding.tvBottomSearchTabNum.text = tagCount
    }

    fun refreshWebView() {
        mCurWebView?.let {
            if (binding.ivNaviPageRefresh.alpha != 1f) return
            it.reload()
            Log.d(TAG, "webView重新加载")
        }
    }

    /*
     * ************************  main调用 ************************
     */

    /**
     * 更新当前snap
     */
    fun updateCurWebSnap(callback: () -> Unit) {
        createWebViewSnapshot { webViewData ->
            if (webViewData != null) {
                // 更新封面
                Log.d(TAG, "更新 WebSnapData: 仅更新")
                DBManager.getDao().updateWebSnapItem(webViewData)
                callback.invoke()
            }
        }
    }

    /**
     * 打开新的标签页并搜素
     */
    fun openNewSnapAndSearch(postUrl: String) {
        createWebViewSnapshot { webViewData ->
            if (webViewData != null) {
                // 更新封面
                Log.d(TAG, "更新 WebSnapData: open new tab search")
                DBManager.getDao().updateWebSnapItem(webViewData)
            }
            // 创建新的webview
            createNewWebTabAndInsertDB(viewModel.privacyObserver.value!!)
            // search
            processWebSearch(postUrl)
        }
    }

    /**
     * 添加到首页导航栏
     */
    fun processNavigationAdd() {
        if (mCurWebView == null) {
            UtilHelper.showToast(activity, activity.getString(R.string.toast_failed))
            return
        }

        val webTitle = mCurWebView!!.getTag(R.id.web_title) as? String ?: (mCurWebView!!.url ?: "")
        val webUrl = mCurWebView!!.url ?: ""
        val iconBitmap = mCurWebView!!.getTag(R.id.web_small_icon) as? Bitmap
        val iconBitmapPath = if (iconBitmap == null) null else UtilHelper.writeBitmapToCache(activity, iconBitmap, "naviIcon")

        mNaviEditDialog = NavigationEditDialog.Companion.tryShowDialog(activity)?.apply {
            setData(iconBitmapPath, webTitle)
            setOnDismissListener {
                mNaviEditDialog = null
            }
            this.mCallback = { name ->
                val result = UtilHelper.homeAddAccessItem(activity, name, webUrl, iconBitmapPath)
                if (result) {
                    EventBus.getDefault().post(HomeAccessUpdateEvent())
                }
            }
        }
    }

    fun onPhoneModeChange(value: Boolean) {
        Log.d(TAG, "现在的模式：${if (value) "手机" else "桌面"}")
        // 重建当前webview
        mCurWebView?.let {
            val changedWebData = WebViewData(sign = mCurWebSign, isPhoneMode = value, url = it.url ?: "")
            WebViewManager.releaseWebView(mCurWebSign)
            mCurWebView = null
            updateWebView(changedWebData)
        }
    }

    fun onPrivacyModeChange(value: Boolean) {
        Log.d(TAG, "现在的隐私模式：$value")
        mCurWebSign.isNotEmpty().let {
            Log.d(TAG, "更新 WebSnapData: 隐私模式变化")
            DBManager.getDao().updateWebSnapItem(WebViewData(sign = mCurWebSign, isPrivacyMode = value))
        }
        binding.ivInputTrace.isVisible = value
        updateTabsCount()
    }

    /**
     * 处理webView内容搜索
     */
    fun processWebContentSearch(keyword: String) {
        if (mCurWebView == null) return
        mCurWebView!!.setFindListener { activeMatchOrdinal, numberOfMatches, isDoneCounting ->
            if (isDoneCounting) {
                binding.tvSearchNum1.text = if (numberOfMatches > 0) "${activeMatchOrdinal + 1}" else "$activeMatchOrdinal"
                binding.tvSearchNum2.text = "/$numberOfMatches"
            }
            binding.ivSearchPre.alpha = if (numberOfMatches > 0) 1f else 0.3f
            binding.ivSearchNext.alpha = if (numberOfMatches > 0) 1f else 0.3f
        }
        mCurWebView!!.findAllAsync(keyword)
    }

    fun exitWebContentSearch() {
        if (binding.containerContentSearch.isVisible) {
            binding.tvSearchNum1.text = "0"
            binding.tvSearchNum2.text = "/0"
            binding.editContentInput.text?.clear()
            mCurWebView?.clearMatches()
            binding.containerContentSearch.isVisible = false
        }
    }

    fun processWebContentSearchPreOrNext(isNext: Boolean) {
        if (mCurWebView == null) return
        if (!binding.containerContentSearch.isVisible) return
        mCurWebView!!.findNext(isNext)
    }


    /*
     * ************************  私有方法 ************************
     */

    /**
     * 文件下载dialog
     */
    private fun showFileDownloadDialog(
        url: String,
        contentDisposition: String?,
        mimeType: String?,
        contentLength: Long
    ) {
        val fileName = UtilHelper.decodeUrlCode(URLUtil.guessFileName(url, contentDisposition, mimeType))
        Log.d(TAG, "解析下载的文件名称：$fileName")
        mPreDownloadDialog = FileDownloadDialog.Companion.tryShowDialog(activity)?.apply {
            this.setData(fileName)
            this.mCallback = { beginDownload: Boolean ->
                if (beginDownload) {
                    val downloadDir = UtilHelper.getExternalFilesDownloadDir()
                    if (!downloadDir.exists()) downloadDir.mkdirs()
                    var destFile = File(downloadDir, fileName)
                    val downloadDestFile = File(downloadDir, DOWNLOADING_NAME_PREFIX.plus(fileName))
                    if (destFile.exists() || downloadDestFile.exists()) {
                        destFile = File(downloadDir, "${System.currentTimeMillis()}_$fileName")
                    }
                    // 文件重命名为下载中状态
                    val downloadFile = File(downloadDir, DOWNLOADING_NAME_PREFIX.plus(destFile.name))
                    DownloadManager.createAndStartDownloadTask(url, downloadFile, contentLength, progressCallback, completeCallback, errorCallback)
                }
            }
            setOnDismissListener {
                mPreDownloadDialog = null
            }
        }
    }

    private fun showFileErrorDialog(fileName: String, filePath: String) {
        mDownloadFailedDialog = DeleteConfirmDialog.Companion.tryShowDialog(activity)?.apply {
            this.initData(
                R.drawable.iv_download_failed_icon,
                activity.getString(R.string.text_download_failed, fileName),
                activity.getString(R.string.text_cancel),
                activity.getString(R.string.text_ok))
            setOnDismissListener {
                mDownloadFailedDialog = null
                this@MainController.activity.lifecycleScope.launch(Dispatchers.IO) {
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
     * 创建webView快照
     */
    fun createWebViewSnapshot(callback: (WebViewData?) -> Unit) {
        // 首页
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val coverBitmap = UtilHelper.getResizedBitmapFromView(binding.containerWeb)
            val coverBitmapPath = UtilHelper.writeBitmapToCache(activity, coverBitmap) ?: ""
            val iconBitmap = mCurWebView?.getTag(R.id.web_small_icon) as? Bitmap
            val iconBitmapPath = if (iconBitmap == null) "" else UtilHelper.writeBitmapToCache(activity, iconBitmap) ?: ""

            Log.d(TAG, "主页创建快照 webView：${mCurWebView?.hashCode()}")
            withContext(Dispatchers.Main) {
                val bitmapSnap = WebViewData(
                    mCurWebView?.getTag(R.id.web_title) as? String ?: ((mCurWebView?.url?.toString()?.trim() ?: "")),
                    mCurWebSign,
                    mCurWebView?.url?.toString()?.trim() ?: "",
                    viewModel.phoneModeObserver.value!!,
                    viewModel.privacyObserver.value!!,
                    coverBitmapPath,
                    iconBitmapPath
                )
                bitmapSnap.setCoverBitmap(coverBitmap)
                bitmapSnap.setWebIconBitmap(iconBitmap)
                callback.invoke(bitmapSnap)
            }
        }
    }

    fun hideSoftKeyBoard() {
        EMUtil.hideSoftKeyboard(binding.editInput, activity)
    }

    fun updateGoBackStatus() {
        mCurWebView?.let {
            binding.btnBottomBack.alpha = if (it.canGoBack()) 1f else 0.3f
            binding.btnBottomNext.alpha = if (it.canGoForward()) 1f else 0.3f
        }
    }

    fun updateUIConfig() {
        mNaviEditDialog?.updateThemeUI()
        mPreDownloadDialog?.updateThemeUI()
        mDownloadFailedDialog?.updateThemeUI()
    }

    /**
     * 历史记录防止多次回调保存多次
     */
    private var lastLoadUrlTimeStamp = 0L
    private var lastLoadUrl = ""

    /**
     * 保存搜索历史
     */
    private fun saveSearchHistory(wevView: WebView?, iconBitmap: Bitmap?) {
        if (wevView == null) return
        if (viewModel.privacyObserver.value!!) return

        val loadUrl = wevView.url ?: ""
        if (loadUrl.isEmpty()) return
        val curTimeStamp = System.currentTimeMillis()
        if (curTimeStamp - lastLoadUrlTimeStamp < 5000 && loadUrl == lastLoadUrl) return
        lastLoadUrlTimeStamp = System.currentTimeMillis()
        lastLoadUrl = loadUrl

        // 当前网页信息
        val titleStr = wevView.getTag(R.id.web_title) as? String ?: loadUrl
        val iconPathStr = if (iconBitmap == null) "" else UtilHelper.writeBitmapToCache(activity, iconBitmap) ?: ""

        // 保存网页信息
        activity.lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "保存搜索历史：$titleStr，$loadUrl，$iconPathStr，$curTimeStamp")
            DBManager.getDao().insertHistoryToTable(
                HistoryData(name = titleStr, url = loadUrl, webIconPath = iconPathStr, timeStamp = curTimeStamp)
            )
        }
    }

    inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (mCurWebView != view) return false
            Log.d(TAG, "shouldOverrideUrlLoading url：${request?.url?.toString()}")
            request?.url?.toString()?.let {
                if (!binding.editInput.hasFocus()) {
                    binding.editInput.setText(it)
                }
                updateGoBackStatus()
            }
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            if (mCurWebView != view) return
            viewModel.noNetObserver.value = false
            binding.ivNaviPageRefresh.alpha = 0.3f
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (mCurWebView != view) return
            if (url.isNullOrEmpty()) return
            Log.d(TAG, "onPageFinished 更新WebSnapData：onPageFinish")
            if (!binding.editInput.hasFocus()) {
                binding.editInput.setText(url)
            }
            binding.ivNaviPageRefresh.alpha = 1f
            DBManager.getDao().updateWebSnapItem(
                WebViewData(
                    name = view?.getTag(R.id.web_title) as? String ?: "",
                    sign = mCurWebSign,
                    url = url,
                    isPhoneMode = viewModel.phoneModeObserver.value!!,
                    isPrivacyMode = viewModel.privacyObserver.value!!,
                    "",
                    ""
                )
            )
            updateGoBackStatus()
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            if (mCurWebView != view) return
            if (request?.isForMainFrame == true) {
                // 无网处理
                if (!UtilHelper.isNetworkAvailable(activity)) {
                    viewModel.noNetObserver.value = true
                }
            }
        }
    }

}