package com.habit.app.model.db

object DBConstant {

    /**
     * 通用key-value相关
     */
    const val KEY_VALUE_TABLE = "key_value_table"
    const val KEY_ID = "key_id"
    const val KEY_NAME = "key_name"
    const val KEY_VALUE = "key_value"
    const val KEY_USER_ID = "key_user_id"
    const val CREATE_KEY_VALUE_TABLE =
        "CREATE TABLE $KEY_VALUE_TABLE ($KEY_ID INTEGER PRIMARY KEY,$KEY_NAME TEXT UNIQUE, $KEY_VALUE TEXT,$KEY_USER_ID TEXT)"

}