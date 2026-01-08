package com.habit.app.data.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

data class RealTimeNewsData(
    var category: String = CATEGORY_WORLD,
    var guid: String = UUID.randomUUID().toString(),
    var title: String = "",
    var newsUrl: String = "",
    var pubTime: Long = -1,
    var thumbUrl: String = ""
) {
    companion object {
        const val CATEGORY_WORLD = "WORLD"
        const val CATEGORY_POLITICS = "POLITICS"
        const val CATEGORY_SCIENCE = "SCIENCE"
        const val CATEGORY_HEALTH = "HEALTH"
        const val CATEGORY_SPORTS = "SPORTS"
        const val CATEGORY_TECH = "TECH"
    }

    fun isValid(): Boolean {
        return title.isNotEmpty() && newsUrl.isNotEmpty() && thumbUrl.isNotEmpty()
    }

    fun transFormatPubTime(timeStr: String) {
        try {
            val timeParser = DateTimeFormatter.RFC_1123_DATE_TIME
            val zonedDateTime = ZonedDateTime.parse(timeStr, timeParser)
            // 转为本地时区的 Instant，再转毫秒
            pubTime = zonedDateTime.toInstant().toEpochMilli()
        } catch (e: Exception) {
            ""
            pubTime = -1
        }
    }

    override fun toString(): String {
        return "RealTimeNewsData(category='$category', guid='$guid', title='$title', newsUrl='$newsUrl', pubTime=$pubTime, thumbUrl='$thumbUrl')"
    }
}