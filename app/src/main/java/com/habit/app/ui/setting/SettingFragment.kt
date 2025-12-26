package com.habit.app.ui.setting

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import com.habit.app.R
import com.habit.app.data.ENGINE_GOOGLE
import com.habit.app.data.TAG
import com.habit.app.databinding.FragmentSettingBinding
import com.habit.app.helper.DayNightUtil
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_DAY
import com.habit.app.helper.DayNightUtil.NIGHT_MODE_FOLLOW_SYSTEM
import com.habit.app.helper.FeedbackUtils
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.BrowseActivity
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.custom.SettingItem
import com.habit.app.ui.dialog.SearchEngineDialog
import com.habit.app.ui.dialog.ThemeSelectDialog
import com.habit.app.ui.home.BookmarkHistoryActivity
import com.habit.app.viewmodel.MainActivityModel
import com.wyz.emlibrary.em.Direction
import com.wyz.emlibrary.em.EMManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.getValue

class SettingFragment() : BaseFragment<FragmentSettingBinding>() {

    private val mScope = MainScope()
    private val loadingObserver = MutableLiveData(false)
    private val viewModel: MainActivityModel by activityViewModels()

    private var searchEngineDialog: SearchEngineDialog? = null
    private var themeSelectDialog: ThemeSelectDialog? = null

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        loadingObserver.observe(requireActivity()) { value ->
            binding.loadingView.visibility = if (value) View.VISIBLE else View.GONE
        }
    }

    private fun initData() {
        val curEngine = KeyValueManager.getValueByKey(KeyValueManager.KEY_ENGINE_SELECT) ?: ENGINE_GOOGLE
        binding.itemSearchEnging.updateDesc(curEngine)
    }

    private fun initListener() {
        binding.loadingView.setOnClickListener {}

        binding.itemSearchEnging.setOnClickListener {
            Log.d(TAG, "initListener: click search engine")
            searchEngineDialog = SearchEngineDialog.tryShowDialog(requireActivity())?.apply {
                this.mCallback = { engine ->
                    this@SettingFragment.binding.itemSearchEnging.updateDesc(engine)
                }
                setOnDismissListener {
                    searchEngineDialog = null
                }
            }
        }
        binding.itemDefaultBrowser.setOnClickListener {

        }
        binding.itemTheme.setOnClickListener {
            processThemeSelect()
        }
        binding.itemAddComponents.setOnClickListener {

        }

        binding.itemDownload.setOnClickListener {

        }
        binding.itemBookmark.setOnClickListener {
            BookmarkHistoryActivity.startActivity(requireContext(), viewModel.privacyObserver.value!!, true)
        }
        binding.itemHistory.setOnClickListener {
            BookmarkHistoryActivity.startActivity(requireContext(), viewModel.privacyObserver.value!!, false)
        }
        binding.itemDeleteData.setOnClickListener {

        }

        binding.itemFeedback.setOnClickListener {
            FeedbackUtils.feedback(requireContext())
        }
        binding.itemPrivacy.setOnClickListener {
            BrowseActivity.startPrivacyPolicy(requireContext())
        }
        binding.itemTerms.setOnClickListener {
            BrowseActivity.startTermOfService(requireContext())
        }
    }

    private fun processThemeSelect() {
        themeSelectDialog = ThemeSelectDialog.tryShowDialog(requireActivity())?.apply {
            this.mCallback = { mode ->
                Log.d(TAG, "用户选择日夜间模式: $mode")
                var realMode = mode
                if (realMode == NIGHT_MODE_FOLLOW_SYSTEM) {
                    realMode = DayNightUtil.getRealCurrentNightMode(context)
                }
                // 执行切换
                if (realMode == NIGHT_MODE_DAY) {
                    if (ThemeManager.getCurTheme() != ThemeManager.THEME_DEFAULT) {
                        Log.d(TAG, "实际切换到日间模式")
                        ThemeManager.switchTheme(ThemeManager.THEME_DEFAULT)
                    }
                } else {
                    if (ThemeManager.getCurTheme() != ThemeManager.THEME_NIGHT) {
                        Log.d(TAG, "实际切换到夜间模式")
                        ThemeManager.switchTheme(ThemeManager.THEME_NIGHT)
                    }
                }
            }
            setOnDismissListener {
                themeSelectDialog = null
            }
        }
    }

    private fun updateUIConfig() {
        EMManager.from(binding.root)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.topView)
            .setGradientRealColor(
                intArrayOf(
                    ThemeManager.getSkinColor(R.color.home_top_bg_start),
                    ThemeManager.getSkinColor(R.color.home_top_bg_end)
                ),
                Direction.TOP
            )
        binding.tvBasic.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.tvBrowser.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.tvApplication.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_40))
        binding.containerOptions.forEach { child ->
            if (child is SettingItem) {
                child.updateThemeUi()
            }
        }
        searchEngineDialog?.updateThemeUI()
        themeSelectDialog?.updateThemeUI()
    }

    override fun onThemeChanged(theme: String) {
        updateUIConfig()
    }

    override fun onDestroy() {
        mScope.cancel()
        super.onDestroy()
    }
}