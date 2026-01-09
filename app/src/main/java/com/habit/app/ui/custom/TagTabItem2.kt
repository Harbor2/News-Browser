package com.habit.app.ui.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import com.habit.app.R
import com.habit.app.databinding.LayoutTagTabItemBinding
import com.habit.app.helper.ThemeManager
import com.wyz.emlibrary.em.EMManager

class TagTabItem2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    val binding: LayoutTagTabItemBinding
    private var mCount: Int = 0
    private var mSelect: Boolean = false

    init {
        binding = LayoutTagTabItemBinding.inflate(LayoutInflater.from(context), this, true)
        context.withStyledAttributes(attrs, R.styleable.TagTabItem2) {
            val title = getString(R.styleable.TagTabItem2_ttiTitle2) ?: ""
            mCount = getResourceId(R.styleable.TagTabItem2_ttiCount2, -1)
            mSelect = getBoolean(R.styleable.TagTabItem2_ttiSelect2, false)


            binding.tvTitle.text = title

            updateCount(mCount)
            updateSelect(mSelect, false)
        }
    }

    fun updateCount(count: Int) {
        mCount = count
        binding.tvCount.text = count.toString()
        binding.tvCount.isVisible = mSelect && count > 0
    }

    fun updateSelect(isSelect: Boolean, isPrivacy: Boolean) {
        mSelect = isSelect
        binding.tvCount.isVisible = isSelect && mCount > 0
        if (isSelect) {
            binding.tvTitle.setTextColor(ThemeManager.getSkinColor(if (!isPrivacy) R.color.tab_type_select_color1 else R.color.tab_type_select_color2))
        } else {
            binding.tvTitle.setTextColor(ThemeManager.getSkinColor(if (!isPrivacy) R.color.tab_type_unselect_color1 else R.color.tab_type_unselect_color2))
        }

        EMManager.from(binding.tvCount)
            .setCorner(4f)
            .setBorderWidth(1.5f)
            .setBorderRealColor(ThemeManager.getSkinColor(R.color.tab_count_border_color_20))
            .setTextRealColor(ThemeManager.getSkinColor(R.color.tab_count_border_color))
        EMManager.from(binding.root)
            .setCorner(16f)
            .setBackGroundRealColor(if (isSelect) ThemeManager.getSkinColor(R.color.page_main_color) else Color.TRANSPARENT)
    }
}