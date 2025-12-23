package com.habit.app.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.databinding.ActivityBookmarkHistoryBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.home.fragment.BookmarkFragment
import com.habit.app.ui.home.fragment.HistoryFragment
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BookmarkHistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityBookmarkHistoryBinding
    private val bookmarkFragmentTag = "BookmarkFragment"
    private val historyFragmentTag = "HistoryFragment"
    private var currentFragmentTag: String = bookmarkFragmentTag
    private var lastFragmentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

        initView()
        setUpObservers()
        initData()
        initListener()
    }

    private fun initView() {
        currentFragmentTag = if (intent.getBooleanExtra(KEY_INPUT_TAB_BOOKMARK, false)) bookmarkFragmentTag else historyFragmentTag
        updateUiConfig()
    }

    private fun initData() {
        binding.tabBookmark.updateSelect(currentFragmentTag == bookmarkFragmentTag)
        binding.tabHistory.updateSelect(currentFragmentTag == historyFragmentTag)

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

            switchFragment(bookmarkFragmentTag)
            updateUiConfig()
        }
        binding.tabHistory.setOnClickListener {
            binding.tabHistory.updateSelect(true)
            binding.tabBookmark.updateSelect(false)

            switchFragment(historyFragmentTag)
            updateUiConfig()
        }
    }

    private fun setUpObservers() {

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

    private fun updateUiConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.containerTopTab)
            .setCorner(22f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.tag_top_bg_color))
        binding.ivNaviBack.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_navi_back))
        binding.ivNaviCreateFolder.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_navi_create_folder))
        binding.ivNaviClearHistory.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_bh_navi_clean_history))
        binding.tabHistory.updateSelect(currentFragmentTag == historyFragmentTag)
        binding.tabBookmark.updateSelect(currentFragmentTag == bookmarkFragmentTag)
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUiConfig()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        /**
         * 进入时，选中bookmark或history
         */
        const val KEY_INPUT_TAB_BOOKMARK = "key_input_tab_bookmark"

        fun startActivity(context: Context, isBookmark: Boolean) {
            context.startActivity(Intent(context, BookmarkHistoryActivity::class.java).apply {
                putExtra(KEY_INPUT_TAB_BOOKMARK, isBookmark)
            })
        }
    }
}