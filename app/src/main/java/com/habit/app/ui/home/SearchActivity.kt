package com.habit.app.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
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
import com.habit.app.data.db.DBManager
import com.habit.app.data.repority.ThinkWordRepository
import com.habit.app.databinding.ActivitySearchBinding
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.custom.SearchThinkWordItem
import com.habit.app.ui.dialog.MicReceiveDialog
import com.habit.app.ui.dialog.SearchEngineDialog
import com.habit.app.viewmodel.home.SearchActivityModel
import com.habit.app.viewmodel.home.SearchActivityModelFactory
import com.wyz.emlibrary.custom.AutoWrapLayout
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.SoftKeyboardHelper
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.launch
import kotlin.getValue

class SearchActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var micDialog: MicReceiveDialog? = null

    private val viewModel: SearchActivityModel by viewModels {
        SearchActivityModelFactory(ThinkWordRepository())
    }

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
        val hasFocus = intent.getBooleanExtra("hasFocus", false)
        val hasMic = intent.getBooleanExtra("hasMic", false)
        // 麦克风优先级高
        if (hasMic) {
            showMicDialog()
        } else if (hasFocus) {
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
                    val originText = binding.editInput.text.toString().trim()
                    binding.editInput.setText(originText.plus(tagStr))
                    binding.editInput.setSelection(originText.length + tagStr.length)
                }
            }
        }
        binding.editInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                viewModel.setCancelObserver(s.isEmpty())
                // 联想词搜索
                if (!s.isEmpty()) {
                    viewModel.loadThinkWord(s.toString().trim())
                }
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
        binding.ivDelete.setOnClickListener {
            DBManager.getDao().clearSearchRecords()
            viewModel.loadHistory()
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
        viewModel.cancelObserver.observe(this) { value ->
            binding.tvSearchCancel.text = getString(if (value) R.string.text_cancel else R.string.text_search)
            binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (value) R.color.text_main_color_30 else R.color.btn_color))
            binding.containerTopicHistory.isVisible = value
            binding.containerThink.isVisible = !value
        }
        lifecycleScope.launch {
            viewModel.searchHistory.collect { historyList ->
                updateSearchHistory(historyList)
            }
        }
        lifecycleScope.launch {
            viewModel.thinkWordObserver.collect { (keyWord, wordList) ->
                updateThinkWord(keyWord, wordList)
            }
        }
    }

    private fun updateSearchHistory(historyList: ArrayList<String>) {
        Log.d(TAG, "searchHistory: $historyList")
        binding.wrapLayout.clearChild()
        binding.wrapLayout.setAdapter(object : AutoWrapLayout.WrapAdapter {
            override fun getItemCount(): Int {
                return historyList.size
            }

            override fun onCreateView(p0: Int): View? {
                return TextView(this@SearchActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        EMUtil.dp2px(31f).toInt()
                    )
                    val showTextStr = historyList[p0].trim()
                    text = if (showTextStr.length > 30) showTextStr.substring(0, 30).plus("…") else showTextStr
                    gravity = Gravity.CENTER_VERTICAL
                    val horPadding = EMUtil.dp2px(12f).toInt()
                    val verPadding = EMUtil.dp2px(9f).toInt()
                    setPadding(horPadding, verPadding, horPadding, verPadding)

                    EMManager.from(this)
                        .setCorner(16f).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
                    setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
                    setOnClickListener {
                        Log.d(TAG, "搜索历史点击：${showTextStr}")
                        binding.editInput.setText(showTextStr)
                        binding.editInput.setSelection(showTextStr.length)
                        processSearch()
                    }
                }
            }
        })
    }

    /**
     * 更新联想词
     */
    private fun updateThinkWord(keyWord: String, wordList: ArrayList<String>) {
        Log.d(TAG, "thinkWord: ${wordList.joinToString(",")}")
        binding.containerThink.removeAllViews()
        wordList.forEach { word ->
            SearchThinkWordItem(this@SearchActivity).apply {
                this.updateKeyWord(keyWord, word)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                if (this.parent != null) {
                    (this.parent as? ViewGroup)?.removeView(this)
                }
                this@SearchActivity.binding.containerThink.addView(this)

                setOnClickListener {
                    Log.d(TAG, "联想词点击：$word")
                    this@SearchActivity.binding.editInput.setText(word)
                    this@SearchActivity.binding.editInput.setSelection(word.length)
                    processSearch()
                }
            }
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

    /**
     * 麦克风dialog
     */
    private fun showMicDialog() {
        micDialog = MicReceiveDialog.tryShowDialog(this)?.apply {
            setOnDismissListener {
                micDialog = null
            }
        }
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
        binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (viewModel.cancelObserver.value!!) R.color.text_main_color_50 else R.color.btn_color))
        updateEngineIcon()
        EMManager.from(binding.containerArea)
            .setCorner(21f)
            .setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_30))
        EMManager.from(binding.bottomTool).setBackGroundRealColor(ThemeManager.getSkinColor(R.color.view_bg_color))
        EMManager.from(binding.tvWwww).setCorner(4f).setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.tvCom).setCorner(4f).setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.tvCn).setCorner(4f).setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.tvPoint).setCorner(4f).setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        EMManager.from(binding.tvSlash).setCorner(4f).setBorderWidth(1f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.text_main_color))
        binding.tvHistory.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color_50))
        binding.ivDelete.setImageResource(ThemeManager.getSkinImageResId(R.drawable.t_night_iv_search_history_delete))
        viewModel.loadHistory()
        binding.containerThink.forEach {
            (it as? SearchThinkWordItem)?.updateThemeUI()
        }
        micDialog?.updateThemeUI()
    }

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }
}