package com.habit.app.helper

import com.habit.app.data.db.DBManager

object KeyValueManager {

    private const val STR_NUM_ONE = "1"
    private const val STR_NUM_ZERO = "0"

    const val KEY_ENTERED_HOME = "key_entered_home"
    const val KEY_NIGHT_MODE = "key_night_mode"

    /**
     * 搜索引擎选择
     */
    const val KEY_ENGINE_SELECT = "key_engine_select"

    /**
     * 首页快捷选项
     */
    const val KEY_HOME_ACCESS_INFO = "key_home_access_info"

    /**
     * 是否自动打开上一个tab
     */
    const val KEY_REOPEN_LAST_TAB = "key_reopen_last_tab"

    /**
     * 记录当前日夜间模式
     * 0 跟随系统 1 日间模式 2 夜间模式
     */
    const val KEY_DARK_MODE = "key_dark_mode"

    fun saveBooleanValue(key: String, value: Boolean) {
        DBManager.getDao().updateKeyValue(key, if (value) STR_NUM_ONE else STR_NUM_ZERO)
    }

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean {
        val result = DBManager.getDao().getValueByKey(key)
            ?: if (defaultValue) STR_NUM_ONE else STR_NUM_ZERO
        return result == STR_NUM_ONE
    }


    fun saveValueWithKey(key: String, value: String) {
        DBManager.getDao().updateKeyValue(key, value)
    }

    fun getValueByKey(key: String): String? {
        return DBManager.getDao().getValueByKey(key)
    }
}