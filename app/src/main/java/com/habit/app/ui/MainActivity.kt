package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.data.db.DBManager
import com.habit.app.databinding.ActivityMainBinding
import com.habit.app.event.HomeTabsCountUpdateEvent
import com.habit.app.helper.FeedbackUtils
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.dialog.BrowserMenuDialog
import com.habit.app.ui.home.fragment.HomeFragment
import com.habit.app.ui.news.NewsFragment
import com.habit.app.ui.setting.SettingFragment
import com.habit.app.ui.tag.TagsActivity
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.immersiveWindow
import org.greenrobot.eventbus.Subscribe

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
     * 原始输入内容
     * 用户重新搜索时用到
     */
    private var mOriginInputTextStr = ""

    /**
     * menu菜单dialog
     */
    private var mBrowserMenuDialog: BrowserMenuDialog? = null

    private val tagsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val transWebSign = result.data?.getStringExtra(TagsActivity.KEY_TRANS_WEB_SIGN) ?: ""
            if (transWebSign.isEmpty()) return@registerForActivityResult
            Log.d(TAG, "主页接收webView sign：$transWebSign")
            // 数据库读取
            val dbWebViewData = DBManager.getDao().getWebSnapsBySign(transWebSign)
            if (dbWebViewData == null) {
                return@registerForActivityResult
            }

            if (dbWebViewData.url.isEmpty()) {
                Log.d(TAG, "主页渲染home页面")
                mController.mCurWebSign = transWebSign
                mController.mCurWebView = null
                viewModel.setPrivacyObserver(dbWebViewData.isPrivacyMode ?: false)
                viewModel.setPhoneModeObserver(true)
                viewModel.setSearchObserver(false)
            } else {
                if (mController.mCurWebSign != transWebSign || (mController.mCurWebView == null)) {
                    mController.mCurWebSign = transWebSign
                    viewModel.setPrivacyObserver(dbWebViewData.isPrivacyMode ?: false)
                    Log.d(TAG, "主页渲染webView sign：${dbWebViewData.sign}")
                    viewModel.setSearchObserver(true)
                    mController.updateWebView(dbWebViewData)
                } else {
                    if (!viewModel.searchObserver.value!!) {
                        viewModel.setSearchObserver(true)
                    }
                }
            }
        }
    }

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

    private val menuCallback = object : BrowserMenuDialog.BrowserMenuCallback {
        override fun onPrivateChanged(enter: Boolean) {
            viewModel.setPrivacyObserver(enter)
        }
        override fun onBookMarksClick() {

        }
        override fun onDownloadClick() {

        }
        override fun onHistoryClick() {

        }


        override fun onDesktopChanged(isPhoneMode: Boolean) {
            if (isPhoneMode == viewModel.phoneModeObserver.value) return
            viewModel.setPhoneModeObserver(isPhoneMode)
        }
        override fun onBookmarkChanged(add: Boolean) {
            UtilHelper.showToast(this@MainActivity, getString(if (add) R.string.toast_bookmark_add else R.string.toast_bookmark_remove))
        }
        override fun onNavigationAddClick() {
            mController.processNavigationAdd()
        }

        override fun onFeedbackClick() {
            FeedbackUtils.feedback(this@MainActivity)
        }
        override fun onPageSearchClick() {
        }
        override fun onShareClick() {
            mController.mCurWebView?.let {
                UtilHelper.shareUrl(this@MainActivity, it.url ?: "")
            }
        }
        override fun onSettingClick() {
            mController.createWebViewSnapshot { webViewData ->
                if (webViewData != null) {
                    // 更新封面
                    DBManager.getDao().updateWebSnapItem(webViewData)
                    // 返回home页，但保留sign不变
                    viewModel.setSearchObserver(false)
                    // 跳转setting
                    binding.containerTabSetting.performClick()
                }
            }
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
    }

    private fun setupObserver() {
        viewModel.loadObserver.observe(this) { value ->
            binding.loadingView.isVisible = value
        }

        viewModel.searchObserver.observe(this) { value ->
            binding.pageSearch.isVisible = value
            KeyValueManager.saveBooleanValue(KeyValueManager.KEY_REOPEN_LAST_TAB, value)
        }

        viewModel.privacyObserver.observe(this) { value ->
            mController.onPrivacyModeChange(value)
        }

        viewModel.phoneModeObserver.observe(this) { value ->
            mController.onPhoneModeChange(value)
        }
    }

    private fun initListener() {
        binding.containerSearchNavi.setOnClickListener {
            EMUtil.hideSoftKeyboard(binding.editInput, this)
        }
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
            tagsLauncher.launch(Intent(this, TagsActivity::class.java).apply {
                putExtra(TagsActivity.KEY_INPUT_PRIVACY_MODE, viewModel.privacyObserver.value!!)
            })
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

        binding.btnBottomBack.setOnClickListener {
            mController.mCurWebView?.let {
                if (it.canGoBack()) {
                    it.goBack()
                }
            }
        }
        binding.btnBottomNext.setOnClickListener {
            mController.mCurWebView?.let {
                if (it.canGoForward()) {
                    it.goForward()
                }
            }
        }
        binding.btnBottomHome.setOnClickListener {
            viewModel.setSearchObserver(false)
        }
        binding.btnBottomContainerNum.setOnClickListener {
            mController.createWebViewSnapshot { webViewData ->
                if (webViewData != null) {
                    TagsActivity.mWebViewData = webViewData
                }
                tagsLauncher.launch(Intent(this, TagsActivity::class.java).apply {
                    putExtra(TagsActivity.KEY_INPUT_PRIVACY_MODE, viewModel.privacyObserver.value!!)
                })
            }
        }
        binding.btnBottomMenu.setOnClickListener {
            showMenuDialog()
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
                mOriginInputTextStr = ""
                true
            } else {
                false
            }
        }
        binding.editInput.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mOriginInputTextStr = binding.editInput.text.toString()
                binding.editInput.text?.clear()
            } else {
                if (mOriginInputTextStr.isNotEmpty()) {
                    binding.editInput.setText(mOriginInputTextStr)
                }
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

    private fun showMenuDialog() {
        mController.mCurWebView?.let { webView ->
            mBrowserMenuDialog = BrowserMenuDialog.tryShowDialog(this)?.apply {
                val webUrl = webView.url ?: ""
                val webName = webView.getTag(R.id.web_title) as? String ?: ""
                val iconBitmap = webView.getTag(R.id.web_small_icon) as? Bitmap
                val iconBitmapPath = if (iconBitmap == null) "" else UtilHelper.writeBitmapToCache(this@MainActivity, iconBitmap) ?: ""

                this.initData(webUrl, webName,  iconBitmapPath,viewModel.privacyObserver.value!!, viewModel.phoneModeObserver.value!!)
                this.mCallback = menuCallback

                setOnDismissListener {
                    mBrowserMenuDialog = null
                }
            }
        } ?: run {
            mBrowserMenuDialog = null
        }
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
        binding.ivInputTrace.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_search_trace))
        // Dialog
        mBrowserMenuDialog?.updateThemeUI()
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    @Subscribe
    fun updateTabsCountEvent(event: HomeTabsCountUpdateEvent) {
        mController.updateTabsCount()
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