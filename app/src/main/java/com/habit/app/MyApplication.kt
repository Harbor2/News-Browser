package com.habit.app

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import com.habit.app.data.TAG
import com.wyz.emlibrary.em.EMLibrary
import com.habit.app.data.db.DBManager
import com.habit.app.helper.DayNightUtil
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager

class MyApplication : Application() {
    companion object {
        lateinit var mContext: Application
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        // 初始化
        initOnMainProcess()
    }

    private fun initOnMainProcess() {
        EMLibrary.init(this)
        DBManager.init(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentNightMode = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val curThemeMode = KeyValueManager.getValueByKey(KeyValueManager.KEY_DARK_MODE)?.toInt()
        if (curThemeMode != DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM) return

        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            Log.d(TAG, "日夜间模式切换监听，当前为夜间模式")
            if (ThemeManager.getCurTheme() != ThemeManager.THEME_NIGHT) {
                ThemeManager.switchTheme(ThemeManager.THEME_NIGHT)
            }
        } else if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            Log.d(TAG, "日夜间模式切换监听，当前为日间模式")
            if (ThemeManager.getCurTheme() != ThemeManager.THEME_DEFAULT) {
                ThemeManager.switchTheme(ThemeManager.THEME_DEFAULT)
            }
        }
    }
}