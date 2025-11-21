package com.habit.app.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.habit.app.R
import com.habit.app.model.AccessSingleData
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
            AccessSingleData("iv_access_single_file", context.getString(R.string.text_file), "").apply {
                sortIndex = 0
                isSpecial = true },
            AccessSingleData("iv_access_single_game", context.getString(R.string.text_game), "").apply {
                sortIndex = 1
                isSpecial = true },
            AccessSingleData("iv_access_single_bookmark", context.getString(R.string.text_bookmark), "").apply {
                sortIndex = 2
                isSpecial = true },
            AccessSingleData("iv_access_single_instagram", context.getString(R.string.text_instagram)).apply {
                sortIndex = 3 },
            AccessSingleData("iv_access_single_tiktok", context.getString(R.string.text_tiktok)).apply {
                sortIndex = 4 },
            AccessSingleData("iv_access_single_youtube", context.getString(R.string.text_youtube)).apply {
                sortIndex = 5 },
            AccessSingleData("iv_access_single_twitter", context.getString(R.string.text_twitter)).apply {
                sortIndex = 6 },
            AccessSingleData("iv_access_single_facebook", context.getString(R.string.text_facebook)).apply {
                sortIndex = 7 },
            AccessSingleData("iv_access_single_amazon", context.getString(R.string.text_amazon)).apply {
                sortIndex = 8 },
            AccessSingleData("iv_access_single_add", context.getString(R.string.text_add), "").apply {
                sortIndex = 9
                isSpecial = true }
        )
    }
}