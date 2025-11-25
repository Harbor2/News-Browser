package com.habit.app.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.habit.app.R
import com.wyz.emlibrary.em.EMManager
import com.habit.app.databinding.LayoutDialogSearchEngineBinding
import com.habit.app.event.EngineChangedEvent
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.data.ENGINE_GOOGLE
import com.habit.app.ui.custom.EngineSelectItem
import org.greenrobot.eventbus.EventBus
import kotlin.let

/**
 * schedule 提醒时间
 */
class SearchEngineDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogSearchEngineBinding
    private var mSelectEngine = ENGINE_GOOGLE

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogSearchEngineBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        initData()
        updateThemeUI()
        initListener()
    }

    private fun initData() {
        mSelectEngine = KeyValueManager.getValueByKey(KeyValueManager.KEY_ENGINE_SELECT) ?: ENGINE_GOOGLE
        binding.gridLayout.forEach { child ->
            if (child is EngineSelectItem) {
                child.setSelect(child.engineTag == mSelectEngine)
            }
        }
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.gridLayout.forEach { item ->
            if (item is EngineSelectItem) {
                item.updateThemeUI()
            }
        }
    }

    private fun initListener() {
        binding.btnConfirm.setOnClickListener {
            if (mSelectEngine.isNotEmpty()) {
                KeyValueManager.saveValueWithKey(KeyValueManager.KEY_ENGINE_SELECT, mSelectEngine)
                EventBus.getDefault().post(EngineChangedEvent())
            }
            dismiss()
        }
        binding.gridLayout.forEach { child ->
            if (child is EngineSelectItem) {
                child.setOnClickListener {
                    updateEngineSelect(child)
                }
            }
        }
    }

    private fun updateEngineSelect(child: EngineSelectItem) {
        this.mSelectEngine = child.engineTag
        binding.gridLayout.forEach { item ->
            if (item is EngineSelectItem) {
                item.setSelect(item == child)
            }
        }
    }

    companion object {
        fun tryShowDialog(activity: Activity) : SearchEngineDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = SearchEngineDialog(activity)
            dialog.setOnShowListener {
                val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                bottomSheet?.let {
                    it.background = Color.TRANSPARENT.toDrawable()
                    val behavior = BottomSheetBehavior.from(it)
                    behavior.isHideable = true
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
            val window: Window? = dialog.window
            window?.let {
                it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                it.setWindowAnimations(R.style.DialogAnimation)
                it.setGravity(Gravity.BOTTOM)
            }
            dialog.show()
            return dialog
        }
    }
}