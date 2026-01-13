package com.habit.app.helper

import android.Manifest
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.reflect.TypeToken
import com.habit.app.MyApplication
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.data.apkTypes
import com.habit.app.data.audioTypes
import com.habit.app.data.docTypes
import com.habit.app.data.imageTypes
import com.habit.app.data.model.AccessSingleData
import com.habit.app.data.model.DownloadFileData
import com.habit.app.data.videoTypes
import com.habit.app.data.zipTypes
import com.wyz.emlibrary.util.EMUtil
import java.io.File
import java.net.URLDecoder
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.random.Random
import androidx.core.net.toUri
import com.habit.app.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.FileOutputStream

object UtilHelper {

    fun getAppVersionName(context: Context): String {
        var versionName: String = ""
        try {
            versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return String.format("%s", versionName)
    }

    fun showToast(context: Context, str: String) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    /**
     * 毫秒时间戳转日期
     */
    fun convertMillisToLocalDate(timestampMillis: Long): LocalDate {
        val instant = Instant.ofEpochMilli(timestampMillis)
        // 使用系统默认时区（也可以替换为 ZoneId.of("UTC")）
        return instant.atZone(ZoneId.systemDefault()).toLocalDate()
    }

    /**
     * 根据日期返回一个随机种子
     */
    fun getRandomByDay(): Random {
        return Random(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
    }

    fun checkWriteStoragePerm(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun checkReadStoragePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * 资源名称转id
     */
    fun getResIdByName(context: Context, resName: String, default: Int = R.drawable.iv_engine_icon_google, resType: String = "drawable"): Int {
        // 查找有效资源id
        return try {
            context.resources.getIdentifier(resName, resType, context.packageName)
        } catch (e: Exception) {
            default
        }
    }

    /**
     * 资源id转名称
     */
    fun getResNameById(context: Context, resId: Int, default: String? = null): String? {
        return try {
            context.resources.getResourceEntryName(resId)
        } catch (e: Exception) {
            default
        }
    }

    fun getDefaultHomeAccessList(context: Context): ArrayList<AccessSingleData> {
        return arrayListOf(
            AccessSingleData("iv_access_single_file", context.getString(R.string.text_download), "file").apply {
                sortIndex = 0
                isSpecial = true },
            AccessSingleData("iv_access_single_game", context.getString(R.string.text_history), "game").apply {
                sortIndex = 1
                isSpecial = true },
            AccessSingleData("iv_access_single_bookmark", context.getString(R.string.text_bookmark), "bookmark").apply {
                sortIndex = 2
                isSpecial = true },
            AccessSingleData("iv_access_single_instagram", context.getString(R.string.text_instagram), "https://www.instagram.com").apply {
                sortIndex = 3 },
            AccessSingleData("iv_access_single_tiktok", context.getString(R.string.text_tiktok), "https://www.tiktok.com").apply {
                sortIndex = 4 },
            AccessSingleData("iv_access_single_youtube", context.getString(R.string.text_youtube), "https://www.youtube.com").apply {
                sortIndex = 5 },
            AccessSingleData("iv_access_single_twitter", context.getString(R.string.text_twitter), "https://twitter.com").apply {
                sortIndex = 6 },
            AccessSingleData("iv_access_single_facebook", context.getString(R.string.text_facebook), "https://www.facebook.com").apply {
                sortIndex = 7 },
            AccessSingleData("iv_access_single_amazon", context.getString(R.string.text_amazon), "https://www.amazon.com").apply {
                sortIndex = 8 },
            AccessSingleData("iv_access_single_add", context.getString(R.string.text_add), "add").apply {
                sortIndex = 9
                isSpecial = true }
        )
    }

    /**
     * 获取全部Access列表
     */
    fun getAllAccessList(context: Context): ArrayList<AccessSingleData> {
        return arrayListOf(
            AccessSingleData("iv_access_single_instagram", context.getString(R.string.text_instagram), "https://www.instagram.com"),
            AccessSingleData("iv_access_single_facebook", context.getString(R.string.text_facebook), "https://www.facebook.com"),
            AccessSingleData("iv_access_single_reddit", context.getString(R.string.text_reddit), "https://www.reddit.com"),
            AccessSingleData("iv_access_single_pinterest", context.getString(R.string.text_pinterest), "https://www.pinterest.com"),
            AccessSingleData("iv_access_single_whatsapp", context.getString(R.string.text_whatsapp), "https://www.whatsapp.com"),
            AccessSingleData("iv_access_single_twitter", context.getString(R.string.text_twitter), "https://twitter.com"),
            AccessSingleData("iv_access_single_snapchat", context.getString(R.string.text_snapchat), "https://www.snapchat.com"),
            AccessSingleData("iv_access_single_netflix", context.getString(R.string.text_netflix), "https://www.netflix.com"),
            AccessSingleData("iv_access_single_youtube", context.getString(R.string.text_youtube), "https://www.youtube.com"),
            AccessSingleData("iv_access_single_vimeo", context.getString(R.string.text_vimeo), "https://vimeo.com"),
            AccessSingleData("iv_access_single_dailymotion", context.getString(R.string.text_dailymotion), "https://www.dailymotion.com"),
            AccessSingleData("iv_access_single_cnn", context.getString(R.string.text_cnn), "https://www.cnn.com"),
            AccessSingleData("iv_access_single_foxnews", context.getString(R.string.text_fox_news), "https://www.foxnews.com"),
            AccessSingleData("iv_access_single_nytimes", context.getString(R.string.text_nytimes), "https://www.nytimes.com"),
            AccessSingleData("iv_access_single_nbcnews", context.getString(R.string.text_nbc_news), "https://www.nbcnews.com"),
            AccessSingleData("iv_access_single_cbc", context.getString(R.string.text_cbc), "https://www.cbc.ca"),
            AccessSingleData("iv_access_single_espn", context.getString(R.string.text_espn), "https://www.espn.com"),
            AccessSingleData("iv_access_single_marca", context.getString(R.string.text_marca), "https://www.marca.com"),
            AccessSingleData("iv_access_single_chatgpt", context.getString(R.string.text_chatgpt), "https://chat.openai.com"),
            AccessSingleData("iv_access_single_linkedin", context.getString(R.string.text_linkedin), "https://www.linkedin.com"),
            AccessSingleData("iv_access_single_wikipedia", context.getString(R.string.text_wikipedia), "https://www.wikipedia.org"),
            AccessSingleData("iv_access_single_tiktok", context.getString(R.string.text_tiktok), "https://www.tiktok.com"),
            AccessSingleData("iv_access_single_twitch", context.getString(R.string.text_twitch), "https://www.twitch.tv"),
            AccessSingleData("iv_access_single_tumblr", context.getString(R.string.text_tumblr), "https://www.tumblr.com"),
            AccessSingleData("iv_access_single_hulu", context.getString(R.string.text_hulu), "https://www.hulu.com"),
            AccessSingleData("iv_access_single_vevo", context.getString(R.string.text_vevo), "https://www.vevo.com"),
            AccessSingleData("iv_access_single_spotify", context.getString(R.string.text_spotify), "https://open.spotify.com"),
            AccessSingleData("iv_access_single_amazon", context.getString(R.string.text_amazon), "https://www.amazon.com"),
            AccessSingleData("iv_access_single_ebay", context.getString(R.string.text_ebay), "https://www.ebay.com"),
            AccessSingleData("iv_access_single_shopee", context.getString(R.string.text_shopee), "https://shopee.com"),
            AccessSingleData("iv_access_single_lazada", context.getString(R.string.text_lazada), "https://www.lazada.com"),
            AccessSingleData("iv_access_single_fantasy", context.getString(R.string.text_fantasy), "https://fantasysports.yahoo.com"),
            AccessSingleData("iv_access_single_yahoosports", context.getString(R.string.text_yahoo_sports), "https://sports.yahoo.com"),
            AccessSingleData("iv_access_single_wwe", context.getString(R.string.text_wwe), "https://www.wwe.com"),
            AccessSingleData("iv_access_single_cricbuzz", context.getString(R.string.text_cricbuzz), "https://www.cricbuzz.com"),
            AccessSingleData("iv_access_single_flashscore", context.getString(R.string.text_flashscore), "https://www.flashscore.com"),
            AccessSingleData("iv_access_single_goal", context.getString(R.string.text_goal), "https://www.goal.com"),
            AccessSingleData("iv_access_single_livescore", context.getString(R.string.text_livescore), "https://www.livescore.com"),
            AccessSingleData("iv_access_single_airbnb", context.getString(R.string.text_airbnb), "https://www.airbnb.com"),
            AccessSingleData("iv_access_single_booking", context.getString(R.string.text_booking), "https://www.booking.com"),
            AccessSingleData("iv_access_single_tripadvisor", context.getString(R.string.text_tripadvisor), "https://www.tripadvisor.com"),
            AccessSingleData("iv_access_single_drugs", context.getString(R.string.text_drugs), "https://www.drugs.com"),
            AccessSingleData("iv_access_single_tnation", context.getString(R.string.text_tnation), "https://www.t-nation.com"),
            AccessSingleData("iv_access_single_who", context.getString(R.string.text_who), "https://www.who.int"),
            AccessSingleData("iv_access_single_healthline", context.getString(R.string.text_healthline), "https://www.healthline.com"),
            AccessSingleData("iv_access_single_bodybuilding", context.getString(R.string.text_bodybuilding), "https://www.bodybuilding.com"),
            AccessSingleData("iv_access_single_kidshealth", context.getString(R.string.text_kids_health), "https://kidshealth.org"),
        )
    }

    /**
     * 判断是否有网络连接
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
    }

    /**
     * 获取view的bitmap
     */
    fun getResizedBitmapFromView(view: View): Bitmap {
        val originalBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(originalBitmap)
        view.draw(canvas)

        val scaledWidth = view.width / 2
        val scaledHeight = view.height / 2

        return Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)
    }

    /**
     * bitmap存储到cache目录
     * @return 返回文件路径
     */
    fun writeBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "webPic"): String? {
        return try {
            val parentFile = File(context.cacheDir, fileName)
            if (!parentFile.exists()) parentFile.mkdirs()
            val bitmapFile = File(parentFile, "${System.currentTimeMillis()}.png")
            bitmapFile.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 75, out)
            }
            return bitmapFile.absolutePath
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "图片缓存到cache目录失败: ${e.message}")
            null
        }
    }

    fun shareUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        context.startActivity(Intent.createChooser(intent, "share"))
    }

    /**
     * 添加home access
     */
    fun homeAddAccessItem(context: Context, title: String, url: String, iconPath: String?): Boolean {
        // 历史记录
        val cacheInfo = KeyValueManager.getValueByKey(KeyValueManager.KEY_HOME_ACCESS_INFO) ?: ""
        val selectedAccess = if (cacheInfo.isEmpty()) {
            getDefaultHomeAccessList(context)
        } else {
            GsonUtil.gson.fromJson(cacheInfo, object : TypeToken<ArrayList<AccessSingleData>>() {}.type)
        }

        if (selectedAccess.size >= 15) {
            showToast(context, context.getString(R.string.toast_can_not_add_more))
            return false
        }

        val accessData = AccessSingleData(
            iconResName = iconPath ?: "iv_web_icon_default",
            name = title,
            linkUrl = url,
        )

        if (selectedAccess.contains(accessData)) {
            showToast(context, context.getString(R.string.toast_access_exist))
            return false
        }

        selectedAccess.add(selectedAccess.size - 1, accessData)

        // 重新设置sortIndex
        selectedAccess.forEachIndexed { index, accessSingleData ->
            accessSingleData.sortIndex = index
        }
        KeyValueManager.saveValueWithKey(KeyValueManager.KEY_HOME_ACCESS_INFO, GsonUtil.gson.toJson(selectedAccess))
        return true
    }

    /**
     * 判断是否是当前自然日
     */
    fun getTodayDate(): String {
        val now = System.currentTimeMillis()
        return EMUtil.formatDateFromTimestamp("dd/MM/yyyy", now)
    }

    /**
     * 跳转wifisetting
     */
    fun jumpWifiSetting(context: Context) {
        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
    }

    /**
     * url解码
     */
    fun decodeUrlCode(url: String): String {
        try {
            return URLDecoder.decode(url, "UTF-8")
        } catch (e: Exception) {
            Log.e(TAG, "url解码失败: ${e.message}")
        }
        return url
    }

    fun getFileTypeByName(name: String): String {
        val fileExtension = name.substringAfterLast('.', "").lowercase()
        if (fileExtension.isEmpty()) {
            return DownloadFileData.TYPE_UNKNOWN
        }
        return when {
            imageTypes.contains(fileExtension) -> DownloadFileData.TYPE_PIC
            videoTypes.contains(fileExtension) -> DownloadFileData.TYPE_VIDEO
            audioTypes.contains(fileExtension) -> DownloadFileData.TYPE_AUDIO
            docTypes.contains(fileExtension) -> DownloadFileData.TYPE_DOC
            zipTypes.contains(fileExtension) -> DownloadFileData.TYPE_ZIP
            apkTypes.contains(fileExtension) -> DownloadFileData.TYPE_APK
            else -> DownloadFileData.TYPE_UNKNOWN
        }
    }

    /**
     * 判断有无相机权限
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun hasMicPerm(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 设备是否支持闪光灯
     */
    fun isDeviceFlashSupported(context: Context): Boolean {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        // 遍历所有摄像头，检查是否有支持闪光灯的摄像头
        for (cameraId in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val flashAvailable =
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false

            if (flashAvailable) {
                return true
            }
        }
        return false
    }

    /**
     * 设备是否支持前后摄像头
     */
    fun isDeviceCameraSupported(context: Context): Pair<Boolean, Boolean> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        var hasFrontCamera = false
        var hasBackCamera = false
        for (cameraId in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                hasFrontCamera = true
            } else if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                hasBackCamera = true
            }
        }
        return Pair(hasBackCamera, hasFrontCamera)
    }

    /**
     * 旋转图片
     * 正数为顺时针旋转，否则逆时针旋转
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * 复制内容到剪切板
     */
    fun copyToClipboard(context: Context, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", text)
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            Log.e(TAG, "复制内容到剪切板失败: ${e.message}")
        }
    }

    /**
     * 获取下载目录
     */
    fun getExternalFilesDownloadDir(create: Boolean = true): File {
        val downloadDir = File(MyApplication.mContext.getExternalFilesDir(null), "downloads")
        if (!downloadDir.exists() && create) {
            downloadDir.mkdirs()
        }
        return downloadDir
    }

    /**
     * 判断当前应用是否是默认浏览器
     */
    fun isDefaultBrowser(context: Context): Boolean {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com".toUri()
        )
        val resolveInfo = context.packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    fun changeLightDarkStatus(window: Window, dark: Boolean) {
        // 调整状态栏字体颜色
        window.decorView.systemUiVisibility = if (dark) {
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        } else {
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }

    fun isNetImageUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    fun decodeBase64Image(base64Str: String, fileName: String): File? {
        return try {
            // 去掉 data:image/...;base64, 头
            val pureBase64 = if (base64Str.startsWith("data:")) {
                base64Str.substringAfter("base64,")
            } else {
                base64Str
            }
            val bytes = Base64.decode(pureBase64, Base64.DEFAULT)
            val destFile = File(getExternalFilesDownloadDir(), fileName)
            if (destFile.exists()) {
                destFile.delete()
            }
            destFile.outputStream().use { it.write(bytes) }
            destFile
        } catch (e: Exception) {
            Log.e(TAG, "保存 Base64 图片失败", e)
            null
        }
    }

    /**
     * 保存图片到相册
     */
    suspend fun saveBitmapToGallery(context: Context, imageFile: File, callback: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmapToGalleryUpQ(context, imageFile, callback)
        } else {
            saveBitmapToGalleryUnderQ(context, imageFile, callback)
        }
    }

    private suspend fun saveBitmapToGalleryUpQ(
        context: Context,
        imageFile: File,
        callback: (Boolean) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val mimeType = when (imageFile.extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/*"
            }
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, imageFile.name)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let { uri ->
                try {
                    FileInputStream(imageFile).use { input ->
                        resolver.openOutputStream(uri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0) // 标记为已完成
                    resolver.update(uri, contentValues, null, null)
                    withContext(Dispatchers.Main) {
                        callback.invoke(true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "保存图片失败: ${e.message}")
                    resolver.delete(uri, null, null)
                    withContext(Dispatchers.Main) {
                        callback.invoke(false)
                    }
                }
            } ?: run {
                Log.e(TAG, "保存图片失败，无法获取 URI")
                withContext(Dispatchers.Main) {
                    callback.invoke(false)
                }
            }
        }
    }

    private suspend fun saveBitmapToGalleryUnderQ(
        context: Context,
        imageFile: File,
        callback: (Boolean) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "browser")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, imageFile.name)
            try {
                FileInputStream(imageFile).use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                // 通知媒体库更新
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/png"), null)
                withContext(Dispatchers.Main) {
                    callback.invoke(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "保存图片失败: ${e.message}")
                withContext(Dispatchers.Main) {
                    callback.invoke(false)
                }
            }
        }
    }

    /**
     * 添加桌面快捷方式
     */
    fun addHomeScreen(context: Context, name: String, url: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val intent = Intent(context, MainActivity::class.java).apply {
            this.action = Intent.ACTION_VIEW
            this.data = url.toUri()
        }

        val shortCut = ShortcutInfo.Builder(context, "url_${System.currentTimeMillis()}")
            .setShortLabel(name.ifEmpty { url })
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher_round))
            .setIntent(intent)
            .build()
        if (shortcutManager.isRequestPinShortcutSupported) {
            val pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortCut)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent,
                PendingIntent.FLAG_IMMUTABLE)
            shortcutManager.requestPinShortcut(shortCut, pendingIntent.intentSender)
        } else {
            showToast(context, context.getString(R.string.toast_failed))
        }
    }
}