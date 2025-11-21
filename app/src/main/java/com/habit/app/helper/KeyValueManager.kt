package com.habit.app.helper

import com.habit.app.model.db.DBManager

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