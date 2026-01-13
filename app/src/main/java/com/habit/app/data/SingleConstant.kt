package com.habit.app.data

const val TAG = "NEWS_BROWSER"
const val TAG_TEST = "NEWS_BROWSER_TEST"

val imageTypes = arrayOf("jpg", "jpeg", "png", "raw", "bmp", "gif", "tif", "svg", "ico", "webp")
val videoTypes = arrayOf("mp4", "avi", "wmv", "flv", "rmvb", "rm", "mov", "3gp", "mpeg")
val audioTypes = arrayOf("mp3","ogg","wav","wma","ape","flac","aac","midi", "m4a")
val docTypes = arrayOf("doc","docx", "dotm", "dotx", "pptx", "ppt", "xls","xlsx", "xlsm", "xltm", "pdf", "txt")
val zipTypes = arrayOf("rar", "7z", "zip", "gz", "tar", "bz")
val apkTypes = arrayOf("apk")

/**
 * 最大web snap 数量
 */
const val MAX_SNAP_COUNT = 20

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

/**
 * download file menu
 */
const val MENU_SHARE = "option_share"
const val MENU_DELETE = "option_delete"
const val MENU_RENAME = "option_rename"
const val MENU_SELECT = "option_select"

/**
 * 图片菜单 menu
 */
const val IMAGE_MENU_COPY_ADDRESS = "option_copy_address"
const val IMAGE_MENU_DOWNLOAD_IMAGE = "option_download_image"
const val IMAGE_MENU_SHARE_IMAGE = "option_share_image"

/**
 * 最大同时下载任务数
 */
const val MAX_COUNT_DOWNLOAD_TASKS = 5

/**
 * 文件下载中名字标识符
 */
const val DOWNLOADING_NAME_PREFIX = "downloading_"

/**
 * 新闻分类
 */
const val NEWS_CATEGORY_WORLD = "World"
const val NEWS_CATEGORY_POLITICS = "Politics"
const val NEWS_CATEGORY_SCIENCE = "Science"
const val NEWS_CATEGORY_HEALTH = "Health"
const val NEWS_CATEGORY_SPORTS = "Sports"
const val NEWS_CATEGORY_TECHNOLOGY = "Technology"
const val NEWS_CATEGORY_BUSINESS = "Business"

/**
 * BBC 新闻
 * 全球：https://feeds.bbci.co.uk/news/world/rss.xml
 * 政治：https://feeds.bbci.co.uk/news/politics/rss.xml
 * 科学：https://feeds.bbci.co.uk/news/science_and_environment/rss.xml
 * 健康：https://feeds.bbci.co.uk/news/health/rss.xml
 * 体育：https://feeds.bbci.co.uk/sport/rss.xml
 * 科技：https://feeds.bbci.co.uk/news/technology/rss.xml
 * 商业：https://feeds.bbci.co.uk/news/business/rss.xml
 *
 * FOX 新闻
 * 全球：
 * 政治：https://moxie.foxnews.com/google-publisher/politics.xml
 * 科学：https://moxie.foxnews.com/google-publisher/science.xml
 * 健康：https://moxie.foxnews.com/google-publisher/health.xml
 * 体育：https://moxie.foxnews.com/google-publisher/sports.xml
 * 科技：https://moxie.foxnews.com/google-publisher/tech.xml
 *
 * NYTime 新闻 （link为html）
 * 全球：https://rss.nytimes.com/services/xml/rss/nyt/World.xml
 * 政治：https://rss.nytimes.com/services/xml/rss/nyt/Politics.xml
 * 科学：https://rss.nytimes.com/services/xml/rss/nyt/Science.xml
 * 健康：https://rss.nytimes.com/services/xml/rss/nyt/Health.xml
 * 体育：https://rss.nytimes.com/services/xml/rss/nyt/Sports.xml
 * 科技：https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml
 * 商业：https://rss.nytimes.com/services/xml/rss/nyt/Business.xml
 *
 * 卫报：
 * 全球: https://www.theguardian.com/world/rss
 * 政治：https://www.theguardian.com/politics/rss
 * 科学：https://www.theguardian.com/science/rss
 * 健康：https://www.theguardian.com/society/health/rss
 * 体育：https://www.theguardian.com/sport/rss
 * 科技：https://www.theguardian.com/technology/rss
 * 商业：https://www.theguardian.com/business/rss
 */

val worldList = arrayListOf(
    "https://feeds.bbci.co.uk/news/world/rss.xml",
    "https://moxie.foxnews.com/google-publisher/world.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/World.xml",
    "https://www.theguardian.com/world/rss",
)

val politicsList = arrayListOf(
    "https://feeds.bbci.co.uk/news/politics/rss.xml",
    "https://moxie.foxnews.com/google-publisher/politics.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/Politics.xml",
    "https://www.theguardian.com/politics/rss",
)

val scienceList = arrayListOf(
    "https://feeds.bbci.co.uk/news/science_and_environment/rss.xml",
    "https://moxie.foxnews.com/google-publisher/science.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/Science.xml",
    "https://www.theguardian.com/science/rss",
)

val healthList = arrayListOf(
    "https://feeds.bbci.co.uk/news/health/rss.xml",
    "https://moxie.foxnews.com/google-publisher/health.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/Health.xml",
    "https://www.theguardian.com/society/health/rss",
)

val sportsList = arrayListOf(
    "https://feeds.bbci.co.uk/sport/rss.xml",
    "https://moxie.foxnews.com/google-publisher/sports.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/Sports.xml",
    "https://www.theguardian.com/sport/rss",
)
val techList = arrayListOf(
    "https://feeds.bbci.co.uk/news/technology/rss.xml",
    "https://moxie.foxnews.com/google-publisher/tech.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml",
    "https://www.theguardian.com/technology/rss",
)
val businessList = arrayListOf(
    "https://feeds.bbci.co.uk/news/business/rss.xml",
    "https://rss.nytimes.com/services/xml/rss/nyt/Business.xml",
    "https://www.theguardian.com/business/rss",
)

