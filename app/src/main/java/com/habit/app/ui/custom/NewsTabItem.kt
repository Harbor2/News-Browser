package com.habit.app.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.helper.ThemeManager
import com.habit.app.databinding.LayoutNewsTabItemBinding

class NewsTabItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: LayoutNewsTabItemBinding
    private var isSelect: Boolean = false
    var mTabCategory: String = ""

    init {
        binding = LayoutNewsTabItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.NewsTabItem) {
            val tabCategory = getString(R.styleable.NewsTabItem_ntTitle) ?: ""
            val select = getBoolean(R.styleable.NewsTabItem_ntSelect, false)

            setData(tabCategory, select)
        }
    }

    fun setData(title: String, select: Boolean) {
        mTabCategory = title
        binding.tvTitle.text = title
        binding.tvTitleHint.text = title
        setSelect(select)
    }

    fun updateThemeUI() {
        binding.tvTitle.setTextColor(ThemeManager.getSkinColor(if (isSelect) R.color.text_main_color else R.color.text_main_color_40))
    }

    fun setSelect(select: Boolean) {
        isSelect = select
        updateThemeUI()
        binding.viewIndication.isVisible = select
        binding.tvTitle.textSize = if (select) 16f else 14f
        binding.tvTitle.typeface = ResourcesCompat.getFont(context, if (select) R.font.gotham_b else R.font.gotham_m)
    }
}