package com.habit.app.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.habit.app.R
import com.habit.app.data.OPTION_ADD_TO_HOME
import com.habit.app.data.OPTION_ADD_TO_NAVI
import com.habit.app.data.OPTION_DELETE
import com.habit.app.data.OPTION_EDIT
import com.habit.app.data.OPTION_OPEN_IN_NEW_TAB
import com.habit.app.data.OPTION_REMOVE
import com.habit.app.data.OPTION_SELECT
import com.habit.app.databinding.LayoutPopupMenuBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil

class MenuPopupFloat(
    val context: Context,
) {
    private var popupWindow: PopupWindow? = null
    private var binding: LayoutPopupMenuBinding
    private var callback: PopupCallback? = null

    /**
     * 操作的数据
     */
    private var mData: Any? = null

    init {
        binding = LayoutPopupMenuBinding.inflate(LayoutInflater.from(context), null, false)
        binding.itemDelete.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemOpenNewTab.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemRemove.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemEdit.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemAddNavigation.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemAddScreen.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.itemSelect.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        initListener()
    }

    fun setData(data: Any): MenuPopupFloat {
        this.mData = data
        return this
    }

    fun setCallback(callback: PopupCallback): MenuPopupFloat {
        this.callback = callback
        return this
    }

    fun show(anchorView: View, menuList: ArrayList<String>) {
        EMManager.from(binding.root)
            .setCorner(12f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))

        popupWindow = PopupWindow(
            binding.root,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        binding.itemDelete.visibility = if (menuList.contains(OPTION_DELETE)) View.VISIBLE else View.GONE
        binding.itemOpenNewTab.visibility = if (menuList.contains(OPTION_OPEN_IN_NEW_TAB)) View.VISIBLE else View.GONE
        binding.itemRemove.visibility = if (menuList.contains(OPTION_REMOVE)) View.VISIBLE else View.GONE
        binding.itemEdit.visibility = if (menuList.contains(OPTION_EDIT)) View.VISIBLE else View.GONE
        binding.itemAddNavigation.visibility = if (menuList.contains(OPTION_ADD_TO_NAVI)) View.VISIBLE else View.GONE
        binding.itemAddScreen.visibility = if (menuList.contains(OPTION_ADD_TO_HOME)) View.VISIBLE else View.GONE
        binding.itemSelect.visibility = if (menuList.contains(OPTION_SELECT)) View.VISIBLE else View.GONE

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
        binding.itemDelete.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(OPTION_DELETE, mData)
            dismiss()
        }
        binding.itemOpenNewTab.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(OPTION_OPEN_IN_NEW_TAB, mData)
            dismiss()
        }
        binding.itemEdit.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(OPTION_EDIT, mData)
            dismiss()
        }
        binding.itemAddNavigation.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(OPTION_ADD_TO_NAVI, mData)
            dismiss()
        }
        binding.itemAddScreen.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(OPTION_ADD_TO_HOME, mData)
            dismiss()
        }
        binding.itemSelect.setOnClickListener {
            if (mData == null) {
                return@setOnClickListener
            }
            callback?.onOptionSelect(OPTION_SELECT, mData)
            dismiss()
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    interface PopupCallback {
        fun onOptionSelect(option: String, data: Any? = null)
    }
}