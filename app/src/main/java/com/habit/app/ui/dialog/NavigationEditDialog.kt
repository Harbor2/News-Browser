package com.habit.app.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import com.bumptech.glide.Glide
import com.habit.app.R
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.databinding.LayoutDialogNavigationEditBinding
import com.habit.app.helper.UtilHelper
import com.wyz.emlibrary.util.EMUtil
import kotlin.let

/**
 * 编辑首页快捷方式 dialog
 */
class NavigationEditDialog(activity: Activity) : Dialog(activity) {
    var binding: LayoutDialogNavigationEditBinding
    var mCallback: ((String) -> Unit)? = null

    private var initialY = 0f
    private val dragThreshold = EMUtil.dp2px(100f).toInt()

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogNavigationEditBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        updateThemeUI()
        initListener()
    }

    fun setData(iconPath: String?, name: String) {
        binding.editInput.setText(name)
        if (iconPath.isNullOrEmpty()) {
            binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
        } else {
            Glide.with(context)
                .load(iconPath)
                .error(ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
                .into(binding.ivIcon)
        }
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.lineView).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.btnCancel)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.create_folder_cancel_color))
        EMManager.from(binding.btnSave)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)
        EMManager.from(binding.tvTitle).setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editInput.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.btnCancel.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_70))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {
        binding.containerContent.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> initialY = event.rawY
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    if (deltaY > 0) binding.root.translationY = deltaY
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaY = event.rawY - initialY
                    if (deltaY > dragThreshold) dismiss()
                    else binding.root.animate().translationY(0f).setDuration(200).start()
                }
            }
            true
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            val name = binding.editInput.text.toString().trim()
            if (name.isEmpty()) {
                UtilHelper.showToast(context, context.getString(R.string.toast_input_sth))
                return@setOnClickListener
            }
            mCallback?.invoke(name)
            dismiss()
        }
    }

    companion object {
        fun tryShowDialog(activity: Activity) : NavigationEditDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = NavigationEditDialog(activity)
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