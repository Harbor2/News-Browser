package com.habit.app.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.habit.app.R
import com.habit.app.data.MENU_DELETE
import com.habit.app.data.MENU_RENAME
import com.habit.app.data.MENU_SELECT
import com.habit.app.data.MENU_SHARE
import com.habit.app.data.model.DownloadFileData
import com.habit.app.databinding.LayoutDownloadMenuFloatBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil

class DownloadMenuFloat(
    val context: Context,
) {
    private var popupWindow: PopupWindow? = null
    private var binding: LayoutDownloadMenuFloatBinding
    private var callback: DownloadMenuCallback? = null

    /**
     * 操作的数据
     */
    private var mData: DownloadFileData? = null

    init {
        binding = LayoutDownloadMenuFloatBinding.inflate(LayoutInflater.from(context), null, false)
        binding.itemShare.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemDelete.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemRename.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemSelect.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        initListener()
    }

    fun setData(data: DownloadFileData): DownloadMenuFloat {
        this.mData = data
        return this
    }

    fun setCallback(callback: DownloadMenuCallback): DownloadMenuFloat {
        this.callback = callback
        return this
    }

    fun show(anchorView: View) {
        EMManager.from(binding.root)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))

        popupWindow = PopupWindow(
            binding.root,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        // 获取左上角坐标
        val anchorViewLocation = IntArray(2)
        anchorView.getLocationOnScreen(anchorViewLocation)
        // 测量popupWindow宽高
        popupWindow!!.contentView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupWindow!!.contentView.measuredWidth
        val popupHeight = popupWindow!!.contentView.measuredHeight
        // 目标高度
        val targetHeight = anchorViewLocation[1] + EMUtil.dp2px(42f)
        val screenHeight = EMUtil.getScreenH(context)
        // 修正展示坐标位置
        val overflowY = targetHeight + popupHeight - screenHeight

        val realY = if (overflowY > 0) {
            (targetHeight - overflowY - EMUtil.dp2px(10f)).toInt()
        } else {
            targetHeight.toInt()
        }
        val realX = anchorView.width - popupWidth - EMUtil.dp2px(14f).toInt()
        popupWindow!!.showAtLocation(anchorView, Gravity.NO_GRAVITY, realX, realY)
    }

    fun initListener() {
        binding.itemShare.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(MENU_SHARE, mData!!)
            dismiss()
        }
        binding.itemDelete.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(MENU_DELETE, mData!!)
            dismiss()
        }
        binding.itemRename.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(MENU_RENAME, mData!!)
            dismiss()
        }
        binding.itemSelect.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(MENU_SELECT, mData!!)
            dismiss()
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    interface DownloadMenuCallback {
        fun onOptionSelect(option: String, data: DownloadFileData)
    }
}