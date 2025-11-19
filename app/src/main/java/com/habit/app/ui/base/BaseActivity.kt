package com.habit.app.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.habit.app.event.BaseEvent
import com.habit.app.skin.ThemeManager
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
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        ThemeManager.removeThemeChangeListener(themeChangedListener)
        super.onDestroy()
    }
}