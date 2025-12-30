package com.habit.app.helper

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.habit.app.data.TAG

/**
 * 手机振动工具类
 */
object VibrateUtil {

    fun cancelVibrate(context: Context) {
        val vibrator = getVibrateService(context)
        vibrator.cancel()
    }

    fun tryVibrate(context: Context) {
        val vibrator = getVibrateService(context)
        // 设备不支持振动
        if (!vibrator.hasVibrator()) {
            Log.d(TAG, "设备不支持震动")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 振动300 等待1000
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(100)
        }
    }

    private fun getVibrateService(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}