package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.databinding.ActivityBookmarkHistoryBinding
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.home.fragment.BookmarkFragment
import com.habit.app.ui.home.fragment.HistoryFragment
import com.habit.app.viewmodel.home.BHActivityModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BookmarkHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityBookmarkHistoryBinding
    private val bhActivityModel: BHActivityModel by viewModels()
    private val bookmarkFragmentTag = "BookmarkFragment"
    private val historyFragmentTag = "HistoryFragment"
    private var currentFragmentTag: String = bookmarkFragmentTag
    private var lastFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, ThemeManager.isNightTheme(), binding.containerNavi)

        initView()
        setUpObservers()
        initData()
        initListener()
    }

    private fun initView() {
        currentFragmentTag = if (intent.getBooleanExtra(KEY_INPUT_TAB_BOOKMARK, false)) bookmarkFragmentTag else historyFragmentTag
        bhActivityModel.setBookMarkInit(intent.getStringExtra(KEY_INPUT_CUR_URL))
        bhActivityModel.homeWebViewPrivacy = intent.getBooleanExtra(KEY_INPUT_HOME_PRIVACY, false)
        updateUiConfig()
    }

    private fun initData() {
        binding.tabBookmark.updateSelect(currentFragmentTag == bookmarkFragmentTag)
        binding.tabHistory.updateSelect(currentFragmentTag == historyFragmentTag)
        binding.ivNaviCreateFolder.isVisible = currentFragmentTag == bookmarkFragmentTag
        binding.ivNaviClearHistory.isVisible = currentFragmentTag == historyFragmentTag

        lifecycleScope.launch(Dispatchers.IO) {
            delay(50)
            withContext(Dispatchers.Main) {
                switchFragment(currentFragmentTag)
            }
        }
    }

    private fun initListener() {
        binding.tabBookmark.setOnClickListener {
            binding.tabBookmark.updateSelect(true)
            binding.tabHistory.updateSelect(false)
            binding.ivNaviCreateFolder.isVisible = true
            binding.ivNaviClearHistory.isVisible = false

            switchFragment(bookmarkFragmentTag)
            updateUiConfig()
        }
        binding.tabHistory.setOnClickListener {
            binding.tabHistory.updateSelect(true)
            binding.tabBookmark.updateSelect(false)
            binding.ivNaviCreateFolder.isVisible = false
            binding.ivNaviClearHistory.isVisible = true

            switchFragment(historyFragmentTag)
            updateUiConfig()
        }

        binding.ivNaviBack.setOnClickListener {
            processBackEvent()
        }
        binding.ivNaviCreateFolder.setOnClickListener {
            (supportFragmentManager.findFragmentByTag(bookmarkFragmentTag) as? BookmarkFragment)?.processAddFolder()
        }
        binding.ivNaviClearHistory.setOnClickListener {
            (supportFragmentManager.findFragmentByTag(historyFragmentTag) as? HistoryFragment)?.processClearHistory()
        }

        binding.ivNaviClose.setOnClickListener {
            processCloseEvent()
        }
        binding.ivNaviAll.setOnClickListener {
            processAllEvent()
        }
    }

    private fun setUpObservers() {
        bhActivityModel.editObserver.observe(this) { value ->
            binding.containerNaviNormal.isVisible = !value
            binding.containerNaviClose.isVisible = value
        }
        bhActivityModel.bookmarkSelectAllObserver.observe(this) { value ->
            binding.ivNaviAll.setImageResource(
                ThemeManager.getSkinImageResId(if (value) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
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
                bookmarkFragmentTag -> BookmarkFragment()
                historyFragmentTag -> HistoryFragment()
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

    private fun processBackEvent() {
        if (currentFragmentTag == historyFragmentTag) {
            finish()
        } else {
            checkPageFinish()
        }
    }

    private fun processCloseEvent() {
        (supportFragmentManager.findFragmentByTag(bookmarkFragmentTag) as? BookmarkFragment)?.selectAllOrNot(true)
    }

    private fun processAllEvent() {
        if (currentFragmentTag == bookmarkFragmentTag) {
            (supportFragmentManager.findFragmentByTag(bookmarkFragmentTag) as? BookmarkFragment)?.selectAllOrNot(false)
        }
    }

    private fun updateUiConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.containerTopTab)
            .setCorner(22f)
            .setBackGroundRealColor(ThemeManager.getSkinColor( R.color.tag_top_bg_color1))
        binding.ivNaviBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_back))
        binding.ivNaviCreateFolder.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_navi_create_folder))
        binding.ivNaviClearHistory.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_navi_clean_history))
        binding.tabHistory.updateSelect(currentFragmentTag == historyFragmentTag)
        binding.tabBookmark.updateSelect(currentFragmentTag == bookmarkFragmentTag)
        binding.ivNaviClose.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_close))
        binding.ivNaviAll.setImageResource(
            ThemeManager.getSkinImageResId(if (bhActivityModel.bookmarkSelectAllObserver.value!!) R.drawable.iv_checkbox_select else R.drawable.iv_checkbox_unselect))
        binding.cardView.setCardBackgroundColor(ThemeManager.getSkinColor(R.color.view_bg_color))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUiConfig()
    }

    private fun checkPageFinish() {
        (supportFragmentManager.findFragmentByTag(bookmarkFragmentTag) as? BookmarkFragment)?.checkPageFinish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && currentFragmentTag == bookmarkFragmentTag) {
            checkPageFinish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        /**
         * 进入时，选中bookmark或history
         */
        const val KEY_INPUT_TAB_BOOKMARK = "key_input_tab_bookmark"

        /**
         * 进入此页面代入的url
         */
        const val KEY_INPUT_CUR_URL = "key_input_cur_url"

        const val KEY_INPUT_HOME_PRIVACY = "key_input_home_privacy"

        fun startActivity(context: Context, isPrivacy: Boolean, isBookmark: Boolean, curUrl: String? = null) {
            context.startActivity(Intent(context, BookmarkHistoryActivity::class.java).apply {
                putExtra(KEY_INPUT_TAB_BOOKMARK, isBookmark)
                putExtra(KEY_INPUT_CUR_URL, curUrl)
                putExtra(KEY_INPUT_HOME_PRIVACY, isPrivacy)
            })
        }
    }
}