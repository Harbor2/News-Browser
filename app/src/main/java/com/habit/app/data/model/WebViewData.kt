package com.habit.app.data.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.habit.app.R
import com.habit.app.helper.ThemeManager
import java.util.UUID

data class WebViewData(
    val name: String = "",
    val sign: String = UUID.randomUUID().toString(),
    val url: String = "",
    var isPhoneMode: Boolean? = null,
    var isPrivacyMode: Boolean? = null,
    val coverBitmapPath: String = "",
    val webIconPath: String = ""
) {
    var mSelect: Boolean = false
    private var coverBitmap: Bitmap? = null
    private var webIconBitmap: Bitmap? = null

    fun setCoverBitmap(bitmap: Bitmap) {
        coverBitmap = bitmap
    }

    fun getCoverBitmap(context: Context): Bitmap {
        if (coverBitmap != null) return coverBitmap!!
        BitmapFactory.decodeFile(coverBitmapPath)?.apply {
            coverBitmap = this
            return this
        }
        return BitmapFactory.decodeResource(context.resources, ThemeManager.getSkinImageResId(R.drawable.iv_pic_test))
    }

    fun setWebIconBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        webIconBitmap = bitmap
    }

    fun getWebIconBitmap(context: Context): Bitmap {
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

        other as WebViewData

        return sign == other.sign
    }

    override fun hashCode(): Int {
        return sign.hashCode()
    }

    override fun toString(): String {
        return "WebViewData(name='$name', sign='$sign', url='$url', isPhoneMode=$isPhoneMode, isPrivacyMode=$isPrivacyMode, coverBitmapPath=$coverBitmapPath, webIconPath=$webIconPath, mSelect=$mSelect, coverBitmap=$coverBitmap, webIconBitmap=$webIconBitmap)"
    }

}
