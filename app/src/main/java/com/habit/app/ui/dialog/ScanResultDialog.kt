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
import com.habit.app.databinding.LayoutDialogScanResultBinding
import kotlin.let

/**
 * 二维码扫描结果弹窗
 */
class ScanResultDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogScanResultBinding
    var mCallback: DialogCallback? = null
    private var mText: String = ""

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogScanResultBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        updateThemeUI()
        initListener()
    }

    fun setData(text: String) {
        mText = text
        val displayText = if (text.length > 797) text.substring(0, 797).plus("…") else text
        binding.tvResult.text = displayText

        binding.btnCopy.text = context.getString(R.string.text_copy)
        binding.btnConfirm.text = context.getString(if (text.startsWith("http://") || text.startsWith("https://")) R.string.text_jump else R.string.text_search)
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_top_line_color))
        EMManager.from(binding.tvResult)
            .setCorner(10f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_main_color))
        EMManager.from(binding.btnCopy)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        binding.btnCopy.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_60))

    }

    private fun initListener() {
        binding.btnCopy.setOnClickListener {
            if (mText.isEmpty()) return@setOnClickListener
            mCallback?.onCopy(mText)
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            if (mText.isEmpty()) return@setOnClickListener
            mCallback?.onJump(mText)
            dismiss()
        }
    }

    companion object {
        fun tryShowDialog(activity: Activity) : ScanResultDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = ScanResultDialog(activity)
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

    interface DialogCallback {
        fun onCopy(text: String)
        fun onJump(text: String)
    }
}