package com.habit.app.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import com.habit.app.R
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.data.model.DownloadFileData
import com.habit.app.databinding.LayoutDialogFileRenameBinding
import com.habit.app.helper.UtilHelper
import com.wyz.emlibrary.util.EMUtil
import java.io.File
import kotlin.let

/**
 * 重命名
 */
class FileRenameDialog(activity: Activity) : Dialog(activity) {
    var binding: LayoutDialogFileRenameBinding
    var mCallback: (DownloadFileData) -> Unit = {}
    private var mData: DownloadFileData? = null

    private var initialY = 0f
    private val dragThreshold = EMUtil.dp2px(100f).toInt()

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogFileRenameBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        updateThemeUI()
        initListener()

        binding.editInput.post {
            EMUtil.showSoftKeyboard(binding.editInput, context)
        }
    }

    fun setData(data: DownloadFileData) {
        mData = data
        binding.editInput.setText(data.fileName)
        binding.editInput.hint = data.fileName
        binding.editInput.setSelection(data.fileName.length)
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_top_line_color))
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
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.dialog_main_color))
        binding.tvName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
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
            renameFile()
        }
    }

    private fun renameFile() {
        if (mData == null) {
            UtilHelper.showToast(context, context.getString(R.string.toast_error))
            return
        }
        val inputStr = binding.editInput.text.toString().trim()
        if (inputStr.isEmpty()) {
            UtilHelper.showToast(context, context.getString(R.string.toast_enter_new_name))
            return
        }
        val originFile = File(mData!!.filePath)
        if (!originFile.exists()) {
            UtilHelper.showToast(context, context.getString(R.string.toast_file_not_exist))
            return
        }
        val newFile = File(originFile.parent, inputStr)
        if (newFile.exists()) {
            UtilHelper.showToast(context, context.getString(R.string.toast_file_already_exist))
            return
        }
        val result = originFile.renameTo(newFile)
        if (!result) {
            UtilHelper.showToast(context, context.getString(R.string.toast_error))
        } else {
            mData!!.fileName = inputStr
            mData!!.filePath = newFile.absolutePath
            mData!!.fileType = UtilHelper.getFileTypeByName(inputStr)
            mCallback.invoke(mData!!)
        }
        dismiss()
    }


    companion object {
        fun tryShowDialog(activity: Activity) : FileRenameDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = FileRenameDialog(activity)
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