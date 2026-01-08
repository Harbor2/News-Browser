package com.habit.app.data.model

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class RealTimeNewsData(
    var title: String = "",
    var newsUrl: String = "",
    var pubTime: String = "",
    var thumbUrl: String = ""
) {
    override fun toString(): String {
        return "RealTimeNewsData(title='$title', newsUrl='$newsUrl', pubTime='${getFormatPubTime()}', thumbUrl='$thumbUrl')"
    }

    fun isValid(): Boolean {
        return title.isNotEmpty() && newsUrl.isNotEmpty() && thumbUrl.isNotEmpty()
    }

    fun getFormatPubTime(): String {
        return try {
            val timeParser = DateTimeFormatter.RFC_1123_DATE_TIME
            val zonedFateTime = ZonedDateTime.parse(pubTime, timeParser)

            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm")
            zonedFateTime.withZoneSameInstant(java.time.ZoneId.systemDefault()).format(formatter)
        } catch (e: Exception) {
            ""
        }
    }
}