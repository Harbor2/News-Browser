package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.habit.app.R
import com.habit.app.databinding.ActivityFileDownloadBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem


class FileDownloadActivity : BaseActivity() {
    private lateinit var binding: ActivityFileDownloadBinding
    private val mAdapter = FlexibleAdapter<AbstractFlexibleItem<*>>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)

        initView()
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

    }

    private fun initListener() {
        binding.ivNaviBack.setOnClickListener {
            finish()
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