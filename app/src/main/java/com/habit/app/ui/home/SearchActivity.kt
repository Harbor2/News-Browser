package com.habit.app.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habit.app.R
import com.habit.app.data.ENGINE_BAIDU
import com.habit.app.data.ENGINE_BING
import com.habit.app.data.ENGINE_DUCKDUCK
import com.habit.app.data.ENGINE_GOOGLE
import com.habit.app.data.ENGINE_YAHOO
import com.habit.app.data.ENGINE_YANDEX
import com.habit.app.data.TAG
import com.habit.app.databinding.ActivitySearchBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.dialog.SearchEngineDialog
import com.habit.app.viewmodel.home.SearchActivityModel
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.SoftKeyboardHelper
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.launch
import kotlin.getValue

class SearchActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchActivityModel by viewModels()
    private lateinit var softKeyboardHelper: SoftKeyboardHelper

    private val keyboardListener = object : SoftKeyboardHelper.OnSoftKeyBoardChangeListener {
        override fun keyBoardHide(height: Int) {
            adjustBottomTool(0)
        }

        override fun keyBoardShow(height: Int) {
            adjustBottomTool(height)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)
        softKeyboardHelper = SoftKeyboardHelper()

        initView()
        initData()
        setupObserver()
        initListener()
    }


    private fun initView() {
        updateUIConfig()
        if (intent.getBooleanExtra("hasFocus", false)) {
            EMUtil.showSoftKeyboard(binding.editInput, this)
        }
    }

    private fun initData() {
        viewModel.loadHistory()
    }

    private fun initListener() {
        softKeyboardHelper.addKeyboardListener(this, keyboardListener)

        binding.tvSearchCancel.setOnClickListener {
            if (viewModel.cancelObserver.value!!) {
                finish()
                return@setOnClickListener
            }
            processSearch()
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
                viewModel.setCancelObserver(s.isEmpty())
            }
        })
        binding.editInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                processSearch()
                true
            } else {
                false
            }
        }
    }


    private fun processSearch() {
        val inputStr = binding.editInput.text.toString().trim()
        if (inputStr.isEmpty()) return
        intent.putExtra("searchStr", inputStr)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setupObserver() {
        lifecycleScope.launch {
            viewModel.searchHistory.collect { historyList ->
                Log.d(TAG, "searchHistory: $historyList")
            }
        }
        viewModel.cancelObserver.observe(this) { value ->
            binding.tvSearchCancel.text = getString(if (value) R.string.text_cancel else R.string.text_search)
            binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (value) R.color.text_main_color_30 else R.color.btn_color))
            binding.containerTopicHistory.isVisible = value
            binding.containerThink.isVisible = !value
        }
    }

    private fun adjustBottomTool(height: Int) {
        (binding.bottomTool.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            bottomMargin = height
            binding.bottomTool.layoutParams = this
        }
    }
    private fun showEngineSelectDialog() {
        SearchEngineDialog.Companion.tryShowDialog(this)
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
        binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (viewModel.cancelObserver.value!!) R.color.text_main_color_30 else R.color.btn_color))
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
        updateUIConfig()
    }
}