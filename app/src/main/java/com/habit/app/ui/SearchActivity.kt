package com.habit.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.habit.app.R
import com.habit.app.databinding.ActivitySearchBinding
import com.habit.app.event.EngineChangedEvent
import com.habit.app.helper.KeyValueManager
import com.habit.app.helper.ThemeManager
import com.habit.app.model.ENGINE_BAIDU
import com.habit.app.model.ENGINE_BING
import com.habit.app.model.ENGINE_DUCKDUCK
import com.habit.app.model.ENGINE_GOOGLE
import com.habit.app.model.ENGINE_YAHOO
import com.habit.app.model.ENGINE_YANDEX
import com.habit.app.model.TAG
import com.habit.app.ui.base.BaseActivity
import com.habit.app.ui.dialog.SearchEngineDialog
import com.wyz.emlibrary.em.EMManager
import com.wyz.emlibrary.util.EMUtil
import com.wyz.emlibrary.util.SoftKeyboardHelper
import com.wyz.emlibrary.util.immersiveWindow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import org.greenrobot.eventbus.Subscribe

/**
 * 搜索Activity
 */
class SearchActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val mScope = MainScope()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersiveWindow(binding.root, false, binding.containerNavi)
        softKeyboardHelper = SoftKeyboardHelper()

        initView()
        initData()
        initListener()
    }

    private fun initView() {
        updateUIConfig()
        if (hasFocus) {
            EMUtil.showSoftKeyboard(binding.editInput, this)
        }

        cancelObserver.observe(this) { value ->
            binding.tvSearchCancel.text = getString(if (value) R.string.text_cancel else R.string.text_search)
            binding.tvSearchCancel.setTextColor(ThemeManager.getSkinColor(if (value) R.color.text_main_color_30 else R.color.btn_color))
            binding.containerTopicHistory.isVisible = value
            binding.containerThink.isVisible = !value
        }
    }

    private fun initData() {

    }

    private fun initListener() {
        softKeyboardHelper.addKeyboardListener(this, keyboardListener)

        binding.tvSearchCancel.setOnClickListener {
            if (cancelObserver.value!!) {
                finish()
                return@setOnClickListener
            }
            WebViewSearchActivity.startActivity(this)
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
            override fun afterTextChanged(s: Editable?) {
                cancelObserver.value = s.isNullOrEmpty()
            }
        })
    }

    private fun adjustBottomTool(height: Int) {
        (binding.bottomTool.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            bottomMargin = height
            binding.bottomTool.layoutParams = this
        }
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

    private fun showEngineSelectDialog() {
        SearchEngineDialog.tryShowDialog(this)
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

    override fun onThemeChanged(theme: String) {
        super.onThemeChanged(theme)
        updateUIConfig()
    }

    @Subscribe
    fun onEngineChangedEvent(event: EngineChangedEvent) {
        updateEngineIcon()
    }

    override fun onDestroy() {
        mScope.cancel()
        softKeyboardHelper.removeKeyboardListener(this)
        super.onDestroy()
    }

    companion object {
        private var hasFocus: Boolean = false
        fun startActivity(context: Context, focus: Boolean = false) {
            this.hasFocus = focus
            context.startActivity(Intent(context, SearchActivity::class.java))
        }
    }
}