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
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.databinding.LayoutDialogThemeSelectBinding
import com.habit.app.helper.DayNightUtil
import com.habit.app.ui.custom.EngineSelectItem
import kotlin.let

/**
 * schedule 提醒时间
 */
class ThemeSelectDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogThemeSelectBinding
    private var mSelectMode = DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM

    var mCallback: ((Int) -> Unit)? = null

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogThemeSelectBinding.inflate(LayoutInflater.from(context))
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
        mSelectMode = KeyValueManager.getValueByKey(KeyValueManager.KEY_DARK_MODE)?.toInt() ?: DayNightUtil.NIGHT_MODE_DAY
        updateThemeSelect()
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_top_line_color))
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_main_color))
        binding.gridLayout.forEach { item ->
            if (item is EngineSelectItem) {
                item.updateThemeUI()
            }
        }
    }

    private fun initListener() {
        binding.btnConfirm.setOnClickListener {
            mCallback?.invoke(mSelectMode)
            KeyValueManager.saveValueWithKey(KeyValueManager.KEY_DARK_MODE, mSelectMode.toString())
            dismiss()
        }

        binding.itemSystem.setOnClickListener {
            mSelectMode = DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM
            updateThemeSelect()
        }
        binding.itemLight.setOnClickListener {
            mSelectMode = DayNightUtil.NIGHT_MODE_DAY
            updateThemeSelect()
        }
        binding.itemDark.setOnClickListener {
            mSelectMode = DayNightUtil.NIGHT_MODE_NIGHT
            updateThemeSelect()
        }
    }

    private fun updateThemeSelect() {
        binding.itemSystem.setSelect(mSelectMode == DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM)
        binding.itemLight.setSelect(mSelectMode == DayNightUtil.NIGHT_MODE_DAY)
        binding.itemDark.setSelect(mSelectMode == DayNightUtil.NIGHT_MODE_NIGHT)
    }

    companion object {
        fun tryShowDialog(activity: Activity) : ThemeSelectDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = ThemeSelectDialog(activity)
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