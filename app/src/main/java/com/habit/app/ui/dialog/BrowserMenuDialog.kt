package com.habit.app.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.forEach
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.BookmarkData
import com.wyz.emlibrary.em.EMManager
import com.habit.app.helper.ThemeManager
import com.habit.app.databinding.LayoutDialogBrowserMenuBinding
import com.habit.app.helper.DayNightUtil
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_DAY
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_NIGHT
import com.habit.app.helper.KeyValueManager
import com.habit.app.ui.custom.BrowserMenuItem
import kotlin.let

/**
 * menu 菜单栏
 */
class BrowserMenuDialog(activity: Activity) : BottomSheetDialog(activity) {
    var binding: LayoutDialogBrowserMenuBinding
    var mCallback: BrowserMenuCallback? = null

    private var mName: String = ""
    private var mUrl: String = ""
    private var mWebIconPath: String = ""

    private var isPrivate = false
    private var isPhoneMode = true
    private var isAddBookmark = false

    init {
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        binding = LayoutDialogBrowserMenuBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        EMManager.from(binding.btnConfirm)
            .setCorner(12f)
            .setBackGroundColor(R.color.btn_color)

        updateThemeUI()
        initListener()
    }

    fun initData(url: String, name: String, webIconPath: String, private: Boolean, phoneMode: Boolean) {
        mUrl = url
        mName = name
        mWebIconPath = webIconPath

        isPrivate = private
        isPhoneMode = phoneMode
        val bookmarkData = DBManager.getDao().getBookMarkByUrl(mUrl)
        isAddBookmark = bookmarkData != null

        updateDayNightBtnShow()
        updatePrivateMode()
        updateDesktopMode()
        updateBookmarkMode()
    }

    fun updateThemeUI() {
        EMManager.from(binding.topLine)
            .setCorner(4f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.containerContent)
            .setCorner(floatArrayOf(24f, 24f, 0f, 0f))
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.gridLayout.forEach { item ->
            if (item is BrowserMenuItem) {
                item.updateThemeUI()
            }
        }
    }

    private fun initListener() {
        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
        binding.itemPrivate.setOnClickListener {
            isPrivate = !isPrivate
            updatePrivateMode()
            mCallback?.onPrivateChanged(isPrivate)
            dismiss()
        }
        binding.itemBookmarks.setOnClickListener {

        }
        binding.itemDownload.setOnClickListener {

        }
        binding.itemHistory.setOnClickListener {

        }

        binding.itemBookmarkAdd.setOnClickListener {
            processBookmarkAdd()
        }
        binding.itemNavigationAdd.setOnClickListener {
            processNavigationAdd()
        }
        binding.itemDarkMode.setOnClickListener {
            processDarkMode()
        }
        binding.itemDesktopSite.setOnClickListener {
            isPhoneMode = !isPhoneMode
            updateDesktopMode()
            mCallback?.onDesktopChanged(isPhoneMode)
            dismiss()
        }

        binding.itemFeedback.setOnClickListener {

        }
        binding.itemPageSearch.setOnClickListener {

        }
        binding.itemShare.setOnClickListener {

        }
        binding.itemSetting.setOnClickListener {

        }
    }

    private fun updateDayNightBtnShow() {
        // 获取展示
        val displayMode = getNextDarkMode()

        when(displayMode) {
            NIGHT_MODE_FOLLOW_SYSTEM -> {
                binding.itemDarkMode.updateData(R.drawable.iv_d_m_system_mode, context.getString(R.string.text_system_mode))
            }
            NIGHT_MODE_DAY -> {
                binding.itemDarkMode.updateData(R.drawable.iv_d_m_light_mode, context.getString(R.string.text_light_mode))
            }
            NIGHT_MODE_NIGHT -> {
                binding.itemDarkMode.updateData(R.drawable.iv_d_m_dark_mode, context.getString(R.string.text_dark_mode))
            }
        }
    }

    private fun updatePrivateMode() {
        binding.itemPrivate.updateData(ThemeManager.getSkinImageResId(if (isPrivate) R.drawable.iv_d_m_exit_private else R.drawable.iv_d_m_private), context.getString(if (isPrivate) R.string.text_exit_private else R.string.text_private))
    }

