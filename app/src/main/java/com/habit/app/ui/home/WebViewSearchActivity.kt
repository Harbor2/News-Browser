package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.habit.app.R
import com.habit.app.databinding.ActivityWebviewSearchBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * 搜索Activity
 */
class WebViewSearchActivity : BaseActivity() {
    private lateinit var binding: ActivityWebviewSearchBinding
    private val mScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
    }

    private fun initData() {

    }

    private fun initListener() {

    }

    private fun updateUIConfig() {
        binding.root.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.ivNaviTabAdd.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_tab_add))
        binding.ivNaviPageRefresh.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_page_refresh))
        binding.ivSearchIcon.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_search_icon))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_80))
        EMManager.from(binding.containerInput)
            .setCorner(18f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, WebViewSearchActivity::class.java))
        }
    }
}