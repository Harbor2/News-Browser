package com.habit.app.ui.tag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.data.TAG
import com.habit.app.data.WEBVIEW_DEFAULT_NAME
import com.habit.app.data.db.DBManager
import com.habit.app.data.model.WebViewData
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.event.HomeTabsCountUpdateEvent
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.base.BaseFragment
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
        immersiveWindow(binding.root, false, binding.containerNavi)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

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
                val historyList = DBManager.getDao().getWebSnapsFromTable()
                if (historyList.contains(it)) {
                    historyList[historyList.indexOf(it)] = it
                    // 更新数据库
                    DBManager.getDao().updateWebSnapItem(it)
                }
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

            switchFragment(publicFragmentTag)
            updateUiConfig()
        }
        binding.tabPrivacy.setOnClickListener {
            binding.tabPrivacy.updateSelect(true)
            binding.tabPublic.updateSelect(false)

            switchFragment(privacyFragmentTag)
            updateUiConfig()
        }

        binding.ivPre.setOnClickListener {
            finish()
        }
        binding.ivAdd.setOnClickListener {
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
        Log.d(TAG, "数据库添加web sign：$newTabSign")
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
            EMManager.from(binding.containerTopTab)
                .setCorner(22f)
                .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.tag_top_bg_color))
            binding.ivPre.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_tag_tab_pre))
            binding.ivAdd.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_tag_tab_add))
            binding.ivClean.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_tag_tab_clean))
        } else {
            EMManager.from(binding.root)
                .setBackGroundColor(R.color.t_night_page_main_color)
            EMManager.from(binding.containerTopTab)
                .setCorner(22f)
                .setBackGroundColor(R.color.t_night_tag_top_bg_color)
            binding.ivPre.setImageResource(R.drawable.t_night_iv_tag_tab_pre)
            binding.ivAdd.setImageResource(R.drawable.t_night_iv_tag_tab_add)
            binding.ivClean.setImageResource(R.drawable.t_night_iv_tag_tab_clean)
        }
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUiConfig()
    }

    override fun finish() {
        super.finish()
        val addSnaps = DBManager.getDao().getWebSnapsFromTable().filter { it.isPrivacyMode == (currentFragmentTag == privacyFragmentTag) }
        if (addSnaps.isEmpty()) {
            Log.d(TAG, "tag页面关闭前为空，新创建一个tab")
            val newTabSign = newTabAndInsertDB()
            setResult(RESULT_OK, Intent().putExtra(KEY_TRANS_WEB_SIGN, newTabSign))
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