package com.habit.app.ui

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.DownloadListener
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
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
import com.habit.app.data.TAG
import com.habit.app.data.USER_AGENT_DESKTOP
import com.habit.app.data.USER_AGENT_PHONE
import com.habit.app.data.WEBVIEW_DEFAULT_NAME
import com.habit.app.data.assessUrlList
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.WebViewData
import com.habit.app.databinding.ActivityMainBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.UtilHelper
import com.habit.app.helper.WebViewManager
import com.habit.app.ui.custom.CustomWebView
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.util.EMUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    /**
     * webView加载进度监听
     */
    private val webProgressListener = object : WebChromeClient() {
        override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
            super.onReceivedIcon(view, icon)
            view?.setTag(R.id.web_small_icon, icon)
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            Log.d(TAG, "当前webView更新：$title, url:${view?.url}")
            view?.setTag(R.id.web_title, title)
            view?.url?.let {
                binding.editInput.setText(it)
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

        }
    }

    /**
     * 初始化时，获取最新的tab数据并使用
     */
    fun getDBLastSnapAndNewTab() {
        val lastWebData = DBManager.getDao().getWebSnapsFromTable().firstOrNull()
        if (lastWebData == null) {
            createNewWebTabAndInsertDB()
            return
        }
        // 有历史tab
        mCurWebSign = lastWebData.sign
        mCurWebView = null
        viewModel.setPhoneModeObserver(lastWebData.isPhoneMode ?: true)
        viewModel.setPrivacyObserver(lastWebData.isPrivacyMode ?: false)

        // 判断是否重新打开
        val isReopenLastTab = KeyValueManager.getBooleanValue(KeyValueManager.KEY_REOPEN_LAST_TAB, true)
        if (isReopenLastTab) {
            Log.d(TAG, "重新打开上次tab ${lastWebData.url}")
            updateWebView(lastWebData)
            viewModel.setSearchObserver(true)
        }
    }

    /**
     * 初始化时，创建新tab
     */
    private fun createNewWebTabAndInsertDB() {
        // 插入数据库新快照
        val newTabSign = System.currentTimeMillis().toString()
        val newTabViewData = WebViewData(
            WEBVIEW_DEFAULT_NAME,
            newTabSign,
            "",
            true,
            false,
            "",
            ""
        )
        Log.d(TAG, "插入WebSnapData")
        DBManager.getDao().insertWebSnapToTable(newTabViewData)

        mCurWebSign = newTabSign
        mCurWebView = null
        viewModel.setPhoneModeObserver(true)
        viewModel.setPrivacyObserver(false)
    }

    /**
     * 保存当前web快照，创建新tab
     */
    fun saveCurSnapAndCreateNewWebTab() {
        // 保存当前快照
        createWebViewSnapshot { webViewData ->
            if (webViewData != null) {
                Log.d(TAG, "更新 WebSnapData")
                DBManager.getDao().updateWebSnapItem(webViewData)
            }
            // 插入数据库新快照
            val newTabSign = System.currentTimeMillis().toString()
            val newTabViewData = WebViewData(
                WEBVIEW_DEFAULT_NAME,
                newTabSign,
                "",
                true,
                false,
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
    fun processWebSearch(searchStr: String) {
        if (searchStr.isEmpty()) {
            hideSoftKeyBoard()
            return
        }

        mCurInputStr = searchStr
        // 检查webView
        if (mCurWebView == null && mCurWebSign.isNotEmpty()) {
            // 搜索时创建webView
            updateWebView()
        } else {
            viewModel.setSearchObserver(true)
        }

        if (!UtilHelper.isNetworkAvailable(activity)) {
//            binding.btnRetryNet.setTag(R.id.no_net_select_engine, engine)
//            binding.btnRetryNet.setTag(R.id.no_net_input_str, mCurInputStr)
//            noNetObserver.value = true
//            changeStatusBarMode(true)
//            binding.editInput.setText(mCurInputStr)
//            hideSoftKeyBoard()
            return
        }
//        noNetObserver.value = false
//        changeStatusBarMode(false)

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
        val tabCount = DBManager.getDao().getWebSnapsFromTable().size
        binding.tvBottomMainTabNum.text = tabCount.toString()
        binding.tvBottomSearchTabNum.text = tabCount.toString()
    }

    fun stopLoadingAndGoBack() {
        mCurWebView?.let {
            if (binding.ivNaviPageRefresh.alpha != 1f) return
            it.reload()
            Log.d(TAG, "webView重新加载")
        }
    }

    /*
     * ************************  main调用 ************************
     */

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
            DBManager.getDao().updateWebSnapItem(WebViewData(sign = mCurWebSign, isPrivacyMode = value))
        }
    }


    /*
     * ************************  私有方法 ************************
     */

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

    inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            Log.d(TAG, "shouldOverrideUrlLoading url：${request?.url?.toString()}")
            request?.url?.toString()?.let {
                binding.editInput.setText(it)
                updateGoBackStatus()
            }
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.ivNaviPageRefresh.alpha = 0.3f
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (url.isNullOrEmpty()) return
            Log.d(TAG, "onPageFinished 更新WebSnapData： url ${url}， sign：$mCurWebSign， name：${view?.getTag(R.id.web_title)}")
            binding.editInput.setText(url)
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
    }

}