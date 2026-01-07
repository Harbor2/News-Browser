package com.habit.app.ui.tag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.data.MAX_SNAP_COUNT
import com.habit.app.data.TAG
import com.habit.app.data.WEBVIEW_DEFAULT_NAME
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.WebViewData
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.event.HomeTabsCountUpdateEvent
import com.habit.app.helper.ThemeManager
import com.habit.app.helper.UtilHelper
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.base.BaseFragment
import com.habit.app.viewmodel.tag.TagsViewModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.UUID
import kotlin.getValue


class TagsActivity : BaseActivity() {
    private lateinit var binding: ActivityTagsBinding
    private val publicFragmentTag = "PublicFragment"
    private val privacyFragmentTag = "PrivacyFragment"
    private var currentFragmentTag: String = publicFragmentTag
    private var lastFragmentTag: String? = null
    private val tagsModel: TagsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, ThemeManager.isNightTheme(), binding.containerNavi)

        initView()
        setUpObservers()
        initData()
        initListener()
    }

    private fun initView() {
        currentFragmentTag = if (intent.getBooleanExtra(KEY_INPUT_PRIVACY_MODE, false)) privacyFragmentTag else publicFragmentTag
        updateUiConfig()
    }

    private fun initData() {
        lifecycleScope.launch(Dispatchers.IO) {
            binding.tabPublic.updateSelect(currentFragmentTag == publicFragmentTag)
            binding.tabPrivacy.updateSelect(currentFragmentTag == privacyFragmentTag)

            mWebViewData?.let {
                DBManager.getDao().updateWebSnapItem(it)
                mWebViewData = null
            }

            delay(50)
            withContext(Dispatchers.Main) {
                switchFragment(currentFragmentTag)
            }
        }
    }

    private fun initListener() {
        binding.tabPublic.setOnClickListener {
            binding.tabPublic.updateSelect(true)
            binding.tabPrivacy.updateSelect(false)
            updateTopTabBg()
            UtilHelper.changeLightDarkStatus(window, ThemeManager.isNightTheme())

            switchFragment(publicFragmentTag)
            updateUiConfig()
        }
        binding.tabPrivacy.setOnClickListener {
            binding.tabPrivacy.updateSelect(true)
            binding.tabPublic.updateSelect(false)
            updateTopTabBg()
            UtilHelper.changeLightDarkStatus(window, true)

            switchFragment(privacyFragmentTag)
            updateUiConfig()
        }

        binding.ivPre.setOnClickListener {
            preCheckAndFinish()
        }
        binding.ivAdd.setOnClickListener {
            if (DBManager.getDao().getWebSnapsCount(currentFragmentTag == privacyFragmentTag) >= MAX_SNAP_COUNT) {
                UtilHelper.showToast(this, getString(R.string.toast_snap_max_count))
                return@setOnClickListener
            }

            val newTabSign = newTabAndInsertDB()
            setResult(RESULT_OK, Intent().putExtra(KEY_TRANS_WEB_SIGN, newTabSign))
            finish()
        }
        binding.ivClean.setOnClickListener {
            deleteSnapData()
        }
    }

    private fun setUpObservers() {
        lifecycleScope.launch {
            tagsModel.publicTagCountObserver.collect { count ->
                binding.tabPublic.updateCount(count)
            }
        }
        lifecycleScope.launch {
            tagsModel.privacyTagCountObserver.collect { count ->
                binding.tabPrivacy.updateCount(count)
            }
        }
        lifecycleScope.launch {
            tagsModel.snapSelectObserver.collect { snapData ->
                if (snapData == null) return@collect
                setResult(RESULT_OK, Intent().apply {
                    putExtra(KEY_TRANS_WEB_SIGN, snapData.sign)
                })
                finish()
            }
        }
    }

    /**
     * 新建snap并插入数据库
     */
    private fun newTabAndInsertDB(): String {
        val newTabSign = UUID.randomUUID().toString()
        val newTabViewData = WebViewData(
            WEBVIEW_DEFAULT_NAME,
            newTabSign,
            "",
            true,
            currentFragmentTag == privacyFragmentTag,
            "",
            ""
        )
        DBManager.getDao().insertWebSnapToTable(newTabViewData)
        EventBus.getDefault().post(HomeTabsCountUpdateEvent())
        Log.d(TAG, "数据库添加webSnapData：$newTabViewData")
        return newTabSign
    }

    /**
     * 删除所有snap数据
     */
    private fun deleteSnapData() {
        supportFragmentManager.findFragmentByTag(currentFragmentTag)?.let { fragment ->
            when (currentFragmentTag) {
                publicFragmentTag -> {
                    if (fragment is PublicFragment) {
                        fragment.deleteSnapDataAndCheckEmpty()
                    }
                }
                privacyFragmentTag -> {
                    if (fragment is PrivacyFragment) {
                        fragment.deleteSnapDataAndCheckEmpty()
                    }
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
                publicFragmentTag -> PublicFragment()
                privacyFragmentTag -> PrivacyFragment()
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
        if (currentFragmentTag == publicFragmentTag) {
            EMManager.from(binding.root)
                .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
            updateTopTabBg()
            binding.ivPre.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_tag_tab_pre))
            binding.ivAdd.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_tag_tab_add))
            binding.ivClean.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_tag_tab_clean))
            UtilHelper.changeLightDarkStatus(window, ThemeManager.isNightTheme())
            binding.tabPublic.updateSelect(true)
            binding.tabPrivacy.updateSelect(false)
        } else {
            EMManager.from(binding.root)
                .setBackGroundColor(R.color.t_night_page_main_color)
            updateTopTabBg()
            binding.ivPre.setImageResource(R.drawable.t_night_iv_tag_tab_pre)
            binding.ivAdd.setImageResource(R.drawable.t_night_iv_tag_tab_add)
            binding.ivClean.setImageResource(R.drawable.t_night_iv_tag_tab_clean)
            UtilHelper.changeLightDarkStatus(window, true)
            binding.tabPublic.updateSelect(false)
            binding.tabPrivacy.updateSelect(true)
        }
    }

    private fun updateTopTabBg() {
        EMManager.from(binding.containerTopTab)
            .setCorner(22f)
            .setBackGroundRealColor(ThemeManager.getSkinColor(if (currentFragmentTag == publicFragmentTag) R.color.tag_top_bg_color1 else R.color.tag_top_bg_color2))
    }

    override fun onThemeChanged(theme: String) {
        updateUiConfig()
    }

    private fun preCheckAndFinish() {
        val addSnaps = DBManager.getDao().getWebSnapsFromTable().filter { it.isPrivacyMode == (currentFragmentTag == privacyFragmentTag) }
        if (addSnaps.isEmpty()) {
            Log.d(TAG, "tag页面关闭前为空，新创建一个tab")
            val newTabSign = newTabAndInsertDB()
            setResult(RESULT_OK, Intent().putExtra(KEY_TRANS_WEB_SIGN, newTabSign))
        }
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            preCheckAndFinish()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        var mWebViewData: WebViewData? = null

        /**
         * 退出页面时携带的webSign
         */
        const val KEY_TRANS_WEB_SIGN = "key_trans_web_sign"

        /**
         * 进入时，是否是隐私模式
         */
        const val KEY_INPUT_PRIVACY_MODE = "key_input_privacy_mode"

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TagsActivity::class.java))
        }
    }
}