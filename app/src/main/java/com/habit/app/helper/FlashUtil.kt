package com.habit.app.helper

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log
import com.habit.app.data.TAG

/**
 * 手机闪光灯工具类
 */
object FlashUtil {
    var isFlashOn = false

    fun changeFlashState(context: Context) {
        Log.d(TAG, "改变flash状态")
        if (isFlashOn) {
            turnOffFlashLight(context)
        } else {
            turnOnFlashLight(context)
        }
    }

    /**
     * 打开闪光灯
     */
    private fun turnOnFlashLight(context: Context) {
        if (!UtilHelper.isDeviceFlashSupported(context)) {
            return
        }
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, true)
            isFlashOn = true
        } catch (e: Exception) {
            Log.e(TAG, "FlashUtil: turnOnFlashLight()发生异常 ${e.message}")
        }
    }

    /**
     * 关闭闪光灯
     */
    private fun turnOffFlashLight(context: Context) {
        if (!UtilHelper.isDeviceFlashSupported(context)) {
            return
        }
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, false)
            isFlashOn = false
        } catch (e: Exception) {
            Log.e(TAG, "FlashUtil: turnOffFlashLight()发生异常 ${e.message}")
        }
    }
}