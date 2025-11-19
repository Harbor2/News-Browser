package com.habit.app.skin

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.habit.app.MyApplication

object ThemeManager {

    private const val SP_NAME = "theme_prefs"
    private const val KEY_THEME = "theme"

    /** 主题常量 */
    const val THEME_DEFAULT = "t_default"
    const val THEME_NIGHT = "t_night"

    /** 当前主题 */
    private var curTheme = loadThemeFromSp()

    /** 监听器 */
    private val listeners = mutableSetOf<(String) -> Unit>()

    /** 缓存（仅缓存无状态资源） */
    private val colorCache = mutableMapOf<Pair<Int,String>, Int>()
    private val colorStateListCache = mutableMapOf<Pair<Int,String>, ColorStateList>()
    private val drawableCache = mutableMapOf<Pair<Int,String>, Drawable>()


    // =================== 主题切换 ===================

    fun getCurTheme() = curTheme

    fun switchTheme(newTheme: String) {
        if (newTheme == curTheme) return
        curTheme = newTheme
        saveThemeToSp(newTheme)

        // 主题改变后清理缓存
        clearCache()

        notifyThemeChanged()
    }

    private fun clearCache() {
        colorCache.clear()
        colorStateListCache.clear()
        drawableCache.clear()
    }


    // =================== 颜色 Color ===================

    fun getSkinColor(res: Int, theme: String = curTheme): Int {
        val key = res to theme
        colorCache[key]?.let { return it }

        val ctx = MyApplication.mContext
        val name = ctx.resources.getResourceEntryName(res)
        val themeResId = ctx.resources.getIdentifier("${theme}_$name", "color", ctx.packageName)

        val color = ContextCompat.getColor(ctx, if (themeResId != 0) themeResId else res)
        colorCache[key] = color
        return color
    }


    // =================== ColorStateList ===================

    fun getSkinColorStateList(res: Int, theme: String = curTheme): ColorStateList {
        val key = res to theme
        colorStateListCache[key]?.let { return it }

        val ctx = MyApplication.mContext
        val name = ctx.resources.getResourceEntryName(res)
        val themeResId = ctx.resources.getIdentifier("${theme}_$name", "drawable", ctx.packageName)

        val csl = AppCompatResources.getColorStateList(ctx, if (themeResId != 0) themeResId else res)
        colorStateListCache[key] = csl
        return csl
    }


    // =================== Drawable（自动判断是否缓存） ===================

    fun getSkinDrawable(res: Int, theme: String = curTheme): Drawable? {
        val ctx = MyApplication.mContext
        val name = ctx.resources.getResourceEntryName(res)
        val themeResId = ctx.resources.getIdentifier("${theme}_$name", "drawable", ctx.packageName)

        val realRes = if (themeResId != 0) themeResId else res
        val key = realRes to theme

        // 检查缓存
        drawableCache[key]?.let {
            // mutate 不需要重复调用
            return it
        }

        val drawable = AppCompatResources.getDrawable(ctx, realRes)?.mutate() ?: return null

        // 是否需要缓存？仅缓存 **无状态的 drawable**
        if (!isStatefulDrawable(drawable)) {
            drawableCache[key] = drawable
        }

        return drawable
    }


    // =================== Drawable 是否有状态 ===================

    private fun isStatefulDrawable(drawable: Drawable): Boolean {
        return drawable.isStateful ||
                drawable is StateListDrawable ||
                drawable is AnimationDrawable
    }


    // =================== Listener ===================

    fun addThemeChangeListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    fun removeThemeChangeListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyThemeChanged() {
        listeners.forEach { it.invoke(curTheme) }
    }

    // =================== SP ===================

    private fun saveThemeToSp(theme: String) {
        MyApplication.mContext
            .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme)
            .apply()
    }

    private fun loadThemeFromSp(): String {
        return MyApplication.mContext
            .getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_DEFAULT) ?: THEME_DEFAULT
    }
}