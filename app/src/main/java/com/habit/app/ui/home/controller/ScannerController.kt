package com.habit.app.ui.home.controller

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.habit.app.data.TAG
import com.habit.app.data.model.RealTimePicData
import com.habit.app.databinding.ActivityCameraScanBinding
import com.habit.app.helper.UtilHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Hashtable
import java.util.Vector

class ScannerController(
    private val mScope: CoroutineScope,
    private val context: Activity,
    private val binding: ActivityCameraScanBinding
) {
    private var camera: Camera? = null
    private var multiFormatReader: MultiFormatReader? = null
    /**
     * false 后摄像头
     * true 前摄像头
     * null 不支持
     */
    var isFrontCamera: Boolean? = null
    /**
     * 是否能够切换摄像头
     */
    private var canSwitchCamera: Boolean = false
    var responseScanResult: Boolean = true
    var mCallback: ScannerCallback? = null
    private var decodeJob: Job? = null

    /**
     * 当前相机缩放倍数
     */
    private var curCameraZoom: Int = 0

    fun initMultiFormatReader() {
        val hints = Hashtable<DecodeHintType, Any>()
        val decodeFormats = Vector<BarcodeFormat>()
        // 添加二维码和条形码支持
        decodeFormats.add(BarcodeFormat.QR_CODE)  // 二维码
        decodeFormats.add(BarcodeFormat.CODE_128) // 128 条形码
        decodeFormats.add(BarcodeFormat.EAN_13)   // EAN-13
        decodeFormats.add(BarcodeFormat.EAN_8)    // EAN-8
        decodeFormats.add(BarcodeFormat.UPC_A)    // UPC-A
        decodeFormats.add(BarcodeFormat.UPC_E)    // UPC-E
        decodeFormats.add(BarcodeFormat.CODE_39)  // Code 39
        decodeFormats.add(BarcodeFormat.CODE_93)  // Code 93
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        hints[DecodeHintType.TRY_HARDER] = true // 增强解析能力
        // 初始化解码器
        multiFormatReader = MultiFormatReader()
        multiFormatReader!!.setHints(hints)
    }

    fun openCamera(isFrontCamera: Boolean = false) {
        Log.d(TAG, "openCamera: $isFrontCamera")
        try {
            camera = Camera.open(if (isFrontCamera) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK)
            // 获取相机的方向信息
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(
                if (isFrontCamera) Camera.CameraInfo.CAMERA_FACING_FRONT else Camera.CameraInfo.CAMERA_FACING_BACK, info
            )
            // 调整相机的旋转角度
            val adjustedRotation = if (isFrontCamera) {
                (info.orientation + 180) % 360
            } else {
                (info.orientation) % 360
            }
            // 设置相机旋转
            camera?.setDisplayOrientation(adjustedRotation)
            // 设置自动对焦
            val parameters = camera?.parameters
            if (parameters?.supportedFocusModes?.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) == true) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                camera?.parameters = parameters
            }
            // 设置缩放倍数
            camera?.parameters?.let {
                if (it.isZoomSupported) {
                    it.zoom = curCameraZoom
                    camera?.parameters = it
                }
            }
            // 设置预览显示
            camera?.setPreviewDisplay(binding.surfaceView.holder)
            camera?.startPreview()
            startPreview()
        } catch (e: Exception) {
            Log.e(TAG, "摄像头打开异常：${e.message}")
        }
    }

    fun closeCamera() {
        try {
            Log.d(TAG, "closeCamera")
            camera?.setPreviewCallback(null)
            camera?.stopPreview()
            camera?.release()
            camera = null
        } catch (e: Exception) {
            Log.e(TAG, "摄像头关闭异常：${e.message}")
        }
    }

    fun turnOnFlash() {
        camera?.apply {
            val params = parameters
            // 不支持闪光灯
            if (params.supportedFlashModes?.contains(Camera.Parameters.FLASH_MODE_TORCH) != true) {
                return
            }
            if (params.flashMode == Camera.Parameters.FLASH_MODE_TORCH) {
                return
            }
            params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            parameters = params
        }
    }

    fun turnOffFlash() {
        camera?.apply {
            val params = parameters
            // 不支持闪光灯
            if (params.supportedFlashModes?.contains(Camera.Parameters.FLASH_MODE_TORCH) != true) {
                return
            }
            if (params.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                return
            }
            params.flashMode = Camera.Parameters.FLASH_MODE_OFF
            parameters = params
        }
    }

    private fun startPreview() {
        var fontCount = 0L
        // 通过 Camera 设置预览回调
        camera?.setPreviewCallback { data, _ ->
            fontCount++
            val menu = fontCount % 5
            if (menu != 0L || !responseScanResult) {
                return@setPreviewCallback
            }
            if (decodeJob?.isActive == true) {
                return@setPreviewCallback
            }

            camera?.parameters?.previewSize?.let { size ->
                val width = size.width
                val height = size.height
                decodeJob = mScope.launch(Dispatchers.IO) {
                    var dataCopy: ByteArray? = null
                    var newData: ByteArray? = null
                    var isRotate = false
                    try {
                        var decodeResult = decodeQRCode(data, width, height)
                        if (decodeResult == null) {
                            // 旋转图片（条形码解码需要确保方向正确）
                            isRotate = true
                            newData = rotateYUV420Degree90(data, width, height)
                            decodeResult = decodeQRCode(newData, width, height)
                        }
                        decodeResult?.let { result ->
                            // 扫描成功，返回二维码数据
                            withContext(Dispatchers.Main) {
                                dataCopy = if (isRotate) newData?.copyOf() else data?.copyOf()
                                val obj = RealTimePicData(false, null, dataCopy, if (isRotate) height else width, if (isRotate) width else height, isRotate)
                                mCallback?.onScanResult(result, obj)
                                Log.d(TAG, "发现二维码内容：${result.text}")
                            }
                        } ?: run {
                            Log.d(TAG, "没有发现二维码内容")
                        }
                    } finally {
                        data.fill(0)
                        newData = null
                    }
                }
            }
        }
    }

    /**
     * 解码二维码
     */
    private fun decodeQRCode(data: ByteArray, width: Int, height: Int): Result? {
        try {
            val source = PlanarYUVLuminanceSource(data,
                width,
                height,
                0, 0,
                width,
                height,
                false
            )
            // 创建 BinaryBitmap
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            return multiFormatReader?.decode(bitmap)
        } catch (e: Exception) {
            // 解码失败，继续监听相机数据
            e.message?.let {
                Log.e(TAG, "解码异常：$it")
            }
            return null
        }
    }

    /**
     * 旋转 YUV 数据
     */
    private fun rotateYUV420Degree90(data: ByteArray, width: Int, height: Int): ByteArray {
        val yuv = ByteArray(data.size)
        var i = 0

        // 旋转 Y 分量
        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                yuv[i++] = data[y * width + x]
            }
        }
        // 旋转 UV 分量
        val uvSize = width * height
        i = uvSize
        val uvWidth = width / 2
        val uvHeight = height / 2
        // UV 平面旋转
        for (x in 0 until uvWidth) {
            for (y in uvHeight - 1 downTo 0) {
                yuv[i++] = data[uvSize + (y * uvWidth) + x]     // U 分量
                yuv[i++] = data[uvSize + (y * uvWidth) + x + 1] // V 分量
            }
        }
        return yuv
    }

    /**
     * 三次机会解析二维码
     */
    fun decodeLocalBitmap(uri: Uri) {
        mScope.launch(Dispatchers.IO) {
            var isRotate = false
            var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            // BitmapFactory会自动检查Exit信息并设置旋转方向，需要复原
            val rotation = getExifRotation(context, uri)
            if (rotation != 0) {
                bitmap = UtilHelper.rotateBitmap(bitmap, rotation.toFloat())
            }
            Log.d(TAG, "图库选择bitmap宽高信息：${bitmap.width},${bitmap.height}")

            var result = decodeQRCode(bitmap)
            if (result == null) {
                result = decodeBitmap(bitmap)
            }
            if (result == null) {
                isRotate = true
                bitmap = UtilHelper.rotateBitmap(bitmap, 90f)
                result = decodeBitmap(bitmap)
            }
            withContext(Dispatchers.Main) {
                if (result == null) {
                    Log.d(TAG, "解析本地图片失败")
                    mCallback?.onScanLocalPicFailed()
                } else {
                    Log.d(TAG, "解析本地图片：${result.text}")
                    val obj = RealTimePicData(true, bitmap, null, 0, 0, isRotate)
                    mCallback?.onScanResult(result, obj)
                }
            }
        }
    }

    private fun decodeBitmap(bitmap: Bitmap): Result? {
        return try {
            val intArray = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            // 将 ARGB 转换为灰度图像的 Y 分量
            val yuv = ByteArray(bitmap.width * bitmap.height)
            for (i in intArray.indices) {
                val color = intArray[i]
                val r = (color shr 16) and 0xff
                val g = (color shr 8) and 0xff
                val b = color and 0xff
                yuv[i] = (0.299 * r + 0.587 * g + 0.114 * b).toInt().toByte() // 灰度公式
            }
            val source = PlanarYUVLuminanceSource(yuv, bitmap.width, bitmap.height, 0, 0, bitmap.width, bitmap.height, false)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            multiFormatReader?.decode(binaryBitmap)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * QRCodeReader解析二维码
     */
    private fun decodeQRCode(bitmap: Bitmap): Result? {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binarizer = HybridBinarizer(source)
        val binaryBitmap = BinaryBitmap(binarizer)
        return try {
            QRCodeReader().decode(binaryBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()

            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }

    fun checkDeviceCamera() {
        val cameraSupportPair = UtilHelper.isDeviceCameraSupported(context)
        isFrontCamera = when {
            cameraSupportPair.first && cameraSupportPair.second -> {
                canSwitchCamera = true
                false
            }
            cameraSupportPair.first -> {
                canSwitchCamera = false
                false
            }
            cameraSupportPair.second -> {
                canSwitchCamera = false
                true
            }
            else -> {
                canSwitchCamera = false
                null
            }
        }
    }

    interface ScannerCallback {
        fun onScanResult(result: Result, obj: RealTimePicData?)

        fun onScanLocalPicFailed()
    }
}