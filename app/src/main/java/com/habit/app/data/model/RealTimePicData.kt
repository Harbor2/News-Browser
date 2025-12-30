package com.habit.app.data.model

import android.graphics.Bitmap

data class RealTimePicData(
    val isBitmap: Boolean = false,
    val bitmap: Bitmap? = null,
    val byteArray: ByteArray? = null,
    val width: Int = 0,
    val height: Int = 0,
    val isRotate: Boolean = false
)
