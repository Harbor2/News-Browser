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
import com.habit.app.databinding.LayoutDialogFileDownloadBinding
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.util.EMUtil
import kotlin.let

/**
 * 文件下载dialog
 */
class FileDownloadDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogFileDownloadBinding
    var mCallback: ((Boolean) -> Unit)? = null

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogFileDownloadBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        updateThemeUI()
        initListener()
    }

    fun setData(titleStr: String) {
        binding.tvTitle.setText(titleStr)
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_top_line_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_main_color))
        EMManager.from(binding.btnCancel)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundRealColor(EMUtil.getColor(R.color.btn_color))
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.btnCancel).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color_60))
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
        fun tryShowDialog(activity: Activity) : FileDownloadDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = FileDownloadDialog(activity)
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