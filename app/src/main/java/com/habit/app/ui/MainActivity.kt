package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
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
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainActivityModel by viewModels()
    private lateinit var mController: MainController

    private val homeFragmentTag = "HomeFragment"
    private val newsFragmentTag = "NewsFragment"
    private val settingFragmentTag = "SettingFragment"
    private var currentFragmentTag: String = homeFragmentTag
    private var lastFragmentTag: String? = null

    /**
     * homepage回调
     */
    private val homePageCallback = object : HomeFragment.HomeFragmentCallback {
        override fun onSearch(searchStr: String) {
            Log.d(TAG, "search input: $searchStr")
            viewModel.setSearchObserver(true)
            mController.processWebSearch(searchStr)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerSearchNavi)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)
        mController = MainController(this, viewModel, binding)

        initView()
        initData()
        setupObserver()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        EMManager.from(binding.bottomShadow)
            .setShadow("#142D0E20", 2f, 0f, -1f)
    }

    private fun initData() {
        // 设置底部选中
        binding.tabHome.isChecked = true
        switchFragment(currentFragmentTag)

        // 获取webSign
        mController.getDBLastSnapAndNewTab()
        // 更新tab数量
        mController.updateTabsCount()
    }

    private fun setupObserver() {
        viewModel.loadObserver.observe(this) { value ->
            binding.loadingView.isVisible = value
        }

        viewModel.searchObserver.observe(this) { value ->
            binding.pageSearch.isVisible = value
            KeyValueManager.getBooleanValue(KeyValueManager.KEY_REOPEN_LAST_TAB, value)
        }

        viewModel.privacyObserver.observe(this) { value ->
            mController.onPrivacyChange(value)
        }

        viewModel.phoneModeObserver.observe(this) { value ->
            mController.onPhoneModeChange(value)
        }
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}
        binding.pageSearch.setOnClickListener {}
        binding.containerBottom.setOnClickListener {}
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
        binding.ivNaviTabAdd.setOnClickListener {
            mController.saveCurSnapAndCreateNewWebTab()
        }
        binding.ivNaviPageRefresh.setOnClickListener {
            mController.stopLoadingAndGoBack()
        }

        binding.btnBottomHome.setOnClickListener {
            viewModel.setSearchObserver(false)
        }
        binding.editInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                mController.mCurInputStr = s.toString().trim()
            }
        })
        binding.editInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                mController.processWebSearch(v.text.toString().trim())
                true
            } else {
                false
            }
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
                homeFragmentTag -> HomeFragment(homePageCallback)
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
        // main
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
        binding.tvBottomMainTabNum.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))

        // 搜索页相关
        binding.pageSearch.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.ivNaviTabAdd.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_tab_add))
        binding.ivNaviPageRefresh.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_page_refresh))
        binding.ivSearchIcon.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_search_icon))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_80))
        EMManager.from(binding.containerInput)
            .setCorner(18f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        EMManager.from(binding.containerBottom)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        binding.btnBottomBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_page_back))
        binding.btnBottomNext.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_page_next))
        binding.btnBottomHome.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_page_home))
        binding.btnBottomContainerNum.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_page_container_num))
        binding.tvBottomSearchTabNum.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.btnBottomMenu.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_page_menu))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    override fun onDestroy() {
        DBManager.close()
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}