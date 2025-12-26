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
import com.habit.app.databinding.LayoutDialogDataDeleteBinding
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.custom.DeleteDataItem
import com.wyz.emlibrary.util.EMUtil
import kotlin.let

/**
 * 设置页面 数据删除dialog
 */
class DataDeleteDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogDataDeleteBinding
    var mCallback: ((List<String>) -> Unit)? = null

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogDataDeleteBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        updateThemeUI()
        initListener()
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.btnCancel)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundRealColor(EMUtil.getColor("#FF1B0B"))
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.btnCancel).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color_60))
        binding.containerList.forEach {
            (it as? DeleteDataItem)?.updateThemeUi()
        }
    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            processConfirm()
        }
    }

    private fun processConfirm() {
        val deleteList = ArrayList<String>()

        if (binding.itemCookies.getSelectStatus()) {
            deleteList.add(COOKIES)
        }
        if (binding.itemHistory.getSelectStatus()) {
            deleteList.add(HISTORY)
        }
        if (binding.itemTabs.getSelectStatus()) {
            deleteList.add(TABS)
        }
        if (binding.itemHistoryRecords.getSelectStatus()) {
            deleteList.add(HISTORY_RECORDS)
        }
        if (binding.itemCache.getSelectStatus()) {
            deleteList.add(CACHE)
        }
        if (deleteList.isEmpty()) {
            UtilHelper.showToast(context, context.getString(R.string.toast_you_must_select_one))
            return
        }
        mCallback?.invoke(deleteList)
        dismiss()
    }

    companion object {
        const val COOKIES = "cookies"
        const val HISTORY = "history"
        const val TABS = "tabs"
        const val HISTORY_RECORDS = "history_records"
        const val CACHE = "cache"

        fun tryShowDialog(activity: Activity) : DataDeleteDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = DataDeleteDialog(activity)
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