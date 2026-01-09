package com.habit.app.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.zxing.Result
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.databinding.ActivityCameraScanBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.helper.VibrateUtil
import com.habit.app.ui.MainActivity
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.dialog.ScanResultDialog
import com.habit.app.ui.home.controller.MediaPlayerController
import com.habit.app.ui.home.controller.ScannerController
import com.wyz.emlibrary.util.immersiveWindow


class CameraScanActivity : BaseActivity() {
    private lateinit var binding: ActivityCameraScanBinding
    private val loadingObserve = MutableLiveData(false)
    private val flashObserve = MutableLiveData(false)
    private lateinit var scannerController: ScannerController
    private lateinit var mediaPlayController: MediaPlayerController
    private var surfaceCreated: Boolean = false
    private var isResultPageOpen: Boolean = false
    private var resultDialog: ScanResultDialog? = null

    private val scannerCallback = object : ScannerController.ScannerCallback {
        override fun onScanResult(result: Result) {
            processScannerResult(result)
        }

        override fun onScanLocalPicFailed() {
            scannerController.responseScanResult = true
            loadingObserve.value = false
            UtilHelper.showToast(this@CameraScanActivity, getString(R.string.toast_failed))
        }
    }

    private val selectPicLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadingObserve.value = true
                scannerController.decodeLocalBitmap(uri)
            }
        } else {
            scannerController.responseScanResult = true
        }
    }

    private val surfaceViewCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        }

        override fun surfaceCreated(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceCreated")
            surfaceCreated = true
            scannerController.isFrontCamera?.let {
                binding.animationView.resumeAnimation()
                scannerController.openCamera(it)
            }
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            Log.d(TAG, "surfaceDestroyed")
            surfaceCreated = false
            binding.animationView.pauseAnimation()
            scannerController.closeCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, ThemeManager.isNightTheme(), binding.containerNavi)
        scannerController = ScannerController(lifecycleScope, this, binding).apply {
            this.mCallback = scannerCallback
        }
        mediaPlayController = MediaPlayerController(this)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        flashObserve.observe(this) { value ->
            binding.ivFlash.setImageResource(if (value) R.drawable.iv_scan_flash_on else R.drawable.iv_scan_flash_off)
            Log.d(TAG, "闪光灯开关：$value")
            if (value) {
                scannerController.turnOnFlash()
            } else {
                scannerController.turnOffFlash()
            }
        }
        loadingObserve.observe(this) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {
        scannerController.checkDeviceCamera()
        if (scannerController.isFrontCamera == null) {
            UtilHelper.showToast(this, getString(R.string.toast_device_not_support_camera))
            return
        }
        // 初始化解析二维码类型
        scannerController.initMultiFormatReader()

        binding.surfaceView.holder.addCallback(surfaceViewCallback)
    }

    private fun initListener() {
        binding.ivNaviBack.setOnClickListener {
            finish()
        }
        binding.ivFlash.setOnClickListener {
            Log.d(TAG, "当前闪光灯状态：${flashObserve.value}")
            flashObserve.value = !flashObserve.value!!
        }
        binding.ivGallery.setOnClickListener {
            scannerController.responseScanResult = false
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
                selectPicLauncher.launch(this)
            }
        }
    }

    private fun processScannerResult(result: Result) {
        val decodedText = result.text
        if (decodedText.isNullOrEmpty() || isResultPageOpen) {
            return
        }
        binding.animationView.pauseAnimation()
        isResultPageOpen = true
        loadingObserve.value = false
        scannerController.responseScanResult = false
        // 播放声音
        mediaPlayController.tryPlayBeep()
        // 振动
        VibrateUtil.tryVibrate(this)
        // 解析二维码内容
        showQRInfoDialog(result.text)
    }

    /**
     * 显示二维码信息弹窗
     */
    private fun showQRInfoDialog(text: String) {
        Log.d(TAG, "解析二维码内容：${text}")
        resultDialog = ScanResultDialog.tryShowDialog(this)?.apply {
            this.setData(text)
            setOnDismissListener {
                resultDialog = null
                isResultPageOpen = false
                scannerController.responseScanResult = true
                this@CameraScanActivity.binding.animationView.resumeAnimation()
            }
            this.mCallback = object : ScanResultDialog.DialogCallback {
                override fun onCopy(text: String) {
                    UtilHelper.copyToClipboard(this@CameraScanActivity, text)
                }

                override fun onJump(text: String) {
                    val intent = Intent(this@CameraScanActivity, MainActivity::class.java).apply {
                        putExtra("post_url", text)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (surfaceCreated
            && scannerController.isFrontCamera != null) {
            binding.animationView.resumeAnimation()
            scannerController.openCamera(scannerController.isFrontCamera!!)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.animationView.pauseAnimation()
        scannerController.closeCamera()
        flashObserve.value = false
    }

    private fun updateUIConfig() {
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        resultDialog?.updateThemeUI()
    }

    override fun onThemeChanged(theme: String) {
        updateUIConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, CameraScanActivity::class.java))
        }
    }
}