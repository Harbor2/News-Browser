package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.forEach
import com.google.gson.reflect.TypeToken
import com.habit.app.R
import com.habit.app.databinding.ActivityAccessSelectBinding
import com.habit.app.helper.GsonUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.data.model.AccessSingleData
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.custom.AccessSelectItem
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * 首页快捷功能选择
 */
class AccessSelectActivity : BaseActivity() {
    private lateinit var binding: ActivityAccessSelectBinding
    private val mScope = MainScope()

    /**
     * 选中的AccessData
     */
    private var selectedAccess = ArrayList<AccessSingleData>()
    /**
     * 全部的AccessData
     */
    private var allAccess = ArrayList<AccessSingleData>()

    private var textViewList = mutableListOf<TextView>()
    private var accessViewList = mutableListOf<AccessSelectItem>()
    private val containerViewList = mutableListOf<LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccessSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, ThemeManager.isNightTheme(), binding.containerNavi)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        binding.containerLinList.forEach {
            if (it is TextView || it is AppCompatTextView) {
                textViewList.add(it)
            }
            if (it is LinearLayout) {
                containerViewList.add(it)
            }
        }
        containerViewList.forEach {
            it.forEach {
                if (it is AccessSelectItem) {
                    accessViewList.add(it)
                }
            }
        }
        updateUIConfig()
    }

    private fun initData() {
        val cacheInfo = KeyValueManager.getValueByKey(KeyValueManager.KEY_HOME_ACCESS_INFO) ?: ""
        selectedAccess = if (cacheInfo.isEmpty()) {
            UtilHelper.getDefaultHomeAccessList(this)
        } else {
            GsonUtil.gson.fromJson(cacheInfo, object : TypeToken<ArrayList<AccessSingleData>>() {}.type)
        }
        allAccess = UtilHelper.getAllAccessList(this)
        for ((index, item) in accessViewList.withIndex()) {
            item.setTag(R.id.tag_access_data, allAccess[index])
            item.setSelect(selectedAccess.contains(allAccess[index]))
        }
    }

    private fun initListener() {
        binding.ivNaviBack.setOnClickListener {
            processPageBack()
        }

        accessViewList.forEach { item ->
            item.setOnClickListener {
                processDataSelectOrNot(item)
            }
        }
    }

    /**
     * 处理选中快捷方式替换
     */
    private fun processDataSelectOrNot(item: AccessSelectItem) {
        val tagData = item.getBindData() ?: return
        val existStatus = selectedAccess.contains(tagData)
        if (!existStatus && selectedAccess.size >= 15) {
            UtilHelper.showToast(this, getString(R.string.toast_can_not_add_more))
            return
        }
        if (existStatus) {
            selectedAccess.remove(tagData)
        } else {
            selectedAccess.add(3, tagData)
        }

        // 处理选中状态
        item.setSelect(!existStatus)
        // 重新处理编号
        for ((index, data) in selectedAccess.withIndex()) {
            data.sortIndex = index
        }
    }

    private fun updateUIConfig() {
        EMManager.from(binding.root).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        textViewList.forEach {
            EMManager.from(it).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color_70))
        }
        accessViewList.forEach { item ->
            item.updateThemeUI()
        }
        binding.ivNaviBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_back))
        binding.tvNaviTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    private fun processPageBack() {
        KeyValueManager.saveValueWithKey(KeyValueManager.KEY_HOME_ACCESS_INFO, GsonUtil.gson.toJson(selectedAccess))
        setResult(RESULT_OK)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            processPageBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, AccessSelectActivity::class.java))
        }
    }
}