package com.habit.app.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.habit.app.R
import com.habit.app.helper.ThemeManager

data class HistoryData(
    val name: String = "",
    val url: String = "",
    val webIconPath: String = "",
    val timeStamp: Long = System.currentTimeMillis()
) {
    private var webIconBitmap: Bitmap? = null


    fun getIconBitmap(context: Context): Bitmap {
        if (webIconBitmap != null) return webIconBitmap!!
        BitmapFactory.decodeFile(webIconPath)?.apply {
            webIconBitmap = this
            return this
        }
        return BitmapFactory.decodeResource(context.resources, ThemeManager.getSkinImageResId(R.drawable.iv_web_icon_default))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryData

        if (timeStamp != other.timeStamp) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeStamp.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }

    override fun toString(): String {
        return "HistoryData(name='$name', url='$url', webIconPath='$webIconPath', timeStamp=$timeStamp)"
    }
}
