package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.habit.app.ui.base.BaseActivity
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.helper.DayNightUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.model.TAG
import com.habit.app.model.db.DBManager
import com.habit.app.skin.ThemeManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class TagsActivity : BaseActivity() {
    private lateinit var binding: ActivityTagsBinding
    private val mScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            // 允许dom存储
            settings.domStorageEnabled = true
            // 适配屏幕
            settings.useWideViewPort = true
            // 缩放至屏幕大小
            settings.loadWithOverviewMode = true
            // 隐藏缩放按钮
            settings.displayZoomControls = false
            // 不允许缩放
            // 关闭 Google 安全浏览
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                settings.safeBrowsingEnabled = false
            }
            loadUrl("https://www.zaobao.com.sg/news")
        }
    }

    private fun initData() {
        when (DayNightUtil.getCurrentNightMode()) {
            DayNightUtil.NIGHT_MODE_DAY -> binding.tvMode.text = "日间"
            DayNightUtil.NIGHT_MODE_NIGHT -> binding.tvMode.text = "夜间"
            DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM -> binding.tvMode.text = "跟随系统"
        }

        when (DayNightUtil.getRealCurrentNightMode(this)) {
            DayNightUtil.NIGHT_MODE_DAY -> binding.tvModeReal.text = "日间"
            DayNightUtil.NIGHT_MODE_NIGHT -> binding.tvModeReal.text = "夜间"
        }
    }

    private fun initListener() {
        binding.btnModeDay.setOnClickListener {
            ThemeManager.switchTheme(ThemeManager.THEME_DEFAULT)
        }
        binding.btnModeNight.setOnClickListener {
            ThemeManager.switchTheme(ThemeManager.THEME_NIGHT)
        }
        binding.btnModeFollow.setOnClickListener {
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "tags Activity onConfigurationChanged")
    }

    override fun onDestroy() {
        Log.d(TAG, "tags Activity onDestroy")
        mScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TagsActivity::class.java))
        }
    }
}