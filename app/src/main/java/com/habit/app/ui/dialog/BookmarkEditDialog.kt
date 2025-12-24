package com.habit.app.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.data.TAG
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.BookmarkData
import com.habit.app.data.model.FolderData
import com.habit.app.databinding.LayoutDialogBookmarkEditBinding
import com.habit.app.helper.UtilHelper
import com.wyz.emlibrary.util.EMUtil
import kotlin.let

/**
 * 编辑书签 dialog
 */
class BookmarkEditDialog(activity: Activity) : Dialog(activity) {
    var binding: LayoutDialogBookmarkEditBinding
    private var mRootFolderId = -1
    private var childFolder: List<FolderData> = arrayListOf()
    var mCallback: EditDialogCallback? = null

    /**
     * 传入需要编辑的标签页数据
     */
    private var mData: List<Any> = arrayListOf()

    /**
     * 当前数据所处的层级id
     */
    private var mCurrentFolder = -1

    /**
     * 新的目录
     */
    private var mNewDirectoryId: Int = -1

    private var initialY = 0f
    private val dragThreshold = EMUtil.dp2px(100f).toInt()

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogBookmarkEditBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        updateThemeUI()
        initListener()
    }

    fun setData(data: List<Any>, curDirectory: Int) {
        this.mData = data
        this.mCurrentFolder = curDirectory
        if (curDirectory == -1) {
            binding.tvFolderName.text = "/"
        } else {
            binding.tvFolderName.text = DBManager.getDao().getFolderById(curDirectory)?.folderName ?: "/"
        }
        childFolder = DBManager.getDao().getSubFolder(mRootFolderId)
        if (data.size != 1) {
            // 批量操作
            binding.containerName.isVisible = false
            binding.containerUrl.isVisible = false
        } else {
            data.firstOrNull()?.let { firstData ->
                // 初始化数据
                when (firstData) {
                    is FolderData -> {
                        binding.containerName.isVisible = true
                        binding.containerUrl.isVisible = false
                        binding.editName.setText(firstData.folderName)
                    }
                    is BookmarkData -> {
                        binding.containerName.isVisible = true
                        binding.containerUrl.isVisible = true
                        binding.editName.setText(firstData.name)
                        binding.editUrl.setText(firstData.url)
                    }
                }
            } ?: run {
                UtilHelper.showToast(context, context.getString(R.string.toast_error))
                dismiss()
            }
        }
    }

    fun updateDirectory(parentFolderId: Int) {
        if (parentFolderId == -1) {
            binding.tvFolderName.text = "/"
            mNewDirectoryId = -1
        } else {
            val mNewDirectory = DBManager.getDao().getFolderById(parentFolderId)
            mNewDirectory?.let {
                binding.tvFolderName.text = it.folderName
            }
        }
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.lineNameView).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.lineUrlView).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
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
        binding.editName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editName.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.tvUrl.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        binding.editUrl.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editUrl.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.tvDirectory.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        binding.tvFolderName.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.ivArrow.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_arrow))
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

        binding.containerDirectory.setOnClickListener {
            val folderIds = mData.filterIsInstance<FolderData>().map { it.folderId }
            mCallback?.onLaunchFolderSelect(folderIds)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            checkAndSaveFolder()
        }
    }

    /**
     * 检查并保存 folder
     */
    private fun checkAndSaveFolder() {
        if (mData.isEmpty()) {
            UtilHelper.showToast(context, context.getString(R.string.toast_error))
            return
        }
        var shouldEditName: Boolean = false
        var shouldEditUrl: Boolean = false
        var shouldMove: Boolean = false
        if (mData.size == 1) {
            // 单文件
            Log.d(TAG, "单文件更新")
            val inputNameStr = binding.editName.text.toString().trim()
            val inputUrlStr = binding.editUrl.text.toString().trim()
            val firstData = mData.first()
            // 移动
            if (mNewDirectoryId != mCurrentFolder) {
                shouldMove = true
            }
            when (firstData) {
                is BookmarkData -> {
                    if (inputNameStr.isEmpty() || inputUrlStr.isEmpty()) {
                        UtilHelper.showToast(context, context.getString(R.string.toast_input_sth))
                        return
                    }
                    if (inputNameStr != firstData.name) {
                        shouldEditName = true
                        firstData.name = inputNameStr
                    }
                    if (inputUrlStr != firstData.url) {
                        firstData.url = inputUrlStr
                        shouldEditUrl = true
                    }
                    if (shouldMove) {
                        firstData.folderId = mNewDirectoryId
                    }
                    if (shouldMove || shouldEditUrl || shouldEditName) {
                        Log.d(TAG, "单文件：名字变化、url变更或目录移动")
                        DBManager.getDao().updateBookmarks(listOf(firstData))
                        mCallback?.onFolderSaved()
                    }
                }
                is FolderData -> {
                    if (inputNameStr.isEmpty()) {
                        UtilHelper.showToast(context, context.getString(R.string.toast_input_sth))
                        return
                    }
                    if (inputNameStr != firstData.folderName) {
                        firstData.folderName = inputNameStr
                        shouldEditName = true
                    }
                    if (shouldMove) {
                        firstData.parentId = mNewDirectoryId
                    }
                    if (shouldMove || shouldEditName) {
                        Log.d(TAG, "单文件：名字变化或目录移动")
                        DBManager.getDao().updateFolders(listOf(firstData))
                        mCallback?.onFolderSaved()
                    }
                }
            }
        } else {
            // 多文件
            Log.d(TAG, "多文件更新")
            if (mNewDirectoryId != mCurrentFolder) {
                Log.d(TAG, "多文件：目录移动")
                val folders = mData.filterIsInstance<FolderData>()
                val bookmarks = mData.filterIsInstance<BookmarkData>()
                val folderNames = folders.map { it.folderName }
                // 校验目录名称
                val targetNames = DBManager.getDao().getSubFolder(mNewDirectoryId).map { it.folderName }
                if (targetNames.any { folderNames.contains(it) }) {
                    // 重复folder
                    Log.e(TAG, "多文件：有重复命名的目录存在")
                    UtilHelper.showToast(context, context.getString(R.string.toast_folder_exist))
                    return
                }
                folders.map { it.parentId = mNewDirectoryId }
                bookmarks.map { it.folderId = mNewDirectoryId }
                // 移动
                DBManager.getDao().updateFolders(folders)
                DBManager.getDao().updateBookmarks(bookmarks)
                mCallback?.onFolderSaved()
            }
        }

        UtilHelper.showToast(context, context.getString(R.string.toast_succeed))
        dismiss()
    }

    companion object {
        fun tryShowDialog(activity: Activity) : BookmarkEditDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = BookmarkEditDialog(activity)
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

    interface EditDialogCallback {
        fun onLaunchFolderSelect(folderIds: List<Int>)
        fun onFolderSaved()
    }
}