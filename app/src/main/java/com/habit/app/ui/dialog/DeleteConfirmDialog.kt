package com.habit.app.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.habit.app.R
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.databinding.LayoutDialogDeleteConfirmBinding
import com.wyz.emlibrary.util.EMUtil
import kotlin.let

/**
 * 删除二次确认dialog
 */
class DeleteConfirmDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogDeleteConfirmBinding
    var mCallback: ((Boolean) -> Unit)? = null
    private val mIconRes: Int? = null

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogDeleteConfirmBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        updateThemeUI()
        initListener()
    }

    fun initData(icon: Int, title: String, canStr: String, confirmStr: String) {
        binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(icon))
        binding.tvTitle.text = title
        binding.btnCancel.text = canStr
        binding.btnConfirm.text = confirmStr
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
        mIconRes?.let {
            binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(it))
        }
    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            mCallback?.invoke(false)
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            mCallback?.invoke(true)
            dismiss()
        }
    }

    companion object {
        fun tryShowDialog(activity: Activity) : DeleteConfirmDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = DeleteConfirmDialog(activity)
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