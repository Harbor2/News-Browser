package com.habit.app.data

const val TAG = "NEWS_BROWSER"
const val TAG_TEST = "NEWS_BROWSER_TEST"

/**
 * 网站Agen 桌面端 移动端
 */
const val USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
const val USER_AGENT_PHONE = "Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"

/**
 * 搜索引擎常量
 */
const val ENGINE_GOOGLE = "Google"
const val ENGINE_BING = "Bing"
const val ENGINE_YAHOO = "Yahoo"
const val ENGINE_DUCKDUCK = "Duckduck"
const val ENGINE_YANDEX = "Yandex"
const val ENGINE_BAIDU = "Baidu"

const val WEBVIEW_DEFAULT_NAME = "Home"



const val ENGINE_GOOGLE_URL = "https://www.google.com/search?q="
const val ENGINE_DUCKDUCKGO_URL = "https://duckduckgo.com/?q="
const val ENGINE_YAHOO_URL = "https://search.yahoo.com/search?p="
const val ENGINE_YANDEX_URL = "https://yandex.com/search/?text="
const val ENGINE_BING_URL = "https://www.bing.com/search?q="
const val ENGINE_BAIDU_URL = "https://www.baidu.com/s?wd="


/**
 * 快捷功能url List
 */
var assessUrlList = listOf("")

/**
 * folder url 操作
 */
const val OPTION_DELETE = "option_delete"
const val OPTION_OPEN_IN_NEW_TAB = "option_open_in_new_tab"
const val OPTION_REMOVE = "option_remove"
const val OPTION_EDIT = "option_edit"
const val OPTION_ADD_TO_NAVI = "option_add_to_navi"
const val OPTION_ADD_TO_HOME = "option_add_to_home"
const val OPTION_ADD_TO_BOOKMARK = "option_add_to_bookmark"
const val OPTION_SELECT = "option_select"