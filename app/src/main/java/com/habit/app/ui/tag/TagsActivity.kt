package com.habit.app.ui.tag

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.habit.app.R
import com.habit.app.databinding.ActivityTagsBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.base.BaseFragment
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.immersiveWindow


class TagsActivity : BaseActivity() {
    private lateinit var binding: ActivityTagsBinding
    private val loadingObserver = MutableLiveData(false)
    private val publicFragmentTag = "PublicFragment"
    private val privacyFragmentTag = "PrivacyFragment"
    private var currentFragmentTag: String = publicFragmentTag
    private var lastFragmentTag: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTagsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)
        KeyValueManager.saveBooleanValue(KeyValueManager.KEY_ENTERED_HOME, true)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUiConfig()
        loadingObserver.observe(this) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }

        switchFragment(currentFragmentTag)
    }

    private fun initData() {
        binding.tabPublic.updateCount(1)
        binding.tabPrivacy.updateCount(1)
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

        }
        binding.ivClean.setOnClickListener {

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

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TagsActivity::class.java))
        }
    }
}