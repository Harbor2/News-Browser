package com.habit.app.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import com.habit.app.R
import com.habit.app.helper.ThemeManager
import com.habit.app.databinding.LayoutDialogLoadingBinding
import kotlin.let

/**
 * 创建 folder
 */
class LoadingDialog(activity: Activity) : Dialog(activity) {
    var binding: LayoutDialogLoadingBinding

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogLoadingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(false)

        updateThemeUI()
    }

    fun updateThemeUI() {
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    companion object {
        fun tryShowDialog(activity: Activity) : LoadingDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = LoadingDialog(activity)
            val window: Window? = dialog.window
            window?.let {
                it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                it.setGravity(Gravity.CENTER)
            }
            dialog.show()
            return dialog
        }
    }
}