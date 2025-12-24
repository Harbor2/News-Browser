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
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.LayoutDialogNewFolderBinding
import com.habit.app.helper.UtilHelper
import com.wyz.emlibrary.util.EMUtil
import kotlin.let

/**
 * 创建 folder
 */
class NewFolderDialog(activity: Activity) : Dialog(activity) {
    var binding: LayoutDialogNewFolderBinding
    private var mRootFolderId = -1
    private var childFolder: List<FolderData> = arrayListOf()
    var mCallback: () -> Unit = {}

    private var initialY = 0f
    private val dragThreshold = EMUtil.dp2px(100f).toInt()

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogNewFolderBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        updateThemeUI()
        initListener()

        binding.editInput.post {
            EMUtil.showSoftKeyboard(binding.editInput, context)
        }
    }

    fun setData(rootFolder: Int) {
        mRootFolderId = rootFolder
        childFolder = DBManager.getDao().getSubFolder(rootFolder)
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
            checkAndSaveFolder()
        }
    }

    private fun checkAndSaveFolder() {
        val inputStr = binding.editInput.text.toString().trim()
        if (inputStr.isEmpty()) {
            UtilHelper.showToast(context, context.getString(R.string.toast_input_sth))
            return
        }
        val newFolder = FolderData(parentId = mRootFolderId, folderName = inputStr)
        if (checkFolderExist(newFolder)) {
            UtilHelper.showToast(context, context.getString(R.string.toast_folder_exist))
            return
        }

        DBManager.getDao().addFolder(newFolder)
        mCallback.invoke()
        dismiss()
    }


    private fun checkFolderExist(newFolder: FolderData): Boolean {
        if (childFolder.isEmpty()) return false

        return childFolder.contains(newFolder)
    }

    companion object {
        fun tryShowDialog(activity: Activity) : NewFolderDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = NewFolderDialog(activity)
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