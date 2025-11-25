package com.habit.app.ui.home.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.databinding.FragmentHistoryBinding
import com.habit.app.event.EngineChangedEvent
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.data.ENGINE_BAIDU
import com.habit.app.data.ENGINE_BING
import com.habit.app.data.ENGINE_DUCKDUCK
import com.habit.app.data.ENGINE_GOOGLE
import com.habit.app.data.ENGINE_YAHOO
import com.habit.app.data.ENGINE_YANDEX
import com.habit.app.data.TAG
import com.habit.app.viewmodel.home.SearchViewModel
import com.habit.app.ui.base.BaseFragment
import com.habit.app.ui.dialog.SearchEngineDialog
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.SoftKeyboardHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe

class HistoryFragment() : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: SearchViewModel by activityViewModels()
    var mCallback: HistoryFragmentCallback? = null
    private var inputAutoHasFocus: Boolean = false
    private lateinit var softKeyboardHelper: SoftKeyboardHelper
    private val keyboardListener = object : SoftKeyboardHelper.OnSoftKeyBoardChangeListener {
        override fun keyBoardHide(height: Int) {
            adjustBottomTool(0)
        }

        override fun keyBoardShow(height: Int) {
            adjustBottomTool(height)
        }
    }
    private val cancelObserver = MutableLiveData(true)

    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FragmentHistoryBinding {
        return FragmentHistoryBinding.inflate(inflater, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (binding.containerNavi.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            topMargin = EMUtil.getStatusBarHeight(requireContext())
        }
        softKeyboardHelper = SoftKeyboardHelper()

        initView()
        initData()
        setupObserver()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        if (inputAutoHasFocus) {
            EMUtil.showSoftKeyboard(binding.editInput, requireContext())
        }

        cancelObserver.observe(requireActivity()) { value ->
            binding.tvSearchCancel.text = getString(if (value) R.string.text_cancel else R.string.text_search)
            binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (value) R.color.text_main_color_30 else R.color.btn_color))
            binding.containerTopicHistory.isVisible = value
            binding.containerThink.isVisible = !value
        }
    }

    private fun initData() {
        viewModel.loadHistory()
    }

    private fun initListener() {
        softKeyboardHelper.addKeyboardListener(requireActivity(), keyboardListener)

        binding.tvSearchCancel.setOnClickListener {
            if (cancelObserver.value!!) {
                requireActivity().finish()
                return@setOnClickListener
            }
            mCallback?.onSearch()
        }
        binding.ivEngineIcon.setOnClickListener {
            showEngineSelectDialog()
        }
        binding.bottomTool.forEach { child ->
            child.setOnClickListener {
                val tagStr = child.tag as? String
                if (child is TextView && !tagStr.isNullOrEmpty()) {
                    Log.d(TAG, "点击tool文案：$tagStr")
                }
            }
        }
        binding.editInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.setEditInput(s.toString())
                cancelObserver.value = s.isEmpty()
            }
        })
    }

    private fun setupObserver() {
        lifecycleScope.launch {
            viewModel.searchHistory.collect { historyList ->
                Log.d(TAG, "searchHistory: $historyList")
            }
        }
    }

    fun setFocus(focus: Boolean) {
        this.inputAutoHasFocus = focus
    }

    private fun adjustBottomTool(height: Int) {
        if (!isFragmentSelect) return
        (binding.bottomTool.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            bottomMargin = height
            binding.bottomTool.layoutParams = this
        }
    }

    private fun showEngineSelectDialog() {
        SearchEngineDialog.Companion.tryShowDialog(requireActivity())
    }

    fun updateEngineIcon() {
        val iconRes = when (KeyValueManager.getValueByKey(KeyValueManager.KEY_ENGINE_SELECT) ?: ENGINE_GOOGLE) {
            ENGINE_GOOGLE -> R.drawable.iv_engine_icon_google
            ENGINE_BING -> R.drawable.iv_engine_icon_bing
            ENGINE_YAHOO -> R.drawable.iv_engine_icon_yahoo
            ENGINE_DUCKDUCK -> R.drawable.iv_engine_icon_duckduck
            ENGINE_YANDEX -> R.drawable.iv_engine_icon_yandex
            ENGINE_BAIDU -> R.drawable.iv_engine_icon_baidu
            else -> R.drawable.iv_engine_icon_google
        }
        binding.ivEngineIcon.setImageResource(iconRes)
    }

    private fun updateUIConfig() {
        binding.root.setBackgroundColor(ThemeManager.getSkinColor(R.color.page_main_color))
        EMManager.from(binding.root).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.page_main_color))
        binding.ivSearchSound.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_search_sound))
        binding.ivEngineIconArrow.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_engine_select_arrow))
        binding.editInput.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.editInput.setHintTextColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (cancelObserver.value!!) R.color.text_main_color_30 else R.color.btn_color))
        updateEngineIcon()
        EMManager.from(binding.containerArea)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        EMManager.from(binding.bottomTool).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.tvWwww).setCorner(4f).setBorderWidth(1f).setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
        EMManager.from(binding.tvCom).setCorner(4f).setBorderWidth(1f).setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
        EMManager.from(binding.tvCn).setCorner(4f).setBorderWidth(1f).setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
        EMManager.from(binding.tvPoint).setCorner(4f).setBorderWidth(1f).setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
        EMManager.from(binding.tvSlash).setCorner(4f).setBorderWidth(1f).setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)

    }

    @Subscribe
    fun onEngineChangedEvent(event: EngineChangedEvent) {
        updateEngineIcon()
    }

    override fun onDestroy() {
        softKeyboardHelper.removeKeyboardListener(requireActivity())
        super.onDestroy()
    }

    interface HistoryFragmentCallback {
        fun onSearch()
    }
}