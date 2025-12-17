package com.habit.app.ui.tag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.habit.app.R
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow

class TagsActivity : BaseActivity() {
    private lateinit var binding: ActivityTagsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUiConfig()
    }

    private fun initData() {

    }

    private fun initListener() {

    }

    private fun updateUiConfig() {
        EMManager.from(binding.containerTopTab)
            .setCorner(22f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
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
            context.startActivity(Intent(context, TagsActivity::class.java))
        }
    }
}