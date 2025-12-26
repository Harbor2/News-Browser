package com.habit.app.data.db

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


    /**
     * tab标签表
     */
    const val TABLE_TAB = "tab_table"
    const val TAB_ID = "tab_id"
    const val TAB_SIGN = "tab_sign"
    const val TAB_NAME = "tab_name"
    const val TAB_URL = "tab_url"
    const val TAB_PHONE_TYPE = "tab_desktop_type"
    const val TAB_PRIVACY_TYPE = "tab_privacy_type"
    const val TAB_COVER_BITMAP = "tab_cover_bitmap"
    const val TAB_ICON_BITMAP = "tab_icon_bitmap"
    const val TAB_UPDATE_TIME = "tab_update_time"
    const val CREATE_TAB_TABLE =
        "CREATE TABLE $TABLE_TAB ($TAB_ID INTEGER PRIMARY KEY,$TAB_SIGN TEXT UNIQUE, $TAB_NAME TEXT, $TAB_URL TEXT, $TAB_PHONE_TYPE INTEGER, $TAB_PRIVACY_TYPE INTEGER, $TAB_COVER_BITMAP TEXT, $TAB_ICON_BITMAP TEXT, $TAB_UPDATE_TIME INTEGER)"

    /**
     * 搜索记录表
     */
    const val TABLE_SEARCH_RECORD = "search_record"
    const val SEARCH_ID = "search_id"
    const val SEARCH_CONTENT = "search_content"
    const val CREATE_SEARCH_RECORD_TABLE =
        "CREATE TABLE $TABLE_SEARCH_RECORD ($SEARCH_ID INTEGER PRIMARY KEY, $SEARCH_CONTENT TEXT UNIQUE)"


    /**
     * 历史记录表
     */
    const val TABLE_HISTORY = "history_table"
    const val HISTORY_ID = "history_id"
    const val HISTORY_NAME = "history_name"
    const val HISTORY_URL = "history_url"
    const val HISTORY_ICON_BITMAP = "history_icon_bitmap"
    const val HISTORY_UPDATE_TIME = "tab_update_time"
    const val CREATE_HISTORY_TABLE =
        "CREATE TABLE $TABLE_HISTORY ($HISTORY_ID INTEGER PRIMARY KEY, $HISTORY_NAME TEXT, $HISTORY_URL TEXT, $HISTORY_ICON_BITMAP TEXT, $HISTORY_UPDATE_TIME INTEGER)"


    /**
     * folder表
     */
    const val TABLE_FOLDER = "folder_table"
    const val FOLDER_ID = "folder_id"
    const val FOLDER_PARENT_ID = "folder_parent_id"
    const val FOLDER_NAME = "folder_name"
    const val CREATE_FOLDER_TABLE =
        "CREATE TABLE $TABLE_FOLDER ($FOLDER_ID INTEGER PRIMARY KEY, $FOLDER_PARENT_ID INTEGER, $FOLDER_NAME TEXT)"

    /**
     * bookmark表
     */
    const val TABLE_BOOKMARK = "bookmark_table"
    const val BOOKMARK_ID = "bookmark_id"
    const val BOOKMARK_SIGN = "bookmark_sign"
    const val BOOKMARK_FOLDER_ID = "bookmark_folder_id"
    const val BOOKMARK_NAME = "bookmark_name"
    const val BOOKMARK_URL = "bookmark_url"
    const val BOOKMARK_ICON_BITMAP = "bookmark_icon_bitmap"
    const val CREATE_BOOKMARK_TABLE =
        "CREATE TABLE $TABLE_BOOKMARK ($BOOKMARK_ID INTEGER PRIMARY KEY, $BOOKMARK_SIGN TEXT UNIQUE, $BOOKMARK_NAME TEXT, $BOOKMARK_URL TEXT, $BOOKMARK_FOLDER_ID INTEGER, $BOOKMARK_ICON_BITMAP TEXT)"

}