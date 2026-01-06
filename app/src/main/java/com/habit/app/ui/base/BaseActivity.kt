package com.habit.app.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.habit.app.event.BaseEvent
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

open class BaseActivity : AppCompatActivity() {

    private val themeChangedListener: (String) -> Unit = { theme->
        onThemeChanged(theme)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        ThemeManager.addThemeChangeListener(themeChangedListener)
    }

    @Subscribe
    fun onBaseEvent(event: BaseEvent) {
    }

    open fun onThemeChanged(theme: String) {
        // 调整状态栏字体颜色
        UtilHelper.changeLightDarkStatus(window, ThemeManager.isNightTheme())
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        ThemeManager.removeThemeChangeListener(themeChangedListener)
        super.onDestroy()
    }
}