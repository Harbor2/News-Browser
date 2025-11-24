package com.habit.app.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.habit.app.R
import com.habit.app.helper.ThemeManager
import java.util.UUID

data class BookmarkData(
    val sign: String = UUID.randomUUID().toString(),
    val name: String = "",
    val url: String = "",
    val folderId: Int = -1,
    val webIconPath: String = "",
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

        other as BookmarkData

        return url == other.url
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    override fun toString(): String {
        return "BookmarkData(url='$url', name='$name')"
    }
}
