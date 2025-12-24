package com.habit.app.helper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.reflect.TypeToken
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.data.model.AccessSingleData
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import kotlin.random.Random

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
            AccessSingleData("iv_access_single_file", context.getString(R.string.text_file), "file").apply {
                sortIndex = 0
                isSpecial = true },
            AccessSingleData("iv_access_single_game", context.getString(R.string.text_game), "game").apply {
                sortIndex = 1
                isSpecial = true },
            AccessSingleData("iv_access_single_bookmark", context.getString(R.string.text_bookmark), "bookmark").apply {
                sortIndex = 2
                isSpecial = true },
            AccessSingleData("iv_access_single_instagram", context.getString(R.string.text_instagram), "instagram").apply {
                sortIndex = 3 },
            AccessSingleData("iv_access_single_tiktok", context.getString(R.string.text_tiktok), "tiktok").apply {
                sortIndex = 4 },
            AccessSingleData("iv_access_single_youtube", context.getString(R.string.text_youtube), "youtube").apply {
                sortIndex = 5 },
            AccessSingleData("iv_access_single_twitter", context.getString(R.string.text_twitter), "twitter").apply {
                sortIndex = 6 },
            AccessSingleData("iv_access_single_facebook", context.getString(R.string.text_facebook), "facebook").apply {
                sortIndex = 7 },
            AccessSingleData("iv_access_single_amazon", context.getString(R.string.text_amazon), "amazon").apply {
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
            AccessSingleData("iv_access_single_instagram", context.getString(R.string.text_instagram), "instagram"),
            AccessSingleData("iv_access_single_facebook", context.getString(R.string.text_facebook), "facebook"),
            AccessSingleData("iv_access_single_reddit", context.getString(R.string.text_reddit), "reddit"),
            AccessSingleData("iv_access_single_pinterest", context.getString(R.string.text_pinterest), "pinterest"),
            AccessSingleData("iv_access_single_whatsapp", context.getString(R.string.text_whatsapp), "whatsapp"),
            AccessSingleData("iv_access_single_twitter", context.getString(R.string.text_twitter), "twitter"),
            AccessSingleData("iv_access_single_snapchat", context.getString(R.string.text_snapchat), "snapchat"),
            AccessSingleData("iv_access_single_netflix", context.getString(R.string.text_netflix), "netflix"),
            AccessSingleData("iv_access_single_youtube", context.getString(R.string.text_youtube), "youtube"),
            AccessSingleData("iv_access_single_vimeo", context.getString(R.string.text_vimeo), "vimeo"),
            AccessSingleData("iv_access_single_dailymotion", context.getString(R.string.text_dailymotion), "dailymotion"),
            AccessSingleData("iv_access_single_cnn", context.getString(R.string.text_cnn), "cnn"),
            AccessSingleData("iv_access_single_foxnews", context.getString(R.string.text_fox_news), "foxnews"),
            AccessSingleData("iv_access_single_nytimes", context.getString(R.string.text_nytimes), "nytimes"),
            AccessSingleData("iv_access_single_nbcnews", context.getString(R.string.text_nbc_news), "nbcnews"),
            AccessSingleData("iv_access_single_cbc", context.getString(R.string.text_cbc), "cbc"),
            AccessSingleData("iv_access_single_espn", context.getString(R.string.text_espn), "espn"),
            AccessSingleData("iv_access_single_marca", context.getString(R.string.text_marca), "marca"),
            AccessSingleData("iv_access_single_chatgpt", context.getString(R.string.text_chatgpt), "chatgpt"),
            AccessSingleData("iv_access_single_linkedin", context.getString(R.string.text_linkedin), "linkedin"),
            AccessSingleData("iv_access_single_wikipedia", context.getString(R.string.text_wikipedia), "wikipedia"),
            AccessSingleData("iv_access_single_tiktok", context.getString(R.string.text_tiktok), "tiktok"),
            AccessSingleData("iv_access_single_twitch", context.getString(R.string.text_twitch), "twitch"),
            AccessSingleData("iv_access_single_tumblr", context.getString(R.string.text_tumblr), "tumblr"),
            AccessSingleData("iv_access_single_hulu", context.getString(R.string.text_hulu), "hulu"),
            AccessSingleData("iv_access_single_vevo", context.getString(R.string.text_vevo), "vevo"),
            AccessSingleData("iv_access_single_spotify", context.getString(R.string.text_spotify), "spotify"),
            AccessSingleData("iv_access_single_amazon", context.getString(R.string.text_amazon), "amazon"),
            AccessSingleData("iv_access_single_ebay", context.getString(R.string.text_ebay), "ebay"),
            AccessSingleData("iv_access_single_shopee", context.getString(R.string.text_shopee), "shopee"),
            AccessSingleData("iv_access_single_lazada", context.getString(R.string.text_lazada), "lazada"),
            AccessSingleData("iv_access_single_fantasy", context.getString(R.string.text_fantasy), "fantasy"),
            AccessSingleData("iv_access_single_yahoosports", context.getString(R.string.text_yahoo_sports), "yahoosports"),
            AccessSingleData("iv_access_single_wwe", context.getString(R.string.text_wwe), "wwe"),
            AccessSingleData("iv_access_single_cricbuzz", context.getString(R.string.text_cricbuzz), "cricbuzz"),
            AccessSingleData("iv_access_single_flashscore", context.getString(R.string.text_flashscore), "flashscore"),
            AccessSingleData("iv_access_single_goal", context.getString(R.string.text_goal), "goal"),
            AccessSingleData("iv_access_single_livescore", context.getString(R.string.text_livescore), "livescore"),
            AccessSingleData("iv_access_single_airbnb", context.getString(R.string.text_airbnb), "airbnb"),
            AccessSingleData("iv_access_single_booking", context.getString(R.string.text_booking), "booking"),
            AccessSingleData("iv_access_single_tripadvisor", context.getString(R.string.text_tripadvisor), "tripadvisor"),
            AccessSingleData("iv_access_single_drugs", context.getString(R.string.text_drugs), "drugs"),
            AccessSingleData("iv_access_single_tnation", context.getString(R.string.text_tnation), "tnation"),
            AccessSingleData("iv_access_single_who", context.getString(R.string.text_who), "who"),
            AccessSingleData("iv_access_single_healthline", context.getString(R.string.text_healthline), "healthline"),
            AccessSingleData("iv_access_single_bodybuilding", context.getString(R.string.text_bodybuilding), "bodybuilding"),
            AccessSingleData("iv_access_single_kidshealth", context.getString(R.string.text_kids_health), "kidshealth"),
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
    fun writeBitmapToCache(context: Context, bitmap: Bitmap): String? {
        return try {
            val parentFile = File(context.cacheDir, "webPic")
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

        selectedAccess.add(3, accessData)

        // 重新设置sortIndex
        selectedAccess.forEachIndexed { index, accessSingleData ->
            accessSingleData.sortIndex = index
        }
        KeyValueManager.saveValueWithKey(KeyValueManager.KEY_HOME_ACCESS_INFO, GsonUtil.gson.toJson(selectedAccess))
        return true
    }
}