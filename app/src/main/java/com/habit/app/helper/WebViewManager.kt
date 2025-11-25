package com.habit.app.helper

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.webkit.WebView
import com.habit.app.data.TAG
import com.habit.app.ui.custom.CustomWebView

object WebViewManager {
    /**
     * 网站Agen 桌面端 移动端
     */
    const val USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    const val USER_AGENT_PHONE = "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

    private const val MAX_ACTIVE_WEBVIEWS = 10
    private val webViewCache = LinkedHashMap<String, WebView>()

    /**
     * 获取 WebView，如果缓存中存在，则复用，否则创建新的。
     */
    fun getWebView(context: Context, key: String, loadUrl: String? = null, isPhoneMode: Boolean = true): WebView {
        return webViewCache[key] ?: createNewWebView(context, loadUrl, isPhoneMode).also {
            addWebViewToCache(key, it)
        }
    }

    fun getAllTabs(): LinkedHashMap<String, WebView> {
        return webViewCache
    }

    /**
     * 创建新的 WebView
     */
    private fun createNewWebView(context: Context, loadUrl: String? = null, isPhoneMode: Boolean): WebView {
        return CustomWebView(context.applicationContext).apply {
            setBackgroundColor(Color.WHITE)
            settings.javaScriptEnabled = true
            // 允许dom存储
            settings.domStorageEnabled = true
            // 适配屏幕
            settings.useWideViewPort = true
            // 缩放至屏幕大小
            settings.loadWithOverviewMode = true
            // 隐藏缩放按钮
            settings.displayZoomControls = false
            // 桌面端
            settings.userAgentString = if (isPhoneMode) USER_AGENT_PHONE else USER_AGENT_DESKTOP
            // 不允许缩放
            settings.builtInZoomControls = !isPhoneMode

            // 关闭 Google 安全浏览
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = false
            }
            Log.d(TAG, "创建webview：$loadUrl")
            // 预加载url
            if (!loadUrl.isNullOrEmpty()) {
                loadUrl(loadUrl)
            }
        }
    }

    /**
     * 将 WebView 加入缓存，超出最大数量时移除最久未使用的。
     */
    private fun addWebViewToCache(key: String, webView: WebView) {
        if (webViewCache.size >= MAX_ACTIVE_WEBVIEWS) {
            val oldestKey = webViewCache.keys.first()
            webViewCache.remove(oldestKey)?.destroy()
        }
        webViewCache[key] = webView
    }

    /**
     * 释放 WebView（可用于 Activity 关闭时调用）
     */
    fun releaseWebView(key: String? = null) {
        when {
            key.isNullOrEmpty() -> {
                webViewCache.forEach {
                    it.value.destroy()
                }
                webViewCache.clear()
            }
            else -> webViewCache.remove(key)?.destroy()
        }
    }
}