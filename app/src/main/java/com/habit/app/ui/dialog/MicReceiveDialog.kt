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
import com.habit.app.databinding.LayoutDialogMicReceiveBinding
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import kotlin.let

/**
 * 麦克风声音接收dialog
 */
class MicReceiveDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogMicReceiveBinding

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogMicReceiveBinding.inflate(LayoutInflater.from(context))
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
        binding.btnCancel.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_60))

    }

    private fun initListener() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun tryShowDialog(activity: Activity) : MicReceiveDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = MicReceiveDialog(activity)
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