    private fun updateBookmarkMode() {
        binding.itemBookmarkAdd.updateData(ThemeManager.getSkinImageResId(if (isAddBookmark) R.drawable.iv_d_m_remove_bookmarks else R.drawable.iv_d_m_add_bookmarks), context.getString(if (isAddBookmark) R.string.text_remove_bookmarks else R.string.text_add_bookmarks))
    }

    private fun updateDesktopMode() {
        binding.itemDesktopSite.updateData(ThemeManager.getSkinImageResId(if (isPhoneMode) R.drawable.iv_d_m_desktop_site else R.drawable.iv_d_m_phone_site), context.getString(if (isPhoneMode) R.string.text_desktop_site else R.string.text_phone_site))
    }

    /**
     * 添加书签 移除书签
     */
    private fun processBookmarkAdd() {
        if (isAddBookmark) {
            DBManager.getDao().deleteBookmarkByUrl(mUrl)
        } else {
            DBManager.getDao().insertBookmarkToTable(BookmarkData(name = mName.ifEmpty { mUrl }, url = mUrl, webIconPath = mWebIconPath))
        }
        isAddBookmark = !isAddBookmark
        updateBookmarkMode()
        mCallback?.onBookmarkChanged(isAddBookmark)
        dismiss()
    }

    /**
     * 添加到导航栏
     */
    private fun processNavigationAdd() {
        mCallback?.onNavigationAddClick()
        dismiss()
    }

    /**
     * 处理日夜间模式切换
     */
    private fun processDarkMode() {
        var nextMode = getNextDarkMode()
        Log.d(TAG, "切换到新的日夜间模式：$nextMode")
        KeyValueManager.saveValueWithKey(KeyValueManager.KEY_DARK_MODE, nextMode.toString())

        updateDayNightBtnShow()

        if (nextMode == NIGHT_MODE_FOLLOW_SYSTEM) {
            nextMode = DayNightUtil.getRealCurrentNightMode(context)
        }

        // 执行切换
        if (nextMode == NIGHT_MODE_DAY) {
            if (ThemeManager.getCurTheme() != ThemeManager.THEME_DEFAULT) {
                Log.d(TAG, "实际切换到日间模式")
                ThemeManager.switchTheme(ThemeManager.THEME_DEFAULT)
            }
        } else {
            if (ThemeManager.getCurTheme() != ThemeManager.THEME_NIGHT) {
                Log.d(TAG, "实际切换到夜间模式")
                ThemeManager.switchTheme(ThemeManager.THEME_NIGHT)
            }
        }
    }

    /**
     * 获取下一个日夜间模式
     */
    private fun getNextDarkMode() : Int {
        // 0 跟随系统 1 日间模式 2 夜间模式
        val curMode = KeyValueManager.getValueByKey(KeyValueManager.KEY_DARK_MODE)?.toInt() ?: NIGHT_MODE_DAY
        return when (curMode) {
            NIGHT_MODE_FOLLOW_SYSTEM -> NIGHT_MODE_DAY
            NIGHT_MODE_DAY -> NIGHT_MODE_NIGHT
            NIGHT_MODE_NIGHT -> NIGHT_MODE_FOLLOW_SYSTEM
            else -> NIGHT_MODE_NIGHT
        }
    }

    companion object {
        fun tryShowDialog(activity: Activity) : BrowserMenuDialog? {
            if (activity.isFinishing || activity.isDestroyed) {
                return null
            }

            val dialog = BrowserMenuDialog(activity)
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

    interface BrowserMenuCallback {
        fun onPrivateChanged(enter: Boolean) {}
        fun onBookMarksClick() {}
        fun onDownloadClick() {}
        fun onHistoryClick() {}

        fun onBookmarkChanged(add: Boolean) {}
        fun onNavigationAddClick() {}
        fun onDesktopChanged(isPhoneMode: Boolean) {}

        fun onFeedbackClick() {}
        fun onPageSearchClick() {}
        fun onShareClick() {}
        fun onSettingClick() {}
    }
}