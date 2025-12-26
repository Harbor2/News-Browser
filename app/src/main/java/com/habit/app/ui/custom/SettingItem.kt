package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.databinding.LayoutSettingItemBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager

class SettingItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    val binding: LayoutSettingItemBinding
    private var mTitle: String = ""
    private var mIcon: Int = -1

    private var switchStatus: Boolean? = null

    /**
     * 0 箭头样式
     * 1 文字样式
     * 2 开关样式
     */
    private var mType: Int = 0

    init {
        binding = LayoutSettingItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.SettingItem) {
            mType = getInteger(R.styleable.SettingItem_stiType, 0)
            mIcon = getResourceId(R.styleable.SettingItem_stiIcon, -1)
            mTitle = getString(R.styleable.SettingItem_stiTitle) ?: ""

            binding.tvTitle.text = mTitle
            binding.ivArrow.isVisible = false
            binding.ivSwitch.isVisible = false
            binding.tvDesc.isVisible = false
            when (mType) {
                0 -> {
                    binding.ivArrow.visibility = VISIBLE
                }
                1 -> {
                    binding.tvDesc.visibility = VISIBLE
                }
                2 -> {
                    binding.ivSwitch.visibility = VISIBLE
                }
            }
            updateThemeUi()
        }
    }

    fun updateDesc(desc: String) {
        if (mType != 1) return
        binding.tvDesc.isVisible = true
        binding.tvDesc.text = desc
    }

    fun updateSwitch(isChecked: Boolean) {
        if (mType != 2) return
        switchStatus = isChecked
        updateThemeUi()
    }

    fun updateIcon(iconRes: Int) {
        mIcon = iconRes
        updateThemeUi()
    }

    fun updateThemeUi() {
        if (mIcon != -1) {
            binding.ivIcon.setImageResource(ThemeManager.getSkinImageResId(mIcon))
        }
        binding.tvTitle.setTextColor(ThemeManager.getSkinColor(R.color.text_main_color))
        when (mType) {
            0 -> {
                binding.ivArrow.setImageResource(ThemeManager.getSkinImageResId(R.drawable.iv_b_folder_arrow))
            }
            2 -> {
                binding.ivSwitch.setImageResource(ThemeManager.getSkinImageResId(if (switchStatus == true) R.drawable.iv_s_on else R.drawable.iv_s_off))
            }
        }
        EMManager.from(binding.lineView)
            .setBackGroundRealColor(ThemeManager.getSkinColor(R.color.text_main_color_10))
    }
}