package com.habit.app.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.habit.app.R
import com.habit.app.data.IMAGE_MENU_COPY_ADDRESS
import com.habit.app.data.IMAGE_MENU_DOWNLOAD_IMAGE
import com.habit.app.data.IMAGE_MENU_SHARE_IMAGE
import com.habit.app.databinding.LayoutImageClickMenuFloatBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil

open class ImageClickMenuFloat(
    val context: Context,
) {
    private var popupWindow: PopupWindow? = null
    private var binding: LayoutImageClickMenuFloatBinding
    private var callback: ImageMenuCallback? = null

    /**
     * 操作的数据
     */
    private var mData: String? = null

    init {
        binding = LayoutImageClickMenuFloatBinding.inflate(LayoutInflater.from(context), null, false)
        updateThemeUi()
        initListener()
    }

    fun setData(imageUrl: String): ImageClickMenuFloat {
        this.mData = imageUrl
        return this
    }

    fun setCallback(callback: ImageMenuCallback): ImageClickMenuFloat {
        this.callback = callback
        return this
    }

    fun show(locationX: Float, locationY: Float) {
        popupWindow = PopupWindow(
            binding.root,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        // 测量popupWindow宽高
        popupWindow!!.contentView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupWindow!!.contentView.measuredWidth
        val popupHeight = popupWindow!!.contentView.measuredHeight
        // 目标高度
        val targetHeight = locationY + popupHeight / 2
        val screenHeight = EMUtil.getScreenH(context)
        // 修正展示坐标位置
        val overflowY = targetHeight + popupHeight - screenHeight

        val realY = if (overflowY > 0) {
            (targetHeight - overflowY - EMUtil.dp2px(10f)).toInt()
        } else {
            targetHeight.toInt()
        }

        val targetWidth = locationX
        val screenWidth = EMUtil.getScreenW(context)
        val overflowX = targetWidth + popupWidth - screenWidth
        val realX = if (overflowX > 0) {
            (targetWidth - overflowX - EMUtil.dp2px(10f)).toInt()
        } else {
            targetWidth.toInt()
        }
        popupWindow!!.showAtLocation(binding.root, Gravity.NO_GRAVITY, realX, realY)
    }

    fun initListener() {
        binding.itemCopyAddress.setOnClickListener {
            if (mData.isNullOrEmpty()) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(IMAGE_MENU_COPY_ADDRESS, mData!!)
            dismiss()
        }
        binding.itemDownload.setOnClickListener {
            if (mData.isNullOrEmpty()) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(IMAGE_MENU_DOWNLOAD_IMAGE, mData!!)
            dismiss()
        }
        binding.itemShare.setOnClickListener {
            if (mData.isNullOrEmpty()) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(IMAGE_MENU_SHARE_IMAGE, mData!!)
            dismiss()
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    fun updateThemeUi() {
        EMManager.from(binding.root)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        binding.itemCopyAddress.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemDownload.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemShare.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
    }

    interface ImageMenuCallback {
        fun onOptionSelect(option: String, data: String)
    }
}