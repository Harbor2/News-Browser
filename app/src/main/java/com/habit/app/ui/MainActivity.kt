package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.base.BaseActivity
import com.habit.app.R
import com.habit.app.databinding.ActivityMainBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.home.fragment.HomeFragment
import com.habit.app.ui.news.NewsFragment
import com.habit.app.ui.setting.SettingFragment
import com.habit.app.ui.tag.TagsActivity
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mScope = MainScope()

    private val homeFragmentTag = "HomeFragment"
    private val newsFragmentTag = "NewsFragment"
    private val settingFragmentTag = "SettingFragment"
    private var currentFragmentTag: String = homeFragmentTag
    private var lastFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        Log.d(TAG, "initView")
        updateUIConfig()
        EMManager.from(binding.bottomShadow)
            .setShadow("#142D0E20", 2f, 0f, -1f)
    }

    private fun initData() {
        Log.d(TAG, "initData")
        // 设置底部选中
        binding.tabHome.isChecked = true
        switchFragment(currentFragmentTag)
    }

    private fun initListener() {
        Log.d(TAG, "initListener")
        binding.containerTabHome.setOnClickListener{
            binding.tabNews.isChecked = false
            binding.tabTag.isChecked = false
            binding.tabSetting.isChecked = false
            binding.tabHome.isChecked = true
            switchFragment(homeFragmentTag)
        }
        binding.containerTabNews.setOnClickListener{
            binding.tabHome.isChecked = false
            binding.tabTag.isChecked = false
            binding.tabSetting.isChecked = false
            binding.tabNews.isChecked = true
            switchFragment(newsFragmentTag)
        }
        binding.containerTabTag.setOnClickListener{
            TagsActivity.startActivity(this)
        }
        binding.containerTabSetting.setOnClickListener{
            binding.tabHome.isChecked = false
            binding.tabNews.isChecked = false
            binding.tabTag.isChecked = false
            binding.tabSetting.isChecked = true
            switchFragment(settingFragmentTag)
        }
    }

    private fun switchFragment(tag: String) {
        lastFragmentTag = currentFragmentTag
        currentFragmentTag = tag
        val fragmentManager = supportFragmentManager
        val currentFragment = fragmentManager.findFragmentByTag(tag)
        val transaction = fragmentManager.beginTransaction()
        if (currentFragment == null) {
            // Fragment 不存在，创建新的实例
            val newFragment = when (tag) {
                homeFragmentTag -> HomeFragment()
                newsFragmentTag -> NewsFragment()
                settingFragmentTag -> SettingFragment()
                else -> return
            }
            transaction.add(R.id.fragment_container, newFragment, tag)
            (newFragment as? BaseFragment<*>)?.updateFragmentSelect(true)
        } else {
            // Fragment 已存在，直接显示
            transaction.show(currentFragment)
            (currentFragment as? BaseFragment<*>)?.updateFragmentSelect(true)
        }
        // 隐藏其他 Fragment
        for (fragment in fragmentManager.fragments) {
            if (fragment.tag != tag) {
                transaction.hide(fragment)
                (fragment as? BaseFragment<*>)?.updateFragmentSelect(false)
            }
        }
        transaction.commit()
    }

    private fun updateUIConfig() {
        binding.tabsMainBottom.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))

        binding.tabHome.background = ThemeManager.getSkinDrawable(R.drawable.selector_tab_background)
        binding.tabHome.setTextColor(ThemeManager.getSkinColorStateList(R.drawable.selector_main_tab_text))
        val drawableTopHome = ThemeManager.getSkinDrawable(R.drawable.tab_drawable_home)
        binding.tabHome.setCompoundDrawablesWithIntrinsicBounds(null, drawableTopHome, null, null)

        binding.tabNews.background = ThemeManager.getSkinDrawable(R.drawable.selector_tab_background)
        binding.tabNews.setTextColor(ThemeManager.getSkinColorStateList(R.drawable.selector_main_tab_text))
        val drawableTopNews = ThemeManager.getSkinDrawable(R.drawable.tab_drawable_news)
        binding.tabNews.setCompoundDrawablesWithIntrinsicBounds(null, drawableTopNews, null, null)

        binding.tabTag.background = ThemeManager.getSkinDrawable(R.drawable.selector_tab_background)
        binding.tabTag.setTextColor(ThemeManager.getSkinColorStateList(R.drawable.selector_main_tab_text))
        val drawableTopTag = ThemeManager.getSkinDrawable(R.drawable.tab_drawable_tag)
        binding.tabTag.setCompoundDrawablesWithIntrinsicBounds(null, drawableTopTag, null, null)

        binding.tabSetting.background = ThemeManager.getSkinDrawable(R.drawable.selector_tab_background)
        binding.tabSetting.setTextColor(ThemeManager.getSkinColorStateList(R.drawable.selector_main_tab_text))
        val drawableTopSetting = ThemeManager.getSkinDrawable(R.drawable.tab_drawable_setting)
        binding.tabSetting.setCompoundDrawablesWithIntrinsicBounds(null, drawableTopSetting, null, null)
    }


    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        Log.d(TAG, "activity中 onThemeChanged")
        updateUIConfig()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        mScope.cancel()
        DBManager.close()
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}