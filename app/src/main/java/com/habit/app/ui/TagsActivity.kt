package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.habit.app.ui.base.BaseActivity
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.helper.DayNightUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.model.db.DBManager
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
            DayNightUtil.changeSkinNightMode(DayNightUtil.NIGHT_MODE_DAY)
        }
        binding.btnModeNight.setOnClickListener {
            DayNightUtil.changeSkinNightMode(DayNightUtil.NIGHT_MODE_NIGHT)
        }
        binding.btnModeFollow.setOnClickListener {
            DayNightUtil.changeSkinNightMode(DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM)
        }
    }

    override fun onDestroy() {
        mScope.cancel()
        DBManager.close()
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TagsActivity::class.java))
        }
    }
